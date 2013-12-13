create or replace view Wahlkreissieger as(
    with maxWkErststimmen as(
        select wahlkreisid, max(anzahlstimmen) max
        from summeerststimmen
        group by wahlkreisid
    ),
    maxWkZweitstimmen as(
        select wahlkreisid, max(anzahlstimmen) max
        from summezweitstimmen
        group by wahlkreisid
    )
    select se.wahlkreisid, k.parteiid SiegerErststimmen, sz.parteiid SiegerZweitstimmen
    from summeerststimmen se join summezweitstimmen sz on se.wahlkreisid = sz.wahlkreisid
                             join kandidat k on se.kandidatid = k.kandidatid
                             join maxwkerststimmen mwe on mwe.wahlkreisid = se.wahlkreisid
                             join maxwkzweitstimmen mwz on mwz.wahlkreisid = se.wahlkreisid
    where se.anzahlstimmen = mwe.max and sz.anzahlstimmen = mwz.max
);