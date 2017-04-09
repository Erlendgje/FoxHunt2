<?php
include('./config.inc.php');

header("Content-type: text/xml");
header( "Cache-Control: no-cache, must-revalidate" );




$xml_output = "<?xml version=\"1.0\"?>\n";

$xml_output .= "\n<game>\n";

$tilkobling = mysql_connect($dbaddr,$dbuser,$dbpass);
mysql_select_db($dbname,$tilkobling);


$sql = "SELECT value FROM SimulationProperty WHERE property='LONMIN';";
$resultat = mysql_query($sql,$tilkobling);
$rad= mysql_fetch_array($resultat);
$lonmin= $rad["value"];

$sql = "SELECT value FROM SimulationProperty WHERE property='LONMAX';";
$resultat = mysql_query($sql,$tilkobling);
$rad= mysql_fetch_array($resultat);
$lonmax= $rad["value"];

$sql = "SELECT value FROM SimulationProperty WHERE property='LATMIN';";
$resultat = mysql_query($sql,$tilkobling);
$rad= mysql_fetch_array($resultat);
$latmin= $rad["value"];

$sql = "SELECT value FROM SimulationProperty WHERE property='LATMAX';";
$resultat = mysql_query($sql,$tilkobling);
$rad= mysql_fetch_array($resultat);
$latmax= $rad["value"];


$xml_output .=  "\t<foxes>\n";


$sql = "SELECT * FROM Object LEFT JOIN ObjectProperty ON Object.id=ObjectProperty.objectId WHERE ObjectProperty.property='ClassName' AND Object.hidden=0";

$resultat = mysql_query($sql,$tilkobling);


while ( $rad = mysql_fetch_array($resultat) ) 
{

 	$sql = "SELECT hunterID FROM Found WHERE objectID='" . $rad["id"] . "';"; 
        $r = mysql_query($sql,$tilkobling);       
        $r2= mysql_fetch_array($r);        
        $found= $r2[0]; 

	if ($found=="")  
	{
		$found="-";
	}


	$xml_output .=  "\t<fox id=\"" . $rad["id"] . "\" class=\"" . $rad["value"] . "\" name=\"" . $rad["name"] . "\" lt=\"" . $rad["lat"] . "\" ln=\"" . $rad["lon"]  . "\" found=\"" . $found .   "\">\n";
	$xml_output .= "\t</fox>\n";

}

$xml_output .=  "\t</foxes>\n";

//End objects


$xml_output .= "\t<hunters>\n";

$sql = "SELECT id,name,lon,lat FROM Hunter";

$resultat = mysql_query($sql,$tilkobling);

while ( $rad = mysql_fetch_array($resultat) ) 
{

	$score = 0;
	
	$sql = "SELECT COUNT(*) FROM Found WHERE hunterID='" . $rad["id"] . "';";
	$r = mysql_query($sql,$tilkobling);
	$r2= mysql_fetch_array($r);
	$score= $r2[0];


	$xml_output .=  "\t<hunter id=\"" .  $rad["id"] . "\" name=\"" . $rad["name"] . "\" lt=\"" . $rad["lat"] . "\" ln=\"" . $rad["lon"] . "\" score=\"" . $score . "\">\n";
	$xml_output .= "\t</hunter>\n";

}



$xml_output .=  "\t</hunters>\n";





//End hunters









$xml_output .= "<game_info>\n";
$xml_output .= "\t<boundry ltmin=\"" . $latmin  . "\" lnmin=\"" . $lonmin . "\" ltmax=\"" . $latmax . "\" lnmax=\"" . $lonmax . "\">\n";
$xml_output .= "\t</boundry>\n";
$xml_output .= "</game_info>\n";


$xml_output .=  "</game>";


echo $xml_output;


if (isset($_GET["userid"]))
{
	$id = $_GET["userid"];
	$lat = $_GET["lat"];
	$lon = $_GET["lon"];

	$sql = "UPDATE Hunter SET lat=" . $lat . ", lon=" . $lon . " where id='" . $id . "';";
	mysql_query($sql, $tilkobling);

//	echo $sql;

//	$sql = "UPDATE Hunter SET lat='". $lat . "' AND id='" . $id . "';";
//	mysql_query($sql,$tilkobling);

//	$sql = "UPDATE Hunter SET lon='". $lon . "' AND id='" . $id . "';";
//	mysql_query($sql,$tilkobling);
}




?>
