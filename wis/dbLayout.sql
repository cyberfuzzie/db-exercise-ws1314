create table Bundesland (
    BundeslandID serial primary key,
    Name varchar(30) not null
);

create table Einwohnerzahl (
    EinwohnerzahlID serial primary key,
    BundeslandID integer references Bundesland (BundeslandID),
    Anzahl integer not null,
    Jahr integer not null
);

create table Wahlkreis (
    WahlkreisID serial primary key,
    BundeslandID integer references Bundesland (BundeslandID),
    Wahlkreisnummer integer not null,
    Name varchar(100) not null,
    Jahr integer not null,
    Wahlberechtigte integer not null
);
-- wahlkreis_jahr_idx wird nicht verwendet (werte für jahr zu häufig => seq scan)
create index wahlkreis_jahr_idx on wahlkreis using hash (jahr);
create index wahlkreis_bundeslandid_idx on wahlkreis using hash (bundeslandid);

create table WkNachfolger (
    WkNachfolgerID integer primary key references Wahlkreis (WahlkreisID),
    WkVorgaengerID integer references Wahlkreis (WahlkreisID)
);

create table Partei (
    ParteiID serial primary key,
    Name varchar(50) not null
);

create table Kandidat (
    KandidatID serial primary key,
    ParteiID integer references Partei (ParteiID),
    Name varchar(50) not null
);
create index kandidat_parteiid_idx on kandidat using hash (parteiid);

create table Direktkandidat (
    DirektkandidatID serial primary key,
    KandidatID integer references Kandidat (KandidatID),
    WahlkreisID integer references Wahlkreis (WahlkreisID)
);
create index direktkandidat_kandidatid_idx on direktkandidat using hash (kandidatid);
create index direktkandidat_wahlkreisid_idx on direktkandidat using hash (wahlkreisid);

create table Listenplatz (
    ListenplatzID serial primary key,
    KandidatID integer references Kandidat (KandidatID),
    ParteiID integer references Partei (ParteiID),
    BundeslandID integer references Bundesland (BundeslandID),
    Nummer integer not null
);
create index listenplatz_kandidatid_idx on listenplatz using hash (kandidatid);
create index listenplatz_parteiid_idx on listenplatz using hash (parteiid);
create index listenplatz_bundeslandid_idx on listenplatz using hash (bundeslandid);
create index listenplatz_nummer_idx on listenplatz using btree (nummer);

create table Erststimme (
    ErststimmeID serial primary key,
    KandidatID integer constraint erststimme_kandidatid_fkey references Kandidat (KandidatID),
    WahlkreisID integer constraint erststimme_wahlkreisid_fkey references Wahlkreis (WahlkreisID)
);
create index erststimme_kandidatid_idx on erststimme using hash (kandidatid);
create index erststimme_wahlkreisid_idx on erststimme using hash (wahlkreisid);

create table Zweitstimme (
    ZweitstimmeID serial primary key,
    ParteiID integer references Partei (ParteiID),
    WahlkreisID integer references Wahlkreis (WahlkreisID)
);
create index zweitstimme_parteiid_idx on zweitstimme using hash (parteiid);
create index zweitstimme_wahlkreisid_idx on zweitstimme using hash (wahlkreisid);

create table SummeErststimmen (
    KandidatID integer references Kandidat (KandidatID),
    WahlkreisID integer references Wahlkreis (WahlkreisID),
    AnzahlStimmen integer not null,
    primary key (KandidatID,WahlkreisID)
);
create index summeerststimmen_kandidatid_idx on summeerststimmen using hash (kandidatid);
create index summeerststimmen_wahlkreisid_idx on summeerststimmen using hash (wahlkreisid);
create index summeerststimmen_anzahlstimmen_idx on summeerststimmen using btree (anzahlstimmen);

create table SummeZweitstimmen (
    ParteiID integer references Partei (ParteiID),
    WahlkreisID integer references Wahlkreis (WahlkreisID),
    AnzahlStimmen integer not null,
    primary key (ParteiID,WahlkreisID)
);
create index summezweitstimmen_parteiid_idx on summezweitstimmen using hash (parteiid);
create index summezweitstimmen_wahlkreisid_idx on summezweitstimmen using hash (wahlkreisid);

create table Stimmschluessel (
    Schluessel varchar(32) primary key
);
