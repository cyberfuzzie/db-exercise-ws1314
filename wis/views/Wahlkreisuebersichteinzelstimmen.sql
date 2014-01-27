/*Unterschied zur Wahlkreisübersicht auf aggregierten Stimmen:
  werte für Stimmsummen kommen aus der View summe(erst|zweit)stimmenlive*/

--1
create or replace view WahlbeteiligungEinzelstimmen as(
    with erststimmenProWahlkreis as(
        select wahlkreisid,sum(anzahlstimmen) summe
        from summeerststimmenlive
        group by wahlkreisid
    ),
    zweitstimmenProWahlkreis as(
        select wahlkreisid,sum(anzahlstimmen) summe
        from summezweitstimmenlive
        group by wahlkreisid
    )
    select wk.wahlkreisid, ((epw.summe + zpw.summe)::float/(wk.wahlberechtigte*2)::float)*100 wahlbeteiligung
    from wahlkreis wk join erststimmenProWahlkreis epw on wk.wahlkreisid = epw.wahlkreisid
                      join zweitstimmenProWahlkreis zpw on wk.wahlkreisid = zpw.wahlkreisid
);

create or replace view Output_WahlbeteiligungEinzelstimmen as(
    select wk.wahlkreisnummer, wk.name Wahlkreis, wk.jahr, wahlbeteiligung
    from WahlbeteiligungEinzelstimmen wb join wahlkreis wk on wb.wahlkreisid = wk.wahlkreisid
);
--2
create or replace view GewaehlterDirektKandidatEinzelstimmen as(
    with tmpDirektMandate as(
        select s1.*
          from summeerststimmenlive s1
            left join summeerststimmenlive s2
              on s1.wahlkreisid = s2.wahlkreisid
              and s1.anzahlstimmen < s2.anzahlstimmen
          where s2.anzahlstimmen is null
    )
    select wk.wahlkreisid, k.kandidatid
        from tmpdirektmandate d join kandidat k on d.kandidatid = k.kandidatid
                             join wahlkreis wk on d.wahlkreisid = wk.wahlkreisid
                             left join partei p on k.parteiid = p.parteiid
);
--3
create or replace view ProzentualeAbsoluteStimmenParteiEinzelstimmen as(
    with summeProWahlkreis as(
            select wahlkreisid, sum(anzahlstimmen) summe
            from summezweitstimmenlive
            group by wahlkreisid
        )
    select sz.wahlkreisid, sz.parteiid, sum(anzahlstimmen) summe, sum(anzahlstimmen)::float/spw.summe::float*100 prozent
    from summezweitstimmenlive sz join summeprowahlkreis spw on sz.wahlkreisid = spw.wahlkreisid
    group by sz.wahlkreisid,sz.parteiid,spw.summe
);

create or replace view Output_ProzentualeAbsoluteStimmenParteiEinzelstimmen as(
    select wk.wahlkreisnummer, wk.name Wahlkreis, p.name Partei, pap.summe, pap.prozent
    from ProzentualeAbsoluteStimmenParteiEinzelstimmen pap join wahlkreis wk on pap.wahlkreisid = wk.wahlkreisid
                                                           join partei p on pap.parteiid = p.parteiid
);
--4
create or replace view EntwicklungStimmenEinzelstimmen as(
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
    from prozentualeabsolutestimmenparteieinzelstimmen pap13 
        left join WahlkreiseMapped wkm on pap13.wahlkreisid = wkm.wahlkreisid13
        right join prozentualeabsolutestimmenparteieinzelstimmen pap09 on pap09.wahlkreisid = wkm.wahlkreisid09 and pap13.parteiid = pap09.parteiid
);

create or replace view Output_EntwicklungStimmenEinzelstimmen as(
    select wk.wahlkreisnummer, wk.name Wahlkreis, p.name Partei, summe13, summe09, differenz
    from EntwicklungStimmenEinzelstimmen es join wahlkreis wk on es.wahlkreisid = wk.wahlkreisid
                               join partei p on es.parteiid = p.parteiid
);