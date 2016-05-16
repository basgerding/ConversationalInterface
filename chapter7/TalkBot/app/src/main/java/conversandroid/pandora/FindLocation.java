package conversandroid.pandora;

/**
 * This class is used to know the location of the user in order to resolve
 * commands asking for directions. It requires an explicit permission to check
 * the location of the device
 *
 * In Android 6 devices or newer (API level 23), it asks for permissions at run time
 * (see: http://developer.android.com/intl/es/training/permissions/requesting.html).
 * In earlier versions permissions are granted when isntalling the app
 */


/*
 *  Code adapted from examples in the Book:
 *  Reto Meier: Professional Android 4 Application Development, Chapter 13. Wrox, 2012
 *  
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;


public class FindLocation implements LocationListener {
	
	private LocationManager locationManager;
    private double latitude;
    private double longitude;
    private Criteria criteria;
    private String provider;
    private static final String LOGTAG = "FindLocation";

    private final int MY_PERMISSIONS_REQUEST_LOCATION = 50;


    // fire the updateWithNewLocation method whenever a location change is detected
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
          updateWithNewLocation(location);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, 
                                    Bundle extras) {}
      };

    public FindLocation(Context context) {
        locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        provider = locationManager.getBestProvider(criteria, true);

        checkLocationPermission(context);
        Location location = locationManager.getLastKnownLocation(provider);
        updateWithNewLocation(location);
        
        // updates restricted to every 2 seconds and only when movement
        // of more than 10 metres has been detected
        locationManager.requestLocationUpdates(provider,2000,10,locationListener);
    }
	    
    private void updateWithNewLocation(Location location) {
        if (location != null) {
             latitude = location.getLatitude();
             longitude = location.getLongitude();
             Log.d(LOGTAG, "Latitude " + latitude);
             Log.d(LOGTAG, "Longitude " + longitude);
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * Checks whether the user has granted permission to access the user's location.
     * If the permission has not been provided, it is requested. The result of the request
     * (whether the user finally grants the permission or not)
     * is processed in the onRequestPermissionsResult method.
     *
     * This is necessary from Android 6 (API level 23), in which users grant permissions to apps
     * while the app is running. In previous versions, the permissions were granted when installing the app
     * See: http://developer.android.com/intl/es/training/permissions/requesting.html
     */
    public void checkLocationPermission(Context ctx) {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // If  an explanation is required, show it
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) ctx, Manifest.permission.ACCESS_FINE_LOCATION))
                showLocationPermissionExplanation(ctx);

            // Request the permission.
            ActivityCompat.requestPermissions((Activity) ctx, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION); //Callback in "onRequestPermissionResult"
        }
    }

    /**
     * Processes the result of permission request. If it is not granted, the
     * abstract method "onPermissionDenied" method is invoked.
     * More info: http://developer.android.com/intl/es/training/permissions/requesting.html
     * */
    public void onRequestPermissionsResult(Context ctx, int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(LOGTAG, "Access location permission granted");
            } else {
                Log.i(LOGTAG, "Access location permission denied");
                onLocationPermissionDenied(ctx);
            }
        }
    }

    /**
     * If the user does not grant permission to access location on the device, a message is shown and the app finishes
     */
    public void showLocationPermissionExplanation(Context ctx){
        Toast.makeText(ctx, "TalkBot needs your permission to access your location to provide directions", Toast.LENGTH_SHORT).show();
    }

    /**
     * Invoked when the permission to access location on the device is denied
     */
    public void onLocationPermissionDenied(Context ctx){
        Toast.makeText(ctx, "Sorry, TalkBot cannot work without accessing the microphone", Toast.LENGTH_SHORT).show();
        System.exit(0);
    }

	@Override
	public void onLocationChanged(Location location) {}

	@Override
	public void onProviderDisabled(String provider) {}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
}