package org.co0p.trackme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;



public class TrackMe extends Activity implements OnClickListener, LocationListener{
	private LocationManager mgr;
	private Handler myHandler;
	private int updateIntervalMSecs = 20000;
	private Location location;
	private boolean locationChanged = false;
	
	Runnable runnable;
	HttpClient client = new DefaultHttpClient();
	String publishURL = ""; // your server here
	HttpPost post = new HttpPost(publishURL);
	
	double altitude = -1;
	double speed = -1;
	double longitude = -1;
	double latitude = -1;
	
    // set inital values
    String lastMsg = "";
    boolean msgChanged = false;
    boolean gpsConnected = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // start location manager / gps !!	
        mgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateIntervalMSecs, 2, this);
    	Criteria crit = new Criteria();
    	crit.setSpeedRequired(true);
    	crit.setAltitudeRequired(true);
    	String best = mgr.getBestProvider(crit, true);
    	location = mgr.getLastKnownLocation(best);
    	
        // register buttons handle
        View button = findViewById(R.id.status_button);
        button.setOnClickListener(this);
        View ebutton = findViewById(R.id.exit_button);
        ebutton.setOnClickListener(this);
        
        myHandler = new Handler();
    	runnable = new Runnable() {
      	   @Override
      	   public void run() {
      		   setGpsStatus();
      		   sendRequest();
      		   myHandler.postDelayed(this, updateIntervalMSecs);
      	   }
      	};
      	myHandler.postDelayed(runnable, updateIntervalMSecs);
    }
    
    
    
    /*
     * handle button click
     */
    public void onClick(View v) {
    	switch (v.getId()) {
    	case R.id.status_button: 
	    	// get input text
	    	EditText inputView = (EditText) findViewById(R.id.inputText);
	    	lastMsg = inputView.getText().toString();
	    	msgChanged = true;
	    	
	    	// inform user
	    	setStatusMsg("New message saved for next update.");
	    	break;
	    	
    	case R.id.exit_button:
    		myHandler.removeCallbacks(runnable);
    		setStatusMsg("stopping program.");
    		myHandler = null;
    		super.finish();
    		finish();
    		System.exit(0);
    	}
	}
    
    private void sendRequest() {
    	Log.d("TRACKME", "sendRequest");
    	
    	if (!gpsConnected || (speed == -1 && altitude == -1 && latitude == -1 && longitude == -1)) {
    		setStatusMsg("no gps connection.");
    		return;
    	}
    	
    	if(locationChanged || msgChanged) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
	    	pairs.add(new BasicNameValuePair("speed", Double.toString(speed)));
	    	pairs.add(new BasicNameValuePair("altitude", Double.toString(altitude)));
	    	pairs.add(new BasicNameValuePair("longitude", Double.toString(longitude)));
	    	pairs.add(new BasicNameValuePair("latitude", Double.toString(latitude)));
			pairs.add(new BasicNameValuePair("msg", lastMsg));
	    	try {
	    		post.setEntity(new UrlEncodedFormEntity(pairs));
	    		client.execute(post);
	    		
	    		lastMsg = ""; // reset to empty string
	        	pairs = null;
	        	locationChanged = false;
	        	msgChanged = false;
	        	setStatusMsg("Update send.");
	        	
	        	return;
	    	} catch(IOException ioe) {
	        	setStatusMsg("Failed sending data.");    		
	    	}
    	}
    	setStatusMsg("Nothing new to send.");
	}
    
    
    
    private void setStatusMsg(String msg) {
    	
    	Calendar now = Calendar.getInstance();
    	int hh = now.get(Calendar.HOUR_OF_DAY);
    	int mm = now.get(Calendar.MINUTE);
    	int ss = now.get(Calendar.SECOND);
    	
    	// put new msg above
    	TextView status = (TextView) findViewById(R.id.statusText);
    	status.setText(hh+ ":" +mm+ ":" +ss+ " " +msg);
    }
    
    
    
    private void setGpsStatus() {
    	Log.d("TRACKME", "setGpsStatus");
    	TextView text = (TextView) findViewById(R.id.value_altitude);
    	text.setText(Double.toString(altitude));
    	
    	text = (TextView) findViewById(R.id.value_speed);
    	text.setText(Double.toString(speed));
    	
    	text = (TextView) findViewById(R.id.value_longitude);
    	text.setText(Double.toString(longitude));
    	
    	text = (TextView) findViewById(R.id.value_latitude);
    	text.setText(Double.toString(latitude));
    	
    		text = (TextView) findViewById(R.id.status_gps);
    		if (gpsConnected) {
	    		Bundle b = location.getExtras();
	    		text.setText("connected (" +b.getInt("satellites")+ " satellites)");
    		} else {
    			text.setText("not connected (0 satellites)");
    		}
    }
    
    
    private void updateLocation(Location newLocation) {
    	Log.d("TRACKME", "update Location called");
    	
    	if (location == null || newLocation == null) {
    		setStatusMsg("Location Provider is disabled ?");
    		locationChanged = false;
    		gpsConnected = false;
    		return;
    	}

    	if (altitude == newLocation.getAltitude() &&  
    			speed == newLocation.getSpeed() &&
    			longitude == newLocation.getLongitude() &&
    			latitude == newLocation.getLatitude()) {
    		
    		setStatusMsg("Location did not change.");
    		locationChanged = false;    		
    		
    	} else {
    		location = newLocation;
    		altitude = location.getAltitude();	
        	speed = location.getSpeed();
        	longitude = location.getLongitude();
        	latitude = location.getLatitude();
    		
    		setStatusMsg("Location changed.");    		    
    		locationChanged = true;
    	}
    }
    
    
    
    /* -----------------------------------------------------
     * gps handler 
     ----------------------------------------------------- */
    public void onProviderDisabled(String provider) {
    	gpsConnected = false;
    	setStatusMsg("Location Provider disabled.");
	}
    public void onProviderEnabled(String provider) {
    	setStatusMsg("Location Provider enabled.");
	}
    public void onStatusChanged(String provider, int status, Bundle extras) {
    	Log.d("TRACKME", "onStatusChanged");
	}
    public void onLocationChanged(Location theLocation) {
    	gpsConnected = true;
    	Log.d("TRACKME", "onLocationChanged");
    	updateLocation(theLocation);
    	setGpsStatus();
	}
}