create or replace view SummeErststimmenLive as (
  select kandidatid,wahlkreisid,count(*) as AnzahlStimmen
    from erststimme
    group by kandidatid, wahlkreisid
);

create or replace view SummeZweitstimmenLive as (
  select parteiid,wahlkreisid,count(*) as AnzahlStimmen
  from zweitstimme
  group by parteiid, wahlkreisid
);

-- Direktmandate im Jahr 2013, gesamter Datensatz
create or replace view DirektMandate as (
  select s1.*
  from summeerststimmen s1
    left join summeerststimmen s2
      on s1.wahlkreisid = s2.wahlkreisid
      and s1.anzahlstimmen < s2.anzahlstimmen
      join wahlkreis wk on s1.wahlkreisid = wk.wahlkreisid
  where s2.anzahlstimmen is null and wk.jahr = 2013
);
-- Anzahl an Direktmandaten pro Partei und Bundesland
create or replace view anzahldirektmandate as (
  with
    anzahldirektmandateohnenull as (
      select wk.bundeslandid,k.parteiid,count(*) sitze from direktmandate dm
        inner join kandidat k
          on dm.kandidatid = k.kandidatid
        inner join wahlkreis wk
          on dm.wahlkreisid = wk.wahlkreisid
      group by wk.bundeslandid,k.parteiid
    )
  select b.bundeslandid,p.parteiid,coalesce(adm.sitze,0) anzahl from partei p
    join bundesland b
      on p.parteiid in (select * from bundestagsparteien)
    left join anzahldirektmandateohnenull adm
      on p.parteiid = adm.parteiid
        and b.bundeslandid = adm.bundeslandid
);

-- Prozentualer Anteil der Zweitstimmen, bundesweit
create or replace view ZweitstimmenProzent as (
  with wahlkreis2013 as (
      select wahlkreisid
        from wahlkreis
        where jahr = 2013
    ),
    summezweitstimmen2013 as (
      select wahlkreisid,parteiid,anzahlstimmen from summezweitstimmen
        where wahlkreisid in (select wahlkreisid from wahlkreis2013)
    ),
    gesamtzweitstimmen as (
      select sum(anzahlstimmen) gesamt from summezweitstimmen2013
    )
  select parteiid,(sum(anzahlstimmen)::float/(select gesamt from gesamtzweitstimmen)) prozent
    from summezweitstimmen2013
    group by parteiid
);

-- Alle Parteien, die im Bundestag vertreten sind
-- Entweder 5% der Zweistimmen, oder 3 Direktmandate
create or replace view BundestagsParteien as (
  select parteiid from ZweitstimmenProzent
    where prozent >= 0.05
  union
  select parteiid from DirektMandate
    inner join kandidat
      on Direktmandate.kandidatid = kandidat.kandidatid
    group by parteiid
    having count(*) >= 3
);

-- Aufteilung der (598) Sitze im Bundestag auf die Bundesländer mit Höchstzahlverfahren
create or replace view BundeslandSitze as (
  with
    einwohner2013 as (
      select bundeslandid,generate_series(1,598) sitze,anzahl from einwohnerzahl where jahr = 2013
    ),
    hoechstzahlen as (
      select bundeslandid,sitze,anzahl/(sitze-0.5) hz from einwohner2013
        order by hz desc
        limit 598
    )
  select bundeslandid,max(sitze) sitze from hoechstzahlen
    group by bundeslandid
);

