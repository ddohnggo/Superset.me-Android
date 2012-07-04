package com.superset.me;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import com.facebook.android.*;
import com.facebook.android.Facebook.*;

public class CheckinActivity extends Activity {
	
	Facebook facebook = new Facebook("440227432655382");
	private SharedPreferences mPrefs;
	protected ProgressDialog dialog;
	
	TextView locationText;
	LocationManager locationManager; //<2>
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);

        // Setup Menu
        ActionBar actionBar = getActionBar();
        actionBar.show();
        
        /*
         * Get existing access_token if any
         */
        mPrefs = getPreferences(MODE_PRIVATE);
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
        if(access_token != null) {
            facebook.setAccessToken(access_token);
        }
        if(expires != 0) {
            facebook.setAccessExpires(expires);
        }
        
        /*
         * Only call authorize if the access_token has expired.
         */
        if(!facebook.isSessionValid()) {

            facebook.authorize(this, new String[] {"user_interests", "friends_interests"}, new DialogListener() {
                @Override
                public void onComplete(Bundle values) {
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("access_token", facebook.getAccessToken());
                    editor.putLong("access_expires", facebook.getAccessExpires());
                    editor.commit();
                }
    
                @Override
                public void onFacebookError(FacebookError error) {}
    
                @Override
                public void onError(DialogError e) {}
    
                @Override
                public void onCancel() {}
            });
        }
        
        // Get user location here
        
        locationText = (TextView)this.findViewById(R.id.lblLocationInfo);
        locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE); //<2>
        
    }
    
  //Start a location listener
  LocationListener onLocationChange=new LocationListener() {
        public void onLocationChanged(Location loc) {
            //sets and displays the lat/long when a location is provided
            String latlong = "Lat: " + loc.getLatitude() + " Long: " + loc.getLongitude();   
            locationText.setText(latlong);
            
            // Query Facebook API for nearby places
            double lat = loc.getLatitude();
            double lon = loc.getLongitude();
            
            Bundle params = new Bundle();
            params.putString("method", "fql.query");
            //Build Query String
            params.putString("query", "SELECT page_id, name, description, latitude, longitude, checkin_count, distance(latitude, longitude, '" + lat + "', '" + lon
                    + "') FROM place WHERE distance(latitude, longitude, '" + lat + "', '" + lon + "') < "+ 1000);
            //locationText.setText(params.toString());
            
            // Get Facebook locations and print to screen
            ListView listView = (ListView) findViewById(R.id.placeList);
            String[] values = new String[] { "Place 1", "Place 2", "Place 3",
            	"Place 4", "Place 5", "Place 6", "Place 7", "Place 8",
            	"Place 9", "Place 10" };
            
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(CheckinActivity.this, android.R.layout.simple_list_item_1, values);
            listView.setAdapter(adapter);
            /**
            try {
				JSONObject response = Util.parseJson(facebook.request("me/friends"));
				JSONArray jArray = response.getJSONArray("data");
				
				JSONObject json_data = jArray.getJSONObject(0);
		        String name = json_data.getString("name");
		        
		        locationText.setText(name);

			} catch (FacebookError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}**/
            
            /**
            String response = null;
            
            try {
				response = facebook.request(params);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}**/
			
            //locationText.setText(response.toString());
			
        }
         
        public void onProviderDisabled(String provider) {
        // required for interface, not used
        }
         
        public void onProviderEnabled(String provider) {
        // required for interface, not used
        }
         
        public void onStatusChanged(String provider, int status,
        Bundle extras) {
        // required for interface, not used
        }
   };
    
   //pauses listener while app is inactive
   @Override
   public void onPause() {
       super.onPause();
       locationManager.removeUpdates(onLocationChange);
   }
   
   //reactivates listener when app is resumed
   @Override
   public void onResume() {
       super.onResume();
       locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,10000.0f,onLocationChange);
   }
   
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebook.authorizeCallback(requestCode, resultCode, data);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_checkin, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_feed:	Intent intentItem1 = new Intent(this, FeedActivity.class);
									startActivityForResult(intentItem1, 0);
									return true;
            case R.id.item_friends:	Intent intentItem2 = new Intent(this, FeedActivity.class);
									startActivityForResult(intentItem2, 0);
									return true;
        }
        return true;
    }
        
}
