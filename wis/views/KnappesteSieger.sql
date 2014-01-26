create or replace view KnappesteSieger as(
    with wahlkreise13 as(
        select wahlkreisid
        from wahlkreis
        where jahr = 2013
    ),
    /*Zuerst werden die Erst- und Zweitplatzierten Parteien pro Wahlkreis nach Zweitstimmen
      bestimmen. Und daraus der Erstplatzierte mit Vorsprung bestimmen*/
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
    /*Danach werden noch die 10 Wahlkreise für jede Partei mit dem geringsten Vorsprung
      bestimmt*/
    select wahlkreisid,parteiid,kandidatid, vorsprung
    from (select row_number() over (partition by parteiid order by vorsprung) as number, emv.*
          from ersterMitVorsprung emv) x
    where x.number <= 10
);

/*Output-View*/
create or replace view Output_KnappesteSieger as(
    select p.name Partei, k.name Kandidat, wk.wahlkreisnummer, wk.jahr, ks.vorsprung,wk.name Wahlkreis
    from KnappesteSieger ks join wahlkreis wk on ks.wahlkreisid = wk.wahlkreisid
                            join partei p on ks.parteiid = p.parteiid
                            join kandidat k on ks.kandidatid = k.kandidatid
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
        where k.parteiid not in (select * from siegerParteien) and wk.jahr = 2013
    )
    select x.parteiid, x.wahlkreisid, x.differenz
    from (select d.*, row_number() over (partition by d.parteiid, d.jahr order by d.Differenz) as number
          from differenzZuSieger d) x
    where x.number <= 5 and x.jahr = 2013
);

create or replace view Output_KnappesteVerloreneWahlkreise as(
    select p."name" partei, wk.wahlkreisnummer, kvw.differenz,wk.name Wahlkreis
    from knappesteverlorenewahlkreise kvw join partei p on kvw.parteiid = p.parteiid
                                          join wahlkreis wk on kvw.wahlkreisid = wk.wahlkreisid
);

create or replace view KnappsteWahlkreise as (
  with
    wahlkreis2013 as (
      select wahlkreisid
        from wahlkreis
        where jahr = 2013
    ),
    wahlkreissiegerparteien as (
      select k.parteiid,ses.kandidatid,ses.wahlkreisid,ses.anzahlstimmen from summeerststimmen ses
        left join summeerststimmen ses2
          on ses.wahlkreisid = ses2.wahlkreisid
          and ses.anzahlstimmen < ses2.anzahlstimmen
        inner join kandidat k
          on ses.kandidatid = k.kandidatid
        where ses.wahlkreisid in (select * from wahlkreis2013)
          and ses2.wahlkreisid is null
    ),
    wahlkreiszweiteparteien as (
      select ses.kandidatid,ses.wahlkreisid,ses.anzahlstimmen from summeerststimmen ses
        inner join summeerststimmen ses2
          on ses.wahlkreisid = ses2.wahlkreisid
          and ses.anzahlstimmen < ses2.anzahlstimmen
        where ses.wahlkreisid in (select * from wahlkreis2013)
        group by ses.kandidatid,ses.wahlkreisid,ses.anzahlstimmen
        having count(*) = 1
    ),
    wahlkreisabstanderster as (
      select wsp.parteiid,wsp.kandidatid,wsp.wahlkreisid,wsp.anzahlstimmen-wzp.anzahlstimmen abstand,row_number() over (partition by wsp.parteiid order by wsp.anzahlstimmen-wzp.anzahlstimmen asc) as row from wahlkreissiegerparteien wsp
        inner join wahlkreiszweiteparteien wzp
          on wsp.wahlkreisid = wzp.wahlkreisid
    ),
    knappstesieger as (
      select wae.parteiid,wae.kandidatid,wae.wahlkreisid,wae.abstand from wahlkreisabstanderster wae
        where row <= 10
    ),
    wahlkreisrueckstand as (
      select k.parteiid,ses.kandidatid,ses.wahlkreisid,ses.anzahlstimmen-wsp.anzahlstimmen abstand,row_number() over (partition by k.parteiid order by ses.anzahlstimmen-wsp.anzahlstimmen desc) as row from summeerststimmen ses
        inner join kandidat k
          on ses.kandidatid = k.kandidatid
        inner join wahlkreissiegerparteien wsp
          on ses.wahlkreisid = wsp.wahlkreisid
        where k.parteiid not in (select parteiid from wahlkreissiegerparteien)
    ),
    knappsteverlierer as (
      select wr.parteiid,wr.kandidatid,wr.wahlkreisid,wr.abstand from wahlkreisrueckstand wr
        where row <= 10
    )
  select ks.parteiid,ks.kandidatid,ks.wahlkreisid,ks.abstand from knappstesieger ks
  union all
  select kv.parteiid,kv.kandidatid,kv.wahlkreisid,kv.abstand from knappsteverlierer kv
);

create or replace view Output_KnappsteWahlkreise as(
    select p.name partei,k.name kandidat,wk.wahlkreisnummer,wk.name wahlkreis,kw.abstand
      from KnappsteWahlkreise kw
        inner join partei p on kw.parteiid = p.parteiid
        inner join wahlkreis wk on kw.wahlkreisid = wk.wahlkreisid
        inner join kandidat k on kw.kandidatid = k.kandidatid
);
