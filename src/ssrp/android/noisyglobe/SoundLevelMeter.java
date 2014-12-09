package ssrp.android.noisyglobe;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Handler;
import android.widget.TextView;

public class SoundLevelMeter {

	public Activity activity;

	protected MediaRecorder mRecorder = null;
	protected Double amp_ref = 0.6;
	protected Integer mInterval = 200;

	protected TextView txtSoundLevel;
	protected TextView txtLongitude;
	protected TextView txtLatitude;

	protected Handler mHandler;
	protected GPSTracker gpsTracker;

	protected Double longitude = 0.0;
	protected Double latitude = 0.0;
	protected Double soundPressureLevel = 0.0;

	public SoundLevelMeter(Activity _activity) {
		this.activity = _activity;

		txtSoundLevel = (TextView) this.activity
				.findViewById(R.id.txtSoundLevel);
		txtLongitude = (TextView) this.activity.findViewById(R.id.txtLongitude);
		txtLatitude = (TextView) this.activity.findViewById(R.id.txtLatitude);

		mHandler = new Handler();
		gpsTracker = new GPSTracker(_activity);

		try {
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
					longitude = gpsTracker.getLongitude();
					latitude = gpsTracker.getLatitude();
					soundPressureLevel = getMaximumAmplitude();
					if (soundPressureLevel > 0) {
						txtSoundLevel.post(new Runnable() {
							public void run() {
								txtSoundLevel.setText(Double
										.toString(soundPressureLevel));
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
			}

		}, 0, mInterval);
		Thread taggedSoundLevel = new Thread(new Runnable() {
			public void run() {

			}
		});

		taggedSoundLevel.start();
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
			power_db = (double) Math.round(power_db * 100.0) / 100.0;
		}
		return power_db;

	}

	protected void updateApplicationUi() {
		if (soundPressureLevel > 0) {

		}
	}
}
