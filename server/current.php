<?php
header('Content-type: application/vnd.google-earth.kml+xml');
$glue = "|";

$file = fopen(date("d.m.y").".db", "r") or exit("<name>database inaccessible</name></Folder>");
$str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n <kml xmlns=\"http://www.opengis.net/kml/2.2\">\n\n";

while(!feof($file)) {
    $line = fgets($file);
    if(empty($line)) break;
    $array = explode($glue, $line);
    $str .= "<Placemark>
    <name> ".$array[0]." </name>
    <description>time: ". date("d.m.y H:i:s")." / current speed: ".$array[1] ."</description>
    <Point>
     <coordinates>".$array[2].", ".$array[3]. ", ".$array[4]."</coordinates>
    </Point>
    </Placemark>";
  }
  $str .= "\n</kml>";
  fclose($file);
  
  echo $str;
