package com.superset.me;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.parse.Parse;
import com.parse.ParseObject;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import com.facebook.android.*;
import com.facebook.android.Facebook.*;

public class CheckinActivity extends Activity {
	
	// Global Variables
	private String name;
	private String id;
	private ParseObject parseLogin;
	private Handler mHandler;
	private Place[] nearbyPlaces;
	
	// Facebook Initialization
	Facebook facebook = new Facebook("440227432655382");
	AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(facebook);
	private SharedPreferences mPrefs;
	protected ProgressDialog dialog;
	
	// UI Variables
	ListView listView;
	LocationManager locationManager; //<2>
	protected static JSONArray jsonArray;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);
        
        Parse.initialize(this, "BMhCXvRBibv30AHIzmorrqWa2xyaiWzhoMENuLw9", "O5YWKO78cE7iNqkpXHODHMDIs6WhmKQ5ZBBmriWo");
        mHandler = new Handler();
        
        // Setup Menu
        ActionBar actionBar = getActionBar();
        actionBar.show();
        
        // Initialize Parse Login 
        parseLogin = new ParseObject("Login");
        
        // Get existing Access Token if necessary
        mPrefs = getPreferences(MODE_PRIVATE);
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
        if(access_token != null) {
            facebook.setAccessToken(access_token);
        }
        if(expires != 0) {
            facebook.setAccessExpires(expires);
        }
        
        
        //Only call authorize if the access_token has expired.
        if(!facebook.isSessionValid()) {
        	Log.i("Session is Not Valid","Session is Not Valid");
            facebook.authorize(this, new String[] {"email", "user_interests", "friends_interests"}, new DialogListener() {
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
        
        
        // Initialize UI and system variables
        locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE); //<2>
        
        // Request User Information
        asyncRunner.request("me", new UserRequestListener());

    }

  //Start a location listener
  LocationListener onLocationChange=new LocationListener() {
        public void onLocationChanged(Location loc) {
            // Query Facebook API for nearby places
            double lat = loc.getLatitude();
            double lon = loc.getLongitude();
            
            fetchPlaces(lat, lon);
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
        
        public void fetchPlaces(double lat, double lon){
     	   Bundle params = new Bundle();
     	   params.putString("q", "Equinox");
           params.putString("type", "place");
           params.putString("center", lat + "," + lon);
           params.putString("distance", "10000");
            
            asyncRunner.request("search", params, new PlacesRequestListener());
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
            case R.id.item_feed:	break;
            case R.id.item_friends:	break;
        }
        return true;
    }
    
    
    /** 
     * @author mrood
     *
     * Request data from Facebook
     */
    public class UserRequestListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
            try {
                // process the response here: executed in background thread
                Log.d("Facebook-Example", "Response: " + response.toString());
                JSONObject json = Util.parseJson(response);
                name = json.getString("name");
                id = json.getString("id");

                // then post the processed result back to the UI thread
                // if we do not do this, an runtime exception will be generated
                // e.g. "CalledFromWrongThreadException: Only the original
                // thread that created a view hierarchy can touch its views."
                CheckinActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                    	// Log user into Login table
                    	parseLogin.put("userid", id);
                    	parseLogin.saveInBackground();
                    }
                });
            } catch (JSONException e) {
                Log.w("Facebook-Example", "JSON Error in response");
            } catch (FacebookError e) {
                Log.w("Facebook-Example", "Facebook Error: " + e.getMessage());
            }
        }
    }
    
    /** 
     * @author mrood
     *
     * Request data from Facebook
     */
    public class PlacesRequestListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
        	// process the response here: executed in background thread
        	Log.d("Facebook-FbAPIs", "Got response: " + response);
        	
        	try {
                jsonArray = new JSONObject(response).getJSONArray("data");
                if (jsonArray == null) {
                	Log.d("Facebook-FbAPIs","Error: jsonArray is null");
                    return;
                }
            } catch (JSONException e) {
            	Log.d("Facebook-FbAPIs","Error: jsonArray exception");
                return;
            }
        	
        	
        	mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listView = (ListView) findViewById(R.id.placeList);
                    nearbyPlaces = new Place[jsonArray.length()];
                    String placeNames[] = new String[jsonArray.length()];
                    
                    JSONObject jsonObject = null;
                    String id;
                    String name;
                    
                    for(int i=0; i<jsonArray.length(); i++){
                    	
                    	try {
							jsonObject = jsonArray.getJSONObject(i);
							name = jsonObject.getString("name");
							id = jsonObject.getString("id");
							nearbyPlaces[i] = new Place(id, name);
							placeNames[i] = name;
							
							Log.d("Facebook-APIs","New Place" + nearbyPlaces[i].getId() + ":" + nearbyPlaces[i].getName());
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
                    	
                    }
                    
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(CheckinActivity.this, android.R.layout.simple_list_item_1, placeNames);
                    listView.setAdapter(adapter);
                    //listView.setOnItemClickListener(CheckinActivity.this);
                    //listView.setAdapter(new PlacesListAdapter(CheckinActivity.this));
                }
            });
        	
        }
        
    }
        
}


