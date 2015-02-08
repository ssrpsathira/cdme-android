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
import android.telephony.TelephonyManager;
import android.widget.TextView;

public class SoundLevelMeter {

	public Activity activity;

	protected MediaRecorder mRecorder = null;
	protected Double amp_ref = 0.6;
	protected Integer mInterval = 200;
	protected Integer record_rate = 1;

	protected TextView txtSoundLevel;
	protected TextView txtLongitude;
	protected TextView txtLatitude;

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

	public SoundLevelMeter(Activity _activity) {
		this.activity = _activity;

		txtSoundLevel = (TextView) this.activity
				.findViewById(R.id.txtSoundLevel);
		txtLongitude = (TextView) this.activity.findViewById(R.id.txtLongitude);
		txtLatitude = (TextView) this.activity.findViewById(R.id.txtLatitude);

		mHandler = new Handler();
		gpsTracker = new GPSTracker(_activity);
		dbHandler = new DataBaseHandler(_activity);

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
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if (gpsTracker.canGetLocation) {
					currentTime = System.currentTimeMillis() / 1000L;
					longitude = gpsTracker.getLongitude();
					latitude = gpsTracker.getLatitude();
					soundPressureLevel = getMaximumAmplitude();
					updateApplicationUi(soundPressureLevel);
					if ((currentTime - startTime) >= record_rate) {
						storeSoundData(
								getAverageSoundLevel(soundPressureLevelArrayList),
								longitude, latitude);
						startTime = currentTime;
						soundPressureLevelArrayList.clear();
					} else {
						soundPressureLevelArrayList.add(soundPressureLevel);
					}
				}
			}

		}, 0, mInterval);
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
		if (spl > 0) {
			txtSoundLevel.post(new Runnable() {
				public void run() {
					txtSoundLevel.setText(Double.toString(soundPressureLevel));
				}
			});
			txtLongitude.post(new Runnable() {
				public void run() {
					txtLongitude.setText(Double.toString(longitude));
				}
			});
			txtLatitude.post(new Runnable() {
				public void run() {
					txtLatitude.setText(Double.toString(latitude));
				}
			});
		}
	}

	protected void storeSoundData(Double spl, Double longitude, Double latitude) {
		if (spl > 0) {
			String param[] = { Double.toString(spl),
					Double.toString(longitude), Double.toString(latitude),
					Long.toString(System.currentTimeMillis() / 1000L)};

			dbHandler.insertTableDataRow(NoiseEntry.TABLE_NAME, param);
		}
	}

	protected boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) this.activity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}
}
