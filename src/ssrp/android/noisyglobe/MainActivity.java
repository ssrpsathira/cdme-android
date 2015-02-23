package ssrp.android.noisyglobe;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity {

	protected DataBaseHandler dbHandler;
	public ViewPager viewPager = null;
	DataUploader dataUploader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);

		dbHandler = new DataBaseHandler(this);
		dbHandler.createTables();

		viewPager = (ViewPager) findViewById(R.id.pager);
		FragmentManager fragmentManager = getSupportFragmentManager();
		viewPager.setAdapter(new NoisyGlobeFragmentAdapter(fragmentManager));

		dataUploader = new DataUploader(this);
		dataUploader.uploadSoundValues();

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isNoisyGlobeServiceRunning(NoisyGlobeService.class)) {
			stopService(new Intent(getBaseContext(), NoisyGlobeService.class));
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		String mode = dbHandler.getOperationalMode();
		if (mode.equals(SettingsViewFragment.OPERATIONAL_MODE_SERVICE)) {
			startService(new Intent(getBaseContext(), NoisyGlobeService.class));
			android.os.Process.killProcess(android.os.Process.myPid());
		}
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

	protected boolean isNoisyGlobeServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	class NoisyGlobeFragmentAdapter extends FragmentPagerAdapter {

		public NoisyGlobeFragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {
			Fragment fragment = null;
			if (arg0 == 0) {
				fragment = new MapViewFragment(getApplicationContext());
			} else if (arg0 == 1) {
				fragment = new SettingsViewFragment(getApplicationContext());
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			String title = new String();
			if (position == 0) {
				title = "Noise Map";
			} else if (position == 1) {
				title = "Settings";
			}
			return title;
		}

	}
}
