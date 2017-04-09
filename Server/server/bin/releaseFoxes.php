<?php
include('./config.inc.php');

$conn = mysql_connect("localhost",$dbuser,$dbpass);
mysql_select_db($dbname,$conn);

$latmin = 0;
$latmax = 0;
$lonmin = 0;
$lonmax = 0;

$sql = "select value, property from SimulationProperty;";
$res = mysql_query($sql, $conn);
while($row = mysql_fetch_array($res)) {
	$val = $row['value'];
	$key = $row['property'];

	if ($key == 'FENCE_LATMIN') $latmin = $val;
        if ($key == 'FENCE_LATMAX') $latmax = $val;
        if ($key == 'FENCE_LONMIN') $lonmin = $val;
        if ($key == 'FENCE_LONMAX') $lonmax = $val;

}

echo "Latmin: " . $latmin . "<br />";
echo "Latmax: " . $latmax . "<br />";
echo "Lonmin: " . $lonmin . "<br />";
echo "Lonmax: " . $lonmax . "<br />";

$sql = "select id from Object;";
$res = mysql_query($sql,$conn);

while( $row = mysql_fetch_array($res)) {

  $oid = $row['id'];
  $rellat = rand(0,1000) / 1000;
  $rellon = rand(0,1000) / 1000;
  $deltalat = $latmax - $latmin;
  $deltalon = $lonmax - $lonmin;

  $newlat = $deltalat * $rellat + $latmin;
  $newlon = $deltalon * $rellon + $lonmin;

  echo "<hr>";
  echo "New latitude: " . $newlat . "<br/>";
  echo "New longitude: " . $newlon . "<br/>";

  $sql = "update Object set lat = $newlat, lon = $newlon where id = $oid;";

  mysql_query($sql, $conn);
  echo $sql;
}

$sql = "delete from Found;";
mysql_query($sql, $conn);

mysql_close($conn);


  
