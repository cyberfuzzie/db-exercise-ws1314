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

create or replace view KnappesteVerloreneWahlkreise as(
    with siegerParteien as(
        select distinct parteiid
        from mindestanzahlsitze
    ),
    maxStimmenWahlkreis as(
        select wahlkreisid, max(anzahlstimmen) max
        from summeerststimmen
        group by wahlkreisid
    ),
    differenzZuSieger as(
        select k.parteiid, se.wahlkreisid, wk.jahr, msw.max - se.anzahlstimmen Differenz
        from summeerststimmen se join maxStimmenWahlkreis msw on se.wahlkreisid = msw.wahlkreisid
                                 join kandidat k on se.kandidatid = k.kandidatid
                                 join wahlkreis wk on se.wahlkreisid = wk.wahlkreisid
        where k.parteiid not in (select * from siegerParteien)
    )
    select x.parteiid, x.wahlkreisid, x.differenz
    from (select d.*, row_number() over (partition by d.parteiid, d.jahr order by d.Differenz) as number
          from differenzZuSieger d) x
    where x.number <= 5
);