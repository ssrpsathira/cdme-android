package ssrp.android.noisyglobe;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

public class MyLocationListener implements LocationListener {

	protected Context applicationContext;
	protected Double longitude;
	protected Double latitude;

	public MyLocationListener(Context applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void onLocationChanged(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();;

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	public Double getLongitude() {
		return longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(applicationContext, "Gps Enabled", Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(applicationContext, "Gps Disabled", Toast.LENGTH_SHORT)
				.show();
	}
}
