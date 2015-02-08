package ssrp.android.noisyglobe;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {

	SoundLevelMeter slm;
	DataUploader dataUploader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		slm = new SoundLevelMeter(this);
		slm.measureSoundLevel();
		
		dataUploader = new DataUploader(this);
		dataUploader.uploadSoundValues();
	}

	@Override
	protected void onResume() {
		super.onResume();
		slm.measureSoundLevel();
	};

	@Override
	protected void onPause() {
		super.onPause();
		slm.stopMediaRecorder();
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
