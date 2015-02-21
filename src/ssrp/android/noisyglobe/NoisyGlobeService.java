package ssrp.android.noisyglobe;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
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
		Toast.makeText(this, "service started", Toast.LENGTH_LONG).show();

		slm = new SoundLevelMeter(this);
		slm.startMeasuringSoundLevel();
		slm.measureSoundLevel();

		dataUploader = new DataUploader(this);
		dataUploader.uploadSoundValues();

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, "service stopped", Toast.LENGTH_LONG).show();

		slm.stopMeasuringSoundLevel();
		slm.stopMediaRecorder();
		slm.cancelTimers();
		dataUploader.cancelTimers();
	}

}
