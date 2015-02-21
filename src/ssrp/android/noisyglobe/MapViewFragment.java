package ssrp.android.noisyglobe;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class MapViewFragment extends Fragment{
	public static SoundLevelMeter slm;

	protected Context appContext;

	public MapViewFragment(Context context) {
		this.appContext = context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_map, container, false);
		WebView myWebView = (WebView) view.findViewById(R.id.webview);
		myWebView.loadUrl("file:///android_asset/index.html");
		WebSettings webSettings = myWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		myWebView.addJavascriptInterface(new WebAppInterface(appContext),
				"Android");
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		slm = new SoundLevelMeter(appContext);
		slm.measureSoundLevel();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		slm.startMeasuringSoundLevel();
		slm.measureSoundLevel();
	};

	@Override
	public void onPause() {
		super.onPause();
		
		slm.stopMeasuringSoundLevel();
		slm.stopMediaRecorder();
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
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
