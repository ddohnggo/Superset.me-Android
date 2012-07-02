package com.superset.me;

import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
	Geocoder geocoder; //<3>
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);
        
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

            facebook.authorize(this, new String[] {}, new DialogListener() {
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
        geocoder = new Geocoder(this); //<3>
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); //<5>
        
        // If location not null, then update view
        this.locationText.setText("I have changed this text label" + location.toString());
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

    /**
     *  
     * @param view
     * 
     * The following function is called when a Checkin is desired
     * Send to both Parse and Facebook Open Graph
     */
    public void sendCheckin(View view){
    	
    	
    }
}
