<?php
require_once 'lib/db.php';

$db = new Database();

?>
<h1>Wahlkreisauswertung auf Einzelstimmbasis</h1>

<p>Wahlkreis</p>
<ul>
<?php
foreach ($db->getWahlkreisListeEinzelstimmen() as $wk) {
    print "<li><a href=\"?page={$thisPage}&wk={$wk['id']}\">" . htmlentities("{$wk['nummer']}: {$wk['wahlkreis']} ({$wk['jahr']})") . "</a></li>\n";
}
?>
</ul>
<?php
if (isset($_GET['wk'])) {
    $wkid = $_GET['wk'];
    $wkrow = $db->getWahlkreisInfoEinzelstimmen($wkid);
    $wkkandidat = $db->getWahlkreisDirektmandatEinzelstimmen($wkid);
?>
<br />
<br />
<h3>Wahlkreis <?php print "{$wkrow['nummer']}: {$wkrow['name']}" ?></h3>
<p>Wahlbeteiligung: <?php print round($wkrow['wahlbeteiligung'],2) ?>%</p>
<p>Gew&auml;hlter Direktkandidat: <?php print htmlentities("{$wkkandidat['kandidat']} ({$wkkandidat['partei']})") ?></p>
<p>Abgegebene Stimmen pro Partei:</p>
<table>
<tr><th>Partei</th><th>#Stimmen</th><th>%Stimmen</th></tr>
<?php
foreach ($db->getWahlkreisStimmverteilungEinzelstimmen($wkid) as $partei) {
    print "<tr><td>" . htmlentities($partei['partei']) . "</td><td>{$partei['summe']}</td><td>" . round($partei['prozent'],2) . "</td></tr>\n";
}
?>
</table>
<?php
$resultWkEntwicklung = $db->getWahlkreisEntwicklungEinzelstimmen($wkid);
if ($resultWkEntwicklung->rowCount() > 0) {
?>
<br />
<br />
<p>Entwicklung im Vergleich zur Wahl 2009:</p>
<table>
    <tr><th>Partei</th><th>#Stimmen 2013</th><th>#Stimmen 2009</th><th>Differenz</th></tr>
<?php
foreach ($resultWkEntwicklung as $partei) {
    print "<tr><td>" . htmlentities($partei['partei']) . "</td><td>{$partei['summe13']}</td><td>{$partei['summe09']}</td><td>{$partei['differenz']}</td></tr>\n";
}
?>
</table>
<?php
}
}
?>