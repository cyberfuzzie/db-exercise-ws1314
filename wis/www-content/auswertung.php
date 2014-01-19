<?php
require_once 'lib/db.php';
require_once 'lib/chart.php';

$db = new Database();

$resultAufteilung = $db->getBundestagAufteilung()->fetchAll();
?>
<h1>Zusammensetzung des Bundestags</h1>
<script type="text/javascript" src="js/Chart.js"></script>
<canvas id="chart" width="400" height="400"></canvas>
<script type="text/javascript">
    var data = <?php
        print json_encode(Chart::getPieData($resultAufteilung, Chart::$parteiConfig));
    ?>;
    var options = {
        animation : false
    };
    var context = document.getElementById("chart").getContext("2d");
    var chart = new Chart(context).Pie(data, options);
</script>
<br />
<ul>
<?php
foreach ($resultAufteilung as $row) {
    print "<li>" . htmlentities("{$row['partei']}: {$row['sitze']}") . " Sitze</li>\n";
}
?>
</ul>
<br />
<br />
<a href="?page=mitgliederbundestag">Mitglieder des Bundestags auflisten</a>