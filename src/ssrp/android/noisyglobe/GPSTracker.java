package ssrp.android.noisyglobe;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

public class GPSTracker extends Service implements LocationListener, Listener {

	private final Context mContext;

	// flag for GPS status
	boolean isGPSEnabled = false;

	Location location; // location
	double latitude; // latitude
	double longitude; // longitude

	// The minimum distance to change Updates in meters
	private static final float MIN_DISTANCE_CHANGE = 10;
	private static final float LOCATION_UPDATE_THRESHOLD = 30;
	private static final float LOCATION_ACCURACY_THRESHOLD = 10;

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 0;

	// Declaring a Location Manager
	protected LocationManager locationManager;

	public boolean canMeasureSound;

	private boolean canGetLocation;

	public GPSTracker(Context context) {
		mContext = context;
	}

	protected void showToastMessage(final String message) {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public boolean getLocation() {
		locationManager = (LocationManager) mContext
				.getSystemService(LOCATION_SERVICE);

		// getting GPS status
		isGPSEnabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (!isGPSEnabled) {
			showToastMessage("You need to enable GPS to monitor sound");
			canMeasureSound = false;
			return false;
		}

		locationManager.addGpsStatusListener(this);

		// finally require updates at -at least- the desired rate
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE, this);

		return true;
	}

	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}
		// return latitude
		return latitude;
	}

	/**
	 * Function to get longitude
	 * */

	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}
		// return longitude
		return longitude;
	}

	@Override
	public void onLocationChanged(Location loc) {
		if(canGetLocation){
			if(loc.getAccuracy() < LOCATION_ACCURACY_THRESHOLD){
				if(location.distanceTo(loc) > LOCATION_UPDATE_THRESHOLD){
					location = loc;
					showToastMessage("NoisyGlobe: Location changed");
				}
			}
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onGpsStatusChanged(int event) {
		switch (event) {
		case GpsStatus.GPS_EVENT_STARTED:
			canGetLocation = false;
			showToastMessage("NoisyGlobe: Location not stable");
			break;
		case GpsStatus.GPS_EVENT_STOPPED:
			showToastMessage("NoisyGlobe: Monitoring stopped");
			canGetLocation = false;
			break;
		case GpsStatus.GPS_EVENT_FIRST_FIX:

			/*
			 * GPS_EVENT_FIRST_FIX Event is called when GPS is locked
			 */
			location = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);

			if (location != null) {
				canGetLocation = true;
				canMeasureSound = true;
				showToastMessage("NoisyGlobe: Location stable");
			}

			break;
		case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
			break;
		}

	}
}
