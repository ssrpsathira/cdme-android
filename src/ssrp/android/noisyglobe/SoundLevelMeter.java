package ssrp.android.noisyglobe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ssrp.android.noisyglobe.CdmeNoiseData.NoiseEntry;
import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

public class SoundLevelMeter {

	public Activity activity;

	protected MediaRecorder mRecorder = null;
	protected Double amp_ref = 0.6;
	protected Integer mInterval = 500;
	protected Integer record_rate = 1;

	protected Handler mHandler;
	protected GPSTracker gpsTracker;
	protected DataBaseHandler dbHandler;
	protected DataUploader dataUploader;

	protected Double longitude = 0.0;
	protected Double latitude = 0.0;
	protected Double soundPressureLevel = 0.0;
	protected ArrayList<Double> soundPressureLevelArrayList = new ArrayList<Double>();

	protected Long startTime;
	protected Long currentTime;
	protected Timer timer;

	protected Boolean paused = false;

	public SoundLevelMeter(Activity _activity) {
		this.activity = _activity;
		mHandler = new Handler();
		gpsTracker = new GPSTracker(_activity);
		dbHandler = new DataBaseHandler(_activity);
		timer = new Timer();
		try {
			startTime = System.currentTimeMillis() / 1000L;
			startMediaRecorder();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void measureSoundLevel() {
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if (gpsTracker.canGetLocation && !paused) {
					currentTime = System.currentTimeMillis() / 1000L;
					longitude = gpsTracker.getLongitude();
					latitude = gpsTracker.getLatitude();
					soundPressureLevel = getMaximumAmplitude();
					if ((currentTime - startTime) >= record_rate) {
						double averageSound = getAverageSoundLevel(soundPressureLevelArrayList);
						Log.v("soundPressureLevel",
								Double.toString(averageSound));
						if (averageSound > 0.0) {
							storeSoundData(averageSound, longitude, latitude);
							soundPressureLevel = averageSound;
						}
						startTime = currentTime;
						soundPressureLevelArrayList.clear();
					} else {
						soundPressureLevelArrayList.add(soundPressureLevel);
					}
				}
			}

		}, 0, mInterval);
	}

	public void stopMeasuringSoundLevel() {
		soundPressureLevel = 0.0;
		paused = true;
	}

	public void startMeasuringSoundLevel() {
		paused = false;
	}

	protected double getAverageSoundLevel(
			ArrayList<Double> soundPressureLevelArrayList) {
		Double sum = 0.0;
		if (!soundPressureLevelArrayList.isEmpty()) {
			for (Double sound : soundPressureLevelArrayList) {
				sum += sound;
			}
			return sum.doubleValue() / soundPressureLevelArrayList.size();
		}
		return sum;
	}

	protected void startMediaRecorder() throws IllegalStateException,
			IOException {
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mRecorder.setOutputFile("/dev/null");
			mRecorder.prepare();
			mRecorder.start();
		}
	}

	protected void stopMediaRecorder() {
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
	}

	protected double getMaximumAmplitude() {
		double power_db = 0;
		if (mRecorder != null) {
			power_db = 20 * Math.log10(mRecorder.getMaxAmplitude() / amp_ref);
		}
		if (power_db > 0) {
			return power_db;
		} else {
			return 0;
		}

	}

	protected void updateApplicationUi(Double spl) {

	}

	protected void storeSoundData(Double spl, Double longitude, Double latitude) {
		if (spl > 0) {
			String param[] = { Double.toString(spl),
					Double.toString(longitude), Double.toString(latitude),
					Long.toString(System.currentTimeMillis() / 1000L) };

			dbHandler.insertTableDataRow(NoiseEntry.TABLE_NAME, param);
		}
	}

	protected boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) this.activity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}

	public Double getLongitude() {
		return longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getSoundPressureLevel() {
		return soundPressureLevel;
	}

}
