package ssrp.android.noisyglobe;

import java.io.IOException;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	protected MediaRecorder mRecorder = null;
	protected double amp_ref = 0.6;
	protected TextView txtSoundLevel;
	protected TextView txtLongitude;
	protected TextView txtLatitude;
	protected int mInterval = 1000;
	protected Handler mHandler;
	protected GPSTracker gpsTracker;

	protected Double longitude;
	protected Double latitude;

	Runnable mStatusChecker = new Runnable() {
		@Override
		public void run() {
			if (gpsTracker.canGetLocation) {
				longitude = gpsTracker.getLongitude();
				latitude = gpsTracker.getLatitude();
			}
			updateUiValues();
			mHandler.postDelayed(this, mInterval);
		}
	};

	private void startUpdatingUiTask() throws IllegalStateException,
			IOException {
		start();
		mStatusChecker.run();
	}

	private void stopUpdatingUiTask() {
		stop();
		mHandler.removeCallbacks(mStatusChecker);
	}

	private void start() throws IllegalStateException, IOException {
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

	private void stop() {
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
	}

	private double getAmplitude() {
		double power_db = 0;
		if (mRecorder != null) {
			power_db = 20 * Math.log10(mRecorder.getMaxAmplitude() / amp_ref);
			power_db = (double) Math.round(power_db * 100.0) / 100.0;
		}
		return power_db;

	}

	private void updateUiValues() {
		txtSoundLevel.setText(Double.toString(getAmplitude()));
		if (longitude != null) {
			txtLongitude.setText(Double.toString(longitude));
		}
		if (latitude != null) {
			txtLatitude.setText(Double.toString(latitude));
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		txtSoundLevel = (TextView) findViewById(R.id.txtSoundLevel);
		txtLongitude = (TextView) findViewById(R.id.txtLongitude);
		txtLatitude = (TextView) findViewById(R.id.txtLatitude);
		mHandler = new Handler();
		gpsTracker = new GPSTracker(this);

		try {
			startUpdatingUiTask();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			startUpdatingUiTask();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		stopUpdatingUiTask();
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
