<?php
require_once 'lib/db.php';

$db = new Database();
?>
<h1>Siegerparteien in den Wahlkreisen</h1>
<table>
    <tr><th>Wahlkreis</th><th>Siegerpartei Erststimme</th><th>Siegerpartei Zweitstimme</th></tr>
<?php
foreach ($db->getWahlkreisSieger() as $row) {
    print "<tr><td>{$row['nummer']}: " . htmlentities($row['wahlkreis']) . "</td>";
    print "<td>" . htmlentities($row['siegererststimmen']) . "</td>";
    print "<td>" . htmlentities($row['siegerzweitstimmen']) . "</td></tr>\n";
}
?>
</table>
