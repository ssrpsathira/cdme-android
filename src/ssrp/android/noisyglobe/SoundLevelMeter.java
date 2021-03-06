package ssrp.android.noisyglobe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ssrp.android.noisyglobe.CdmeNoiseData.NoiseEntry;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class SoundLevelMeter implements SensorEventListener {

	public Context context;

	protected MediaRecorder mRecorder = null;
	protected Double amp_ref = 0.6;
	protected Integer mInterval = 3000;
	protected Integer record_rate = 5;

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
	private SensorManager mSensorManager;
	protected AudioManager audioManager;
	private Sensor mProximity;

	protected Boolean isPausedInCall = false;
	protected PhoneStateListener phoneStateListener;
	protected TelephonyManager telephonyManager;

	protected boolean callAnswered = false;

	public SoundLevelMeter(Context context) {
		this.context = context;
		mHandler = new Handler();
		gpsTracker = new GPSTracker(context);
		dbHandler = new DataBaseHandler(context);
		timer = new Timer();

		mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		mSensorManager.registerListener(this, mProximity,
				SensorManager.SENSOR_DELAY_NORMAL);

		audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);

		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		phoneStateListener = new PhoneStateListener() {

			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
				case TelephonyManager.CALL_STATE_OFFHOOK:
					isPausedInCall = true;
					callAnswered = true;
					stopMediaRecorder();
					showToastMessage("NoisyGlobe: Active call");
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					isPausedInCall = true;
					stopMediaRecorder();
					showToastMessage("NoisyGlobe: Active call");
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					if (callAnswered) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						startMediaRecorder();
					}else{
						startMediaRecorder();
					}
					isPausedInCall = false;
					showToastMessage("NoisyGlobe: Call state idle");
					break;
				}
			}
		};
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);

		startTime = System.currentTimeMillis() / 1000L;
		startMediaRecorder();

	}

	public boolean isLoudspeakerOn() {
		boolean value = false;
		if (audioManager.isMusicActive()) {
			value = true;
			Log.i("music on", "music on");
			showToastMessage("NoisyGlobe: Loudspeaker on");
		}
		return value;
	}

	public void measureSoundLevel() {
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				boolean result = isLoudspeakerOn();
				Log.i("gps tracker",
						Boolean.toString(gpsTracker.canMeasureSound));
				if (gpsTracker.canMeasureSound && !paused && !result
						&& !isPausedInCall) {
					currentTime = System.currentTimeMillis() / 1000L;
					longitude = gpsTracker.getLongitude();
					latitude = gpsTracker.getLatitude();
					soundPressureLevel = getMaximumAmplitude();
					if ((currentTime - startTime) >= record_rate) {
						double averageSound = getAverageSoundLevel(soundPressureLevelArrayList);
						Log.i("soundPressureLevel",
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
				} else {
					soundPressureLevel = 0.0;
				}
			}

		}, 0, mInterval);
	}

	public void stopMeasuringSoundLevel() {
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

	protected void startMediaRecorder() {
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mRecorder.setOutputFile("/dev/null");
			try {
				mRecorder.prepare();
				mRecorder.start();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected void stopMediaRecorder() {
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.reset();
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
		long now = 0;

		now = System.currentTimeMillis();
		if (spl > 0 && now > 0) {
			String param[] = { Double.toString(spl),
					Double.toString(longitude), Double.toString(latitude),
					Long.toString(now / 1000L) };

			dbHandler.insertTableDataRow(NoiseEntry.TABLE_NAME, param);
		}
	}

	protected boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) context
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

	public void cancelTimers() {
		timer.cancel();
	}

	protected void showToastMessage(final String message) {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Log.v("sensor event", Float.toString(event.values[0]));
		if (event.values[0] > 0.0) {
			startMeasuringSoundLevel();
			showToastMessage("NoisyGlobe: Open space");
		} else {
			stopMeasuringSoundLevel();
			showToastMessage("NoisyGlobed: Closed space");
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	public GPSTracker getGpsTracker() {
		return gpsTracker;
	}

	public Boolean getPaused() {
		return paused;
	}

	public Boolean getIsPausedInCall() {
		return isPausedInCall;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public void setSoundPressureLevel(Double soundPressureLevel) {
		this.soundPressureLevel = soundPressureLevel;
	}

}
