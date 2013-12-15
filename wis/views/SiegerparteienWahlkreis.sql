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

create or replace view Output_Wahlkreissieger as(
    select wk.wahlkreisnummer, wk.name Wahlkreis, p1.name SiegerErststimmen, p2.name SiegerZweitstimmen
    from Wahlkreissieger ws join wahlkreis wk on ws.wahlkreisid = wk.wahlkreisid
                            join partei p1 on ws.siegererststimmen = p1.parteiid
                            join partei p2 on ws.siegerzweitstimmen = p2.parteiid
);