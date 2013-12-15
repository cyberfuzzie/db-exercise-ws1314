<?php
require_once 'lib/db.php';

$db = new Database();

?>
<h1>Liste der &Uuml;berhangmandate</h1>
<table>
    <tr><th>Bundesland</th><th>Partei</th><th>Anzahl &Uuml;berhangmandate</th></tr>
<?php
foreach ($db->getUeberhangmandate() as $row) {
    print "<tr><td>" . htmlentities($row['bundesland']) . "</td>";
    print "<td>" . htmlentities($row['partei']) . "</td>";
    print "<td>{$row['ueberhangmandate']}</td></tr>\n";
}
?>
</table>