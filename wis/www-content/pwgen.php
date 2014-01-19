<?php
require_once 'lib/db.php';

$db = new Database();

?>
<h1>OTP-Generator</h1>

<?php
if (isset($_POST['anzahl'])) {
    $anzahl = $_POST['anzahl'];
    $result = $db->erzeugePasswoerter($anzahl);
    if ($result->errorInfo()[0] == '00000') {
        print "<h3>Passw&ouml;rter</h3>\n";
        print "<ul>\n";
        foreach ($result as $pw) {
            print "<li>{$pw[0]}</li>\n";
        }
        print "</ul>\n";
    } else {
        print "Fehler bei der Erzeugung der Passw&ouml;rter: " . $result->errorInfo()[2];
    }
} else {
?>
<form method="POST">
    <label for="anzahl">Anzahl Passw&ouml;rter</label>
    <input type="text" name="anzahl" size="10" /><br />
    <input type="submit" value="Passw&o&ouml;rter generieren" />
</form>
<?php
}
?>
