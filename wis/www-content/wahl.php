<?php
require_once 'lib/db.php';

$db = new Database();

?>
<h1>Stimmabgabe</h1>

<?php
if (isset($_POST['erststimme']) && isset($_POST['zweitstimme']) && isset($_POST['passwort'])) {
    $wkid = $_GET['wk'];
    $erststimme = $_POST['erststimme'];
    $zweitstimme = $_POST['zweitstimme'];
    $passwort = $_POST['passwort'];
    $result = $db->gibStimmeAb($wkid, $erststimme, $zweitstimme, $passwort);
    if ($result->errorInfo()[0] == '00000') {
        print "Stimme erfolgreich abgegeben";
    } else {
        print "Fehler bei der Stimmabgabe: " . $result->errorInfo()[2];
    }
} else if (isset($_GET['wk'])) {
    $wkid = $_GET['wk'];
    $blid = $_GET['bl'];
    print "<li><a href=\"?page={$thisPage}&bl={$blid}\">zur&uuml;ck zu Wahlkreisen</a></li>\n";
    print "<form action=\"\" method=\"POST\">"
?>
<table>
    <tr><th><h3>Erststimme</h3></th><th><h3>Zweitstimme</h3></th></tr>
<tr><td>
<ul>
<?php
foreach ($db->getKandidatenListe($wkid) as $kandidat) {
    print "<li><input name=\"erststimme\" type=\"radio\" value=\"{$kandidat['id']}\" />{$kandidat['kandidat']} ({$kandidat['partei']})</li>\n";
}
?>
</ul>
</td><td>
<ul>
<?php
foreach ($db->getParteienListe($blid) as $partei) {
    print "<li><input name=\"zweitstimme\" type=\"radio\" value=\"{$partei['id']}\" />{$partei['partei']}</li>\n";
}
?>
</ul>
</td>
</tr>
</table>
<label for="passwort">Passwort: </label><input type="text" name="passwort" size="32" /><br>
<input type="submit" value="Stimme abgeben" />
</form>
<?php
} else if (isset($_GET['bl'])) {
    $blid = $_GET['bl'];
?>
<p>Wahlkreis</p>
<ul>
<?php
print "<li><a href=\"?page={$thisPage}\">zur&uuml;ck zu Bundesl&auml;ndern</a></li>\n";
foreach ($db->getWahlkreisListe($blid) as $wk) {
    print "<li><a href=\"?page={$thisPage}&bl={$blid}&wk={$wk['id']}\">" . htmlentities("{$wk['nummer']}: {$wk['name']}") . "</a></li>\n";
}
?>
</ul>
<?php
} else {
?>
<p>Bundesland</p>
<ul>
<?php
foreach ($db->getBundeslandListe() as $bl) {
    print "<li><a href=\"?page={$thisPage}&bl={$bl['id']}\">" . htmlentities("{$bl['name']}") . "</a></li>\n";
}
?>
</ul>
<?php
}
?>