package ssrp.android.noisyglobe;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.widget.Toast;

public class NoisyGlobeService extends Service {

	private SoundLevelMeter slm;
	private DataUploader dataUploader;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "NoisyGlobe service started", Toast.LENGTH_LONG).show();

		slm = new SoundLevelMeter(this);
		slm.startMeasuringSoundLevel();
		slm.measureSoundLevel();
		
		slm.gpsTracker.getLocation();

		dataUploader = new DataUploader(this);
		dataUploader.uploadSoundValues();

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, "NoisyGlobe service stopped", Toast.LENGTH_LONG).show();

		slm.stopMeasuringSoundLevel();
		slm.stopMediaRecorder();
		slm.cancelTimers();
		dataUploader.cancelTimers();
		if(slm.phoneStateListener != null){
			slm.telephonyManager.listen(slm.phoneStateListener, PhoneStateListener.LISTEN_NONE);
		}
		slm.gpsTracker.locationManager.removeGpsStatusListener(slm.gpsTracker);
		slm.gpsTracker.locationManager.removeUpdates(slm.gpsTracker);
		slm.gpsTracker.locationManager = null;
	}

	public SoundLevelMeter getSlm() {
		return slm;
	}

	public DataUploader getDataUploader() {
		return dataUploader;
	}
	
	

}
