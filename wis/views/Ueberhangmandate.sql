create or replace view Ueberhangmandate as(
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
    hoechstzahlennummeriert as (
      select h.parteiid,h.bundeslandid,h.gesamtsitze,h.sitze,mas.sitze mindestsitze,greatest((mas.sitze - h.sitze),0) fehler,row_number() over (partition by h.parteiid order by h.hz desc) as row from hoechstzahlen h
        inner join mindestanzahlsitze mas
          on h.parteiid = mas.parteiid
          and h.bundeslandid = mas.bundeslandid
    ),
    SitzeParteienLaenderOhneUeberhang as (
      select parteiid,bundeslandid,max(sitze) sitze from hoechstzahlennummeriert
        where row <= gesamtsitze
        group by parteiid,bundeslandid
    )
  select m.bundeslandid, m.parteiid, greatest(0, m.sitze-s.sitze) AnzahlUeberhangmandate
    from SitzeParteienLaenderOhneUeberhang s
      inner join mindestanzahlsitze m
        on m.bundeslandid = s.bundeslandid
        and m.parteiid = s.parteiid
);

create or replace view Output_Ueberhangmandate as(
    select b.name Bundesland, p.name Partei, anzahlueberhangmandate
    from Ueberhangmandate u join bundesland b on u.bundeslandid = b.bundeslandid
                            join partei p on u.parteiid = p.parteiid
);