-- Aufteilung der Sitze in jedem Bundesland auf die zu berücksichtigenden
-- Parteien gemäß der Zweitstimmen nach dem Höchstzahlverfahren
create or replace view Listenergebnis as (
  with
    bundeslandserie as (
      select bs.bundeslandid,bs.sitze gesamtsitze,parteiid,sum(anzahlstimmen) stimmen,generate_series(1,sitze) sitze from summezweitstimmen szs
        inner join wahlkreis wk
          on szs.wahlkreisid = wk.wahlkreisid
        inner join BundeslandSitze bs
          on wk.bundeslandid = bs.bundeslandid
      where parteiid in (select parteiid from bundestagsparteien)
            and wk.jahr = 2013
      group by bs.bundeslandid,bs.sitze,parteiid
    ),
    hoechstzahlen as (
      select bundeslandid,parteiid,gesamtsitze,sitze,stimmen/(sitze-0.5) hz from bundeslandserie
    ),
    hoechstzahlennummeriert as (
      select bundeslandid,parteiid,gesamtsitze,sitze,hz,row_number() over (partition by bundeslandid order by bundeslandid asc, hz desc) as row from hoechstzahlen
    )
  select bundeslandid,parteiid,max(sitze) sitze from hoechstzahlennummeriert
    where row <= gesamtsitze
    group by bundeslandid,parteiid,gesamtsitze
);

-- Mindestanzahl an Sitzen in jedem Bundesland
create or replace view MindestanzahlSitze as (
  select l.bundeslandid,l.parteiid,greatest(l.sitze,m.anzahl) sitze from listenergebnis l
    left join anzahldirektmandate m
      on l.bundeslandid = m.bundeslandid
       and l.parteiid = m.parteiid
);

-- Endgültige Aufteilung der Sitze im Bundestag auf die Parteien
-- Höchstzahlverfahren mit variabler Anzahl an Sitzen
create or replace view SitzeParteienBundesweit as (
    with wahlkreis2013 as (
      select wahlkreisid
        from wahlkreis
        where jahr = 2013
    ),
    -- Anzahl Zweitstimmen bundesweit
    bundeszweitstimmen as (
      select parteiid,sum(anzahlstimmen) stimmen from summezweitstimmen
      where parteiid in (select * from bundestagsparteien)
        and wahlkreisid in (select * from wahlkreis2013)
      group by parteiid
    ),
    -- Mindestanzahl an Sitzen bundesweit
    mindestsitzebund as (
      select parteiid,sum(sitze) sitze from MindestanzahlSitze
      group by parteiid
    ),
    zweitstimmenserie as (
      select bzs.parteiid,msb.sitze mindestsitze,stimmen,generate_series(1,598) sitze from bundeszweitstimmen bzs
        inner join mindestsitzebund msb
          on bzs.parteiid = msb.parteiid
    ),
    hoechstzahlen as (
      select parteiid,mindestsitze,sitze,stimmen/(sitze-0.5) hz from zweitstimmenserie
    ),
    hoechstzahlennummeriert as (
      select parteiid,mindestsitze,sitze,row_number() over (order by hz desc) as row from hoechstzahlen
    ),
    -- Für jede Partei die Anzahl an Sitzen, die der Bundestag insgesamt mindestens haben muss
    mindestgroesse as (
      select parteiid,min(row) minsitze from hoechstzahlennummeriert
      where sitze >= mindestsitze
      group by parteiid
    )
  -- Endgültige Größe des Bundestags durch `mindestgroesse` festgelegt,
  -- jetzt für jede Partei die tatsächliche Anzahl an Sitzen berechnen
  select parteiid,max(sitze) sitze from hoechstzahlennummeriert
    where row <= (select max(minsitze) from mindestgroesse)
      group by parteiid
);

