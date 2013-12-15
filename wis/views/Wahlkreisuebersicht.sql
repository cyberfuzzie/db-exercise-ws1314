-- 1
create or replace view Wahlbeteiligung as(
    with erststimmenProWahlkreis as(
        select wahlkreisid,sum(anzahlstimmen) summe
        from summeerststimmen
        group by wahlkreisid
    ),
    zweitstimmenProWahlkreis as(
        select wahlkreisid,sum(anzahlstimmen) summe
        from summezweitstimmen
        group by wahlkreisid
    )
    select wk.wahlkreisid, ((epw.summe + zpw.summe)::float/(wk.wahlberechtigte*2)::float)*100 wahlbeteiligung
    from wahlkreis wk join erststimmenProWahlkreis epw on wk.wahlkreisid = epw.wahlkreisid
                      join zweitstimmenProWahlkreis zpw on wk.wahlkreisid = zpw.wahlkreisid
);

create or replace view Output_Wahlbeteiligung as(
    select wk.wahlkreisnummer, wk.name Wahlkreis, wk.jahr, wahlbeteiligung
    from Wahlbeteiligung wb join wahlkreis wk on wb.wahlkreisid = wk.wahlkreisid
);
-- 2
create or replace view Output_GewaehlterDirektKandidat as(
    select wk.wahlkreisnummer, wk.name Wahlkreis, k.name Kandidat
    from direktmandate d join kandidat k on d.kandidatid = k.kandidatid
                         join wahlkreis wk on d.wahlkreisid = wk.wahlkreisid
                         left join partei p on k.parteiid = p.parteiid
);
-- 3
create or replace view ProzentualeAbsoluteStimmenPartei as(
    with summeProWahlkreis as(
        select wahlkreisid, sum(anzahlstimmen) summe
        from summezweitstimmen
        group by wahlkreisid
    )
    select sz.wahlkreisid, sz.parteiid, sum(anzahlstimmen) summe, sum(anzahlstimmen)::float/spw.summe::float*100 prozent
    from summezweitstimmen sz join summeprowahlkreis spw on sz.wahlkreisid = spw.wahlkreisid
    group by sz.wahlkreisid,sz.parteiid,spw.summe
);

create or replace view Output_ProzentualeAbsoluteStimmenPartei as(
    select wk.wahlkreisnummer, wk.name Wahlkreis, p.name Partei, pap.summe, pap.prozent
    from ProzentualeAbsoluteStimmenPartei pap join wahlkreis wk on pap.wahlkreisid = wk.wahlkreisid
                                              join partei p on pap.parteiid = p.parteiid
);
-- 4
create or replace view EntwicklungStimmen as(
    with wk13 as (
        select * from wahlkreis where jahr = 2013
    ),
    wk09 as (
        select * from wahlkreis where jahr = 2009
    ),
    WahlkreiseMapped as(
        select w1.wahlkreisid wahlkreisid13,w2.wahlkreisid wahlkreisid09
        from wk13 w1 left join wk09 w2 on w1.name = w2.name
    )
    select pap13.wahlkreisid, pap13.parteiid, coalesce(pap13.summe,0) Summe13, coalesce(pap09.summe,0) Summe09, (coalesce(pap13.summe,0) - coalesce(pap09.summe,0)) Differenz
    from prozentualeabsolutestimmenpartei pap13 
        left join WahlkreiseMapped wkm on pap13.wahlkreisid = wkm.wahlkreisid13
        left join prozentualeabsolutestimmenpartei pap09 on pap09.wahlkreisid = wkm.wahlkreisid09 and pap13.parteiid = pap09.parteiid
);

create or replace view Output_EntwicklungStimmen as(
    select wk.wahlkreisnummer, wk.name Wahlkreis, p.name Partei, summe13, summe09, differenz
    from EntwicklungStimmen es join wahlkreis wk on es.wahlkreisid = wk.wahlkreisid
                               join partei p on es.parteiid = p.parteiid
);