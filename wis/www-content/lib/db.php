<?php

class Database {
    private $db;
    function __construct() {
        try {
            $this->db = new PDO('pgsql:host=localhost;dbname=wis', 'wis', 'wis');
        } catch (PDOException $ex) {
            print 'Error connecting to database';
        }
    }
    
    function executeSQL($sql, $params) {
        $stmt = $this->db->prepare($sql);
        $stmt->execute($params);
        return $stmt;
    }
    
    function getBundestagAufteilung() {
        return $this->db->query('SELECT sitze,partei FROM Output_SitzeParteienBundesweit ORDER BY partei');
    }
    
    function getBundestagMitglieder() {
        return $this->db->query('SELECT kandidat,partei FROM Output_MdBs ORDER BY partei,kandidat');
    }
    
    function getBundeslandListe() {
        return $this->db->query('SELECT bl.bundeslandid id, bl.name "name" FROM bundesland bl ORDER BY bl.name ASC');
    }
    
    function getUeberhangmandate() {
        return $this->db->query('SELECT bundesland,partei,anzahlueberhangmandate ueberhangmandate FROM Output_Ueberhangmandate WHERE anzahlueberhangmandate > 0 ORDER BY bundesland ASC');
    }

    function getWahlkreisListe($blid) {
        return $this->executeSQL('SELECT wahlkreis.wahlkreisid id,wahlkreis.wahlkreisnummer nummer,wahlkreis.name "name" FROM wahlkreis WHERE wahlkreis.bundeslandid = :blid AND wahlkreis.jahr = 2013 ORDER BY wahlkreis.wahlkreisnummer',
                                 array(':blid' => $blid));
    }
    
    function getWahlkreisListeEinzelstimmen() {
        return $this->db->query("SELECT DISTINCT szl.wahlkreisid id,wk.wahlkreisnummer nummer,wk.name wahlkreis,wk.jahr FROM summezweitstimmenlive szl INNER JOIN wahlkreis wk ON szl.wahlkreisid = wk.wahlkreisid ORDER BY wk.wahlkreisnummer ASC, wk.jahr DESC");
    }
    
    function getWahlkreisInfo($wkid) {
        return $this->executeSQL('SELECT wahlkreisnummer nummer,wahlkreis "name",wahlbeteiligung FROM Output_Wahlbeteiligung WHERE wahlkreisid = :wkid',
                                 array(':wkid' => $wkid));
    }
    
    function getWahlkreisInfoEinzelstimmen($wkid) {
        return $this->executeSQL('SELECT wahlkreisnummer nummer,wahlkreis "name",wahlbeteiligung FROM Output_WahlbeteiligungEinzelstimmen WHERE wahlkreisid = :wkid',
                                 array(':wkid' => $wkid));
    }
    
    function getWahlkreisDirektmandat($wkid) {
        return $this->executeSQL('SELECT k.name kandidat,p.name partei FROM DirektMandate dk INNER JOIN Kandidat k ON dk.kandidatid = k.kandidatid INNER JOIN Partei p on k.parteiid = p.parteiid WHERE dk.wahlkreisid = :wkid',
                                 array(':wkid' => $wkid));
    }
    
    function getWahlkreisDirektmandatEinzelstimmen($wkid) {
        return $this->executeSQL('SELECT k.name kandidat,p.name partei FROM GewaehlterDirektKandidatEinzelstimmen dk INNER JOIN Kandidat k ON dk.kandidatid = k.kandidatid INNER JOIN Partei p on k.parteiid = p.parteiid WHERE dk.wahlkreisid = :wkid',
                                 array(':wkid' => $wkid))->fetch();
    }
    
    function getWahlkreisStimmverteilung($wkid) {
        return $this->executeSQL('SELECT partei,summe,prozent FROM Output_ProzentualeAbsoluteStimmenPartei WHERE wahlkreisid = :wkid ORDER BY summe DESC',
                                 array(':wkid' => $wkid));
    }
    
