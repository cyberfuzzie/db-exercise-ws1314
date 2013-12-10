-- 1

-- 2
select k."name"
from direktmandate d join kandidat k on d.kandidatid = k.kandidatid
                     join wahlkreis wk on d.wahlkreisid = wk.wahlkreisid
                     left join partei p on k.parteiid = p.parteiid
where wk.wahlkreisnummer = ? and wk.jahr = 2013

-- 3
with summepropartei as(
    select parteiid,sum(anzahlstimmen) anzahl
    from summezweitstimmen sz join wahlkreis wk on sz.wahlkreisid = wk.wahlkreisid
    where wk.jahr = 2013
    group by parteiid
)
select p."name", coalesce(zp.prozent,0) prozent, coalesce(sp.anzahl,0) anzahl
from partei p left join zweitstimmenprozent zp on p.parteiid = zp.parteiid
              left join summepropartei sp on sp.parteiid = p.parteiid

-- 4
with wk13 as (
    select * from wahlkreis where jahr = 2013
),
wk09 as (
    select * from wahlkreis where jahr = 2009
)
select *
from wk13 w1 left join wk09 w2 on w1.name = w2.name
order by w2.wahlkreisid,w1.wahlkreisnummer