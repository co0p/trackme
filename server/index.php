<?php
$glue = "|";

// put data from app in today db  
if (isset($_POST["msg"])) {
  $fh = fopen(date("d.m.y").".db", 'a') or die("can't open file");
  
  $msg = " ".$_POST["msg"];
  $speed = $_POST["speed"];
  $altitude = $_POST["altitude"];
  $longitude = $_POST["longitude"];
  $latitude = $_POST["latitude"];
  $time = time();
  $array = array($msg, $speed, $longitude, $latitude, $altitude, $time);
  
  $str = implode($glue, $array);
	fwrite($fh, $str."\n");
	fclose($fh);
	
	
	// write new kml file
  $fh = fopen(date("d.m.y").".kml", 'w') or die("can't open file");
  $str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
      <kml xmlns=\"http://www.opengis.net/kml/2.2\">
      <Document>
      <Style id=\"redPoint\">
      <IconStyle>
        <Icon>
          <href>http://maps.google.com/mapfiles/kml/paddle/red-stars.png</href>
        </Icon>
      </IconStyle>
      </Style>";
      
  $file = fopen(date("d.m.y").".db", "r") or exit("Unable to open file!");
  $oldLongitude = 0;
  $oldLatitude = 0;
  
  // on every line, there is a placemark
  while(!feof($file)) {
    $line = fgets($file);
    if (empty($line)) break;
    
    $array = explode($glue, $line);
    $str .= "<Placemark>\n";
    
    // make points with messages red
    $tmpStr = trim($array[0]);
    if (!empty($tmpStr)) {
      $str .= "<styleUrl>#redPoint</styleUrl>\n";
    }
    $str .="<name> ".$array[0]." </name>
    <description>time: ". date("d.m.y H:i:s", $array[5])." \ncurrent speed: ".$array[1] ."</description>
    <Point>
     <coordinates>".$array[2].", ".$array[3]. ", ".$array[4]."</coordinates>
    </Point>
    </Placemark>";
    
    // add lines between old and this point
    if ($oldLongitude != $oldLatitude) {
      $str .= "<Placemark>
        <LineString>
        <extrude>1</extrude>
      <tessellate>1</tessellate>
      <coordinates>
        ".$oldLongitude.",".$oldLatitude.",0 ".$array[2].",".$array[3].",0 
      </coordinates>
      </LineString>
      </Placemark>";
    }
    
    // update 
    $oldLongitude = $array[2];
    $oldLatitude = $array[3];
    
  }
  $str .= "\n</Document></kml>";
  fclose($file);
  
  fwrite($fh, $str);
  fclose($fh);
}

?>

<?php
$url = "http://maps.google.de/maps?f=q&hl=de&q=http://www.co0p.org/trackme/";

foreach (glob("*.kml") as $filename) {
    echo "<a href=".$url.$filename."?rand=".time().">".$filename."</a><br>";
}
?>




