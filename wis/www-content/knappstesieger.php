<?php
require_once 'lib/db.php';

$db = new Database();

?>
<h1>Knappste Wahlkreise</h1>

<?php
$parteiName = "";
foreach ($db->getKnappsteWahlkreise() as $ergebnis) {
    if ($parteiName != $ergebnis['partei']) {
        if ($parteiName != "") {
            echo "</table>\n";
        }
        $parteiName = $ergebnis['partei'];
        if ($ergebnis['abstand'] > 0) {
            echo "<br /><br /><h3>Knappste von {$ergebnis['partei']} gewonnene Wahlkreise</h3>\n";
            echo "<table><tr><th>Wahlkreis</th><th>Kandidat</th><th>Vorsprung (#Stimmen)</th></tr>\n";
        } else {
            echo "<br /><br /><h3>Knappste von {$ergebnis['partei']} verlorene Wahlkreise</h3>\n";
            echo "<table><tr><th>Wahlkreis</th><th>Kandidat</th><th>R&uuml;ckstand (#Stimmen)</th></tr>\n";
        }
    }
    print "<tr><td>" . htmlentities("{$ergebnis['wahlkreisnummer']}: {$ergebnis['wahlkreis']}") . "</td>";
    print "<td>" . htmlentities($ergebnis['kandidat']) . "</td>";
    print "<td>" . abs($ergebnis['abstand']) . "</td></tr>\n";
}
echo "</table>\n";