-- Aufteilung der pro Partei bundesweit errungenen Sitze auf die Bundesländer
-- zur Berechnung der pro Bundesland errungenen Listenmandate
create or replace view AnzahlListenMandate as (
  with
    wahlkreis2013 as (
      select wahlkreisid
        from wahlkreis
        where jahr = 2013
    ),
    -- Zweitstimmen pro Partei und Bundesland
    bundeslandzweitstimmen as (
      select szs.parteiid,wk.bundeslandid,sum(szs.anzahlstimmen) stimmen from summezweitstimmen szs
        inner join wahlkreis wk
          on szs.wahlkreisid = wk.wahlkreisid
      where szs.parteiid in (select * from bundestagsparteien)
        and wk.wahlkreisid in (select * from wahlkreis2013)
      group by szs.parteiid,wk.bundeslandid
    ),
    bzsserie as (
      select spb.parteiid,bzs.bundeslandid,spb.sitze gesamtsitze,bzs.stimmen,generate_series(1,sitze) sitze from sitzeparteienbundesweit spb
        inner join bundeslandzweitstimmen bzs
          on spb.parteiid = bzs.parteiid
    ),
    -- Höchstzahlen für jede Kombination aus Partei und Bundesland bilden
    -- In jedem Bundesland die ersten [Anzahl Direktmandate] Sitze pro Partei weglassen
    hoechstzahlen as (
      select bzsserie.parteiid,bzsserie.bundeslandid,gesamtsitze,stimmen/(bzsserie.sitze-0.5) hz,bzsserie.sitze sitze,adm.anzahl mindestsitze from bzsserie
        left outer join anzahldirektmandate adm
          on bzsserie.parteiid = adm.parteiid
            and bzsserie.bundeslandid = adm.bundeslandid
        where bzsserie.sitze > adm.anzahl
    ),
    -- Pro Partei bundesweit zu vergebende Anzahl an Listenmandaten
    listenplaetzeanzahlbundesweit as (
      select adm.parteiid,spb.sitze-sum(adm.sitze) sitze from anzahldirektmandate adm
        inner join SitzeParteienBundesweit spb
          on adm.parteiid = spb.parteiid
        group by adm.parteiid,spb.sitze
    ),
    -- Höchstzahlen pro Partei absteigend sortieren und nummerieren
    listenserie as (
      select hz.parteiid,hz.bundeslandid,lpab.sitze sitze,row_number() over (partition by hz.parteiid order by hz.hz desc) as row from hoechstzahlen hz
        left join listenplaetzeanzahlbundesweit lpab
          on hz.parteiid = lpab.parteiid
    )
  -- Pro Bundesland errungene Listenmandate zählen
  select parteiid,bundeslandid,count(*) sitze from listenserie
    where row <= sitze
    group by parteiid,bundeslandid
);


create or replace view ListenMandate as (
  with
    -- gewählte Direktkandidaten aus Landeslisten entfernen
    moeglichelistenkandidaten as (
      select parteiid,bundeslandid,kandidatid,nummer from listenplatz
        where kandidatid not in (select kandidatid from direktmandate) and jahr = 2013
    ),
    -- Verbleibende Listenkandidaten entsprechende Position auf Liste aufsteigend nummerieren
    listenkandidatennummeriert as (
      select alm.parteiid,alm.bundeslandid,alm.sitze,mlk.kandidatid,mlk.nummer,row_number() over (partition by alm.parteiid,alm.bundeslandid order by nummer asc) as row from AnzahlListenMandate alm
        left join moeglichelistenkandidaten mlk
          on alm.parteiid = mlk.parteiid
             and alm.bundeslandid = mlk.bundeslandid
    )
  -- Gemäß Anzahl an Listenmandaten die gewählten Listenkandidaten auswählen
  select parteiid,bundeslandid,kandidatid,nummer from listenkandidatennummeriert
    where row <= sitze
);


-- Mitglieder des Bundestags
-- Alle gewählten Direktkandidaten und alle über Landeslisten gewählten Kandidaten
create or replace view MdBs as (
  select parteiid,bundeslandid,kandidatid,nummer listenplatz from ListenMandate
  union all
  select k.parteiid,w.bundeslandid,k.kandidatid,null from DirektMandate dm
    inner join kandidat k
      on dm.kandidatid = k.kandidatid
    inner join wahlkreis w
      on dm.wahlkreisid = w.wahlkreisid
);


-- Views zur Ausgabe
create or replace view Output_SitzeParteienBundesweit as (
  select spb.sitze sitze, p.name partei
    from sitzeParteienBundesweit spb
      inner join partei p on spb.parteiid = p.parteiid
);

create or replace view Output_MdBs as (
  select Kandidat.name kandidat, Partei.name partei
    from MdBs
      inner join Kandidat
        on MdBs.kandidatid = kandidat.kandidatid
      inner join Partei
        on MdBs.parteiid = Partei.parteiid
);
