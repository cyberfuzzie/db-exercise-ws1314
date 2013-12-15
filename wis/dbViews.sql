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


create or replace view DirektMandate as (
  select s1.*
  from summeerststimmen s1
    left join summeerststimmen s2
      on s1.wahlkreisid = s2.wahlkreisid
      and s1.anzahlstimmen < s2.anzahlstimmen
      join wahlkreis wk on s1.wahlkreisid = wk.wahlkreisid
  where s2.anzahlstimmen is null and wk.jahr = 2013
);

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

create or replace view MindestanzahlSitze as (
  with anzahldirektmandate as (
      select wk.bundeslandid,k.parteiid,count(*) anzahl from direktmandate dm
        inner join kandidat k
          on dm.kandidatid = k.kandidatid
        inner join wahlkreis wk
          on dm.wahlkreisid = wk.wahlkreisid
      group by wk.bundeslandid,k.parteiid
    )
  select l.bundeslandid,l.parteiid,greatest(l.sitze,m.anzahl) sitze from listenergebnis l
    left join anzahldirektmandate m
      on l.bundeslandid = m.bundeslandid
       and l.parteiid = m.parteiid
);

create or replace view SitzeParteienBundesweit as (
    with wahlkreis2013 as (
      select wahlkreisid
        from wahlkreis
        where jahr = 2013
    ),
    bundeszweitstimmen as (
      select parteiid,sum(anzahlstimmen) stimmen from summezweitstimmen
      where parteiid in (select * from bundestagsparteien)
        and wahlkreisid in (select * from wahlkreis2013)
      group by parteiid
    ),
    mindestsitzebund as (
      select parteiid,sum(sitze) sitze from listenergebnis
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
    mindestgroesse as (
      select parteiid,min(row) minsitze from hoechstzahlennummeriert
      where sitze >= mindestsitze
      group by parteiid
    )
  select parteiid,max(sitze) sitze from hoechstzahlennummeriert
    where row <= (select max(minsitze) from mindestgroesse)
      group by parteiid
);

create or replace view SitzeParteienLaender as (
  with
    bundeslandzweitstimmen as (
      select szs.parteiid,wk.bundeslandid,sum(szs.anzahlstimmen) stimmen from summezweitstimmen szs
        inner join wahlkreis wk
          on szs.wahlkreisid = wk.wahlkreisid
      where szs.parteiid in (select * from bundestagsparteien)
      group by szs.parteiid,wk.bundeslandid
    ),
    bzsserie as (
      select spb.parteiid,bzs.bundeslandid,spb.sitze gesamtsitze,bzs.stimmen,generate_series(1,sitze) sitze  from sitzeparteienbundesweit spb
        inner join bundeslandzweitstimmen bzs
          on spb.parteiid = bzs.parteiid
    ),
    hoechstzahlen as (
      select parteiid,bundeslandid,gesamtsitze,stimmen/(sitze-0.5) hz,sitze from bzsserie
    ),
    hoechstzahlenmitfehler as (
      select h.parteiid,h.bundeslandid,h.gesamtsitze,h.sitze,mas.sitze mindestsitze,greatest((mas.sitze - h.sitze),0) fehler,row_number() over (partition by h.parteiid order by h.hz desc) as row from hoechstzahlen h
        inner join mindestanzahlsitze mas
          on h.parteiid = mas.parteiid
             and h.bundeslandid = mas.bundeslandid
    ),
    hoechstzahlengesamtfehler as (
      select hzf1.parteiid,hzf1.bundeslandid blid1,hzf1.row,hzf1.gesamtsitze,hzf1.sitze,min(hzf2.fehler) blfehler from hoechstzahlenmitfehler hzf1
        inner join hoechstzahlenmitfehler hzf2
          on hzf1.parteiid = hzf2.parteiid
             and hzf1.row >= hzf2.row
      where hzf1.row <= hzf1.gesamtsitze
      group by hzf1.parteiid,hzf1.bundeslandid,hzf1.row,hzf1.gesamtsitze,hzf1.sitze,hzf2.bundeslandid
    ),
    sitzberechnungparteien as (
      select parteiid,blid1 bundeslandid,sitze from hoechstzahlengesamtfehler
      group by parteiid,blid1,row,gesamtsitze,sitze
      having row+sum(blfehler) <= gesamtsitze
    ),
    sitzelandeslisten as (
      select parteiid,bundeslandid,max(sitze) sitze from sitzberechnungparteien
      group by parteiid,bundeslandid
    )
  select sll.parteiid,sll.bundeslandid,greatest(sll.sitze,mas.sitze) sitze from sitzelandeslisten sll
    full outer join mindestanzahlsitze mas
      on sll.bundeslandid = mas.bundeslandid
      and sll.parteiid = mas.parteiid
);

create or replace view ListenMandate as (
  with anzahldirektmandate as (
      select wk.bundeslandid,k.parteiid,count(*) direktsitze from direktmandate dm
        inner join kandidat k
          on dm.kandidatid = k.kandidatid
        inner join wahlkreis wk
          on dm.wahlkreisid = wk.wahlkreisid
      group by wk.bundeslandid,k.parteiid
    ),
    anzahllistensitze as (
      select spl.parteiid,spl.bundeslandid,spl.sitze-coalesce(adm.direktsitze,0) listensitze from sitzeparteienlaender spl
        left join anzahldirektmandate adm
          on spl.parteiid = adm.parteiid
             and spl.bundeslandid = adm.bundeslandid
    ),
    moeglichelistenkandidaten as (
      select parteiid,bundeslandid,kandidatid,nummer from listenplatz
        where kandidatid not in (select kandidatid from direktmandate) and jahr = 2013
    ),
    listenkandidatennummeriert as (
      select als.parteiid,als.bundeslandid,als.listensitze,mlk.kandidatid,mlk.nummer,row_number() over (partition by als.parteiid,als.bundeslandid order by nummer) as row from anzahllistensitze als
        left join moeglichelistenkandidaten mlk
          on als.parteiid = mlk.parteiid
             and als.bundeslandid = mlk.bundeslandid
    )
  select parteiid,bundeslandid,kandidatid,nummer from listenkandidatennummeriert
    where row <= listensitze
);

create or replace view MdBs as (
  select parteiid,bundeslandid,kandidatid,nummer listenplatz from ListenMandate
  union all
  select k.parteiid,w.bundeslandid,k.kandidatid,null from DirektMandate dm
    inner join kandidat k
      on dm.kandidatid = k.kandidatid
    inner join wahlkreis w
      on dm.wahlkreisid = w.wahlkreisid
);