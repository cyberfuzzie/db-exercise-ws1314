<?php

// elements of array: <naviName> => (<naviTitle>, <contentFile>, <visibleInNavi>)
$items = array(
               'auswertung' => array('Auswertung', 'auswertung.php', true),
               'mitgliederbundestag' => array('Mitglieder des Bundestags', 'mitgliederbundestag.php', false),
               'wahlkreis' => array('Wahlkreisauswertung', 'wahlkreis.php', true),
//               'stimmverteilung' => array('Stimmverteilung', 'stimmverteilung.php', true),
//               'vergleich' => array('Vergleich', 'vergleich.php', true),
               'zusatzstatistiken' => array('Zusatzstatistiken', 'zusatzstatistiken.php', true),
               'wahlkreissieger' => array('Wahlkreissieger', 'wahlkreissieger.php', false),
               'ueberhangmandate' => array('Überhangmandate', 'ueberhangmandate.php', false),
               'knappstesieger' => array('Knappste Sieger', 'knappstesieger.php', false),
               'einzelstimmberechnung' => array('Auswertung auf Einzelstimmbasis', 'einzelstimmen.php', true),
               'wahl' => array('Stimmabgabe', 'wahl.php', true),
               'pwgen' => array('OTP-Generator', 'pwgen.php', true)
         );

$contentfile = "default.php";
$thisPage = "index";
?>
<div id="navi">
    <ul>
<?php
foreach ($items as $key => $content) {
    $class = "";
    if ($_GET['page'] == $key) {
        $class = "active";
        $contentfile = $content[1];
        $thisPage = $key;
    }
    if ($content[2]) {
    print "<li class=\"{$class}\"><a href=\"?page=$key\">" . htmlentities($content[0]) . "</a></li>\n";
    }
}
?>
    </ul>
</div>