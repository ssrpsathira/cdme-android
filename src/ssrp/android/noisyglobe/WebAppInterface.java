package ssrp.android.noisyglobe;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebAppInterface {
	Context mContext;

	/** Instantiate the interface and set the context */
	WebAppInterface(Context c) {
		mContext = c;
	}

	/** Show a toast from the web page */
	@JavascriptInterface
	public void showToast(String toast) {
		Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
	}
	
	/** Show a toast from the web page 
	 * @return */
	@JavascriptInterface
	public String getLongitudeLatitudeSound() {
		return Double.toString(MainActivity.slm.getLongitude()) +":"+Double.toString(MainActivity.slm.getLatitude())+":"+Double.toString(MainActivity.slm.getSoundPressureLevel());
	}
}
