create or replace view KnappesteSieger as(
    with wahlkreise13 as(
        select wahlkreisid
        from wahlkreis
        where jahr = 2013
    ),
    ersterProWahlkreis as(
        select wahlkreisid, max(anzahlstimmen) erster
        from summeerststimmen
        where wahlkreisid in (select * from wahlkreise13)
        group by wahlkreisid
    ),
    zweiterProWahlkreis as(
        select wahlkreisid, max(anzahlstimmen) zweiter
        from summeerststimmen
        where anzahlstimmen not in (select erster as anzahlstimmen from ersterProWahlkreis)
              and wahlkreisid in (select * from wahlkreise13)
        group by wahlkreisid
    ),
    ersterMitVorsprung as(
        select f.wahlkreisid,k.parteiid, se.kandidatid, f.erster - s.zweiter Vorsprung
        from ersterProWahlkreis f join zweiterProWahlkreis s on f.wahlkreisid = s.wahlkreisid
                                  join summeerststimmen se on f.erster = se.anzahlstimmen
                                        and se.wahlkreisid = f.wahlkreisid
                                  join kandidat k on se.kandidatid = k.kandidatid
    )
    select wahlkreisid,parteiid,kandidatid
    from (select row_number() over (partition by parteiid order by vorsprung) as number, emv.*
          from ersterMitVorsprung emv) x
    where x.number <= 10
);