    function getWahlkreisStimmverteilungEinzelstimmen($wkid) {
        return $this->executeSQL('SELECT partei,summe,prozent FROM Output_ProzentualeAbsoluteStimmenParteiEinzelstimmen WHERE wahlkreisid = :wkid ORDER BY summe DESC',
                                 array(':wkid' => $wkid));
    }
    
    function getWahlkreisEntwicklung($wkid) {
        return $this->executeSQL('SELECT partei,summe13,summe09,differenz FROM Output_EntwicklungStimmen WHERE wahlkreisid = :wkid ORDER BY summe13 DESC',
                                 array(':wkid' => $wkid));
    }
    
    function getWahlkreisEntwicklungEinzelstimmen($wkid) {
        return $this->executeSQL('SELECT partei,summe13,summe09,differenz FROM Output_EntwicklungStimmenEinzelstimmen WHERE wahlkreisid = :wkid ORDER BY summe13 DESC',
                                 array(':wkid' => $wkid));
    }
    
    function getWahlkreisSieger() {
        return $this->db->query("SELECT nummer,wahlkreis,siegererststimmen,siegerzweitstimmen FROM Output_Wahlkreissieger WHERE jahr = 2013 ORDER BY nummer");
    }
    
    function getParteiListe() {
        return $this->db->query('SELECT p.parteiid id, p.name "name" FROM partei p ORDER BY p.name ASC');
    }
    
    function getParteiKnappsteSieger($pid) {
        return $this->executeSQL('SELECT wk.wahlkreisnummer nummer,wk.name wahlkreis,k.name kandidat,ks.vorsprung FROM KnappesteSieger ks INNER JOIN Wahlkreis wk ON ks.wahlkreisid = wk.wahlkreisid INNER JOIN Kandidat k ON ks.kandidatid = k.kandidatid WHERE ks.parteiid = :pid ORDER BY ks.vorsprung ASC',
                                 array(':pid' => $pid));
    }
    
    function getParteiKnappsteVerloreneWahlkreise($pid) {
        return $this->executeSQL('SELECT wk.wahlkreisnummer nummer,wk.name wahlkreis,kv.differenz FROM KnappesteVerloreneWahlkreise kv INNER JOIN Wahlkreis wk ON kv.wahlkreisid = wk.wahlkreisid WHERE kv.parteiid = :pid ORDER BY kv.differenz ASC',
                                 array(':pid' => $pid));
    }
    
    function getKnappsteWahlkreise() {
        return $this->db->query('SELECT partei,kandidat,wahlkreisnummer,wahlkreis,abstand FROM Output_KnappsteWahlkreise ORDER BY partei ASC, ABS(abstand) ASC');
    }
    
    function getKandidatenListe($wkid) {
        return $this->executeSQL('SELECT k.name kandidat,k.kandidatid id,p.name partei FROM kandidat k INNER JOIN partei p ON k.parteiid = p.parteiid INNER JOIN direktkandidat dk ON k.kandidatid = dk.kandidatid WHERE dk.wahlkreisid = :wkid',
                                 array(':wkid' => $wkid));
    }
    
    function getParteienListe($blid) {
        return $this->executeSQL('SELECT DISTINCT p.name partei,p.parteiid id FROM partei p INNER JOIN listenplatz lp ON p.parteiid = lp.parteiid WHERE lp.bundeslandid = :blid',
                                 array(':blid' => $blid));
    }
    
    function erzeugePasswoerter($anzahl) {
        return $this->executeSQL('SELECT erzeugeStimmschluessel(:anzahl)',
                                 array(':anzahl' => $anzahl));
    }
    
    function gibStimmeAb($wkid, $erststimme, $zweitstimme, $passwort) {
        return $this->executeSQL('SELECT gibStimmeAb(:wahlkreis,:erststimme,:zweitstimme,:passwort)',
                                 array(':wahlkreis' => $wkid,
                                       ':erststimme' => $erststimme,
                                       ':zweitstimme' => $zweitstimme,
                                       ':passwort' => $passwort));
    }
}
