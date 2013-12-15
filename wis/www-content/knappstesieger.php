<?php
require_once 'lib/db.php';

$db = new Database();

?>
<h1>Knappste Sieger</h1>

<p>Partei</p>
<ul>
<?php
$parteiName = "";
foreach ($db->getParteiListe() as $partei) {
    if (isset($_GET['p']) && $_GET['p'] == $partei['id']) {
        $parteiName = $partei['name'];
    }
    print "<li><a href=\"?page={$thisPage}&p={$partei['id']}\">" . htmlentities("{$partei['name']}") . "</a></li>\n";
}
?>
</ul>
<?php
if (isset($_GET['p'])) {
    $pid = $_GET['p'];
    $result = $db->getParteiKnappsteSieger($pid);
    if ($result->rowCount() > 0) {
?>
<br />
<br />
<h3>Knappste von <?php print $parteiName ?> gewonnene Wahlkreise</h3>
<table>
    <tr><th>Wahlkreis</th><th>Kandidat</th><th>Vorsprung (#Stimmen)</th></tr>
<?php
foreach ($result as $row) {
    print "<tr><td>" . htmlentities("{$row['nummer']}: {$row['wahlkreis']}") . "</td>";
    print "<td>" . htmlentities($row['kandidat']) . "</td>";
    print "<td>{$row['vorsprung']}</td></tr>\n";
}
?>
</table>
<?php
    } else {
?>
<br />
<br />
<h3>Knappste von <?php print $parteiName ?> verlorene Wahlkreise</h3>
<table>
    <tr><th>Wahlkreis</th><th>R&uuml;ckstand (#Stimmen)</th></tr>
<?php
$result = $db->getParteiKnappsteVerloreneWahlkreise($pid);
foreach ($result as $row) {
    print "<tr><td>" . htmlentities("{$row['nummer']}: {$row['wahlkreis']}") . "</td>";
    print "<td>{$row['differenz']}</td></tr>\n";
}
?>
</table>
<?php
    }
}
