package ssrp.android.noisyglobe;

import android.content.Context;
import android.webkit.JavascriptInterface;

public class WebAppInterface {
	Context mContext;

	/** Instantiate the interface and set the context */
	public WebAppInterface(Context c) {
		mContext = c;
	}

	/**
	 * Show a toast from the web page
	 * 
	 * @return
	 */
	@JavascriptInterface
	public String getLongitudeLatitudeSound() {
		return Double.toString(MapViewFragment.slm.getLongitude()) + ":"
				+ Double.toString(MapViewFragment.slm.getLatitude()) + ":"
				+ Double.toString(MapViewFragment.slm.getSoundPressureLevel());
	}
}
