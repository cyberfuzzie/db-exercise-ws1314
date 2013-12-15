create or replace function erzeugeErststimmen(pKandidatID int, pWahlkreisID int) returns void strict as $$
declare
  anzahl int;
begin
  select anzahlstimmen
    into anzahl
    from summeerststimmen
      where kandidatid = pKandidatID
      and wahlkreisid = pWahlkreisID;
  with eintrag as (select pKandidatID as kandidatid, pWahlkreisID as wahlkreisid, generate_series(1, anzahl))
  insert into erststimme (kandidatid,wahlkreisid)
    select eintrag.kandidatid,eintrag.wahlkreisid from eintrag;
 end;
$$ language plpgsql;

create or replace function erzeugeErststimmen(pWahlkreisID int) returns void as $$
declare
  stimmsumme record;
begin
  alter table erststimme
    drop constraint erststimme_kandidatid_fkey,
    drop constraint erststimme_wahlkreisid_fkey;
  for stimmsumme in select kandidatid from summeerststimmen where wahlkreisid = pWahlkreisID loop
    perform erzeugeErststimmen(stimmsumme.kandidatid, pWahlkreisID);
  end loop;
  alter table erststimme
    add constraint erststimme_kandidatid_fkey foreign key (kandidatid) references Kandidat (KandidatID),
    add constraint erststimme_wahlkreisid_fkey foreign key (wahlkreisid) references Wahlkreis (WahlkreisID);
end;
$$ language plpgsql;

create or replace function erzeugeErststimmen() returns void as $$
declare
  stimmsumme record;
begin
  alter table erststimme
    drop constraint erststimme_kandidatid_fkey,
    drop constraint erststimme_wahlkreisid_fkey;
  for stimmsumme in select kandidatid, wahlkreisid from summeerststimmen loop
    perform erzeugeErststimmen(stimmsumme.kandidatid, stimmsumme.wahlkreisid);
  end loop;
  alter table erststimme
    add constraint erststimme_kandidatid_fkey foreign key (kandidatid) references Kandidat (KandidatID),
    add constraint erststimme_wahlkreisid_fkey foreign key (wahlkreisid) references Wahlkreis (WahlkreisID);
end;
$$ language plpgsql;

create or replace function erzeugeZweitstimmen(pParteiID int, pWahlkreisID int) returns void strict as $$
declare
  anzahl int;
begin
  select anzahlstimmen
    into anzahl
    from summezweitstimmen
      where parteiid = pParteiID
      and wahlkreisid = pWahlkreisID;
  with numbers as (select generate_series(1, anzahl)),
       eintrag as (select pParteiID as parteiid, pWahlkreisID as wahlkreisid)
    insert into zweitstimme (parteiid,wahlkreisid)
      select eintrag.* from eintrag,numbers;
 end;
$$ language plpgsql;

create or replace function erzeugeZweitstimmen(pWahlkreisID int) returns void as $$
declare
  stimmsumme record;
begin
  alter table zweitstimme
    drop constraint zweitstimme_parteiid_fkey,
    drop constraint zweitstimme_wahlkreisid_fkey;
  drop index zweitstimme_parteiid_idx;
  drop index zweitstimme_wahlkreisid_idx;
  for stimmsumme in select parteiid from summezweitstimmen where wahlkreisid = pWahlkreisID loop
    perform erzeugeZweitstimmen(stimmsumme.parteiid, pWahlkreisID);
  end loop;
  alter table zweitstimme
    add constraint zweitstimme_parteiid_fkey foreign key (parteiid) references Partei (ParteiID),
    add constraint zweitstimme_wahlkreisid_fkey foreign key (wahlkreisid) references Wahlkreis (WahlkreisID);
  create index zweitstimme_parteiid_idx on zweitstimme using hash (parteiid);
  create index zweitstimme_wahlkreisid_idx on zweitstimme using hash (wahlkreisid);
end;
$$ language plpgsql;

create or replace function erzeugeZweitstimmen() returns void as $$
declare
  stimmsumme record;
begin
  alter table zweitstimme
    drop constraint zweitstimme_parteiid_fkey,
    drop constraint zweitstimme_wahlkreisid_fkey;
  for stimmsumme in select parteiid,wahlkreisid from summezweitstimmen loop
    perform erzeugeZweitstimmen(stimmsumme.parteiid, stimmsumme.wahlkreisid);
  end loop;
  alter table zweitstimme
    add constraint zweitstimme_parteiid_fkey foreign key (parteiid) references Partei (ParteiID),
    add constraint zweitstimme_wahlkreisid_fkey foreign key (wahlkreisid) references Wahlkreis (WahlkreisID);
end;
$$ language plpgsql;

