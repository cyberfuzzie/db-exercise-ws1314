<?php
require_once 'lib/db.php';

$db = new Database();

?>
<h1>Mitglieder des Bundestags</h1>
<ul>
<?php

foreach ($db->getBundestagMitglieder() as $row) {
    print "<li>" . htmlentities("{$row['kandidat']} ({$row['partei']})") . "</li>\n";
}
?>
</ul>
<br />
<br />
<a href="?page=auswertung">Zur&uuml;ck zur &Uuml;bersicht</a>