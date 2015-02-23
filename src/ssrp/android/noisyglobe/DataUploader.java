package ssrp.android.noisyglobe;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import ssrp.android.noisyglobe.CdmeNoiseData.NoiseEntry;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DataUploader {

	protected TelephonyManager tlphnyMngr;
	protected DataBaseHandler dbHandler;
	protected Integer uploadIntervel = 1000;
	protected String imei;
	protected HttpResponse httpresponse;
	protected ConnectionDetector cd;
	protected Timer timer;

	public DataUploader(Context context) {
		dbHandler = new DataBaseHandler(context);
		tlphnyMngr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		imei = tlphnyMngr.getDeviceId();
		this.cd = new ConnectionDetector(context);
	}

	public void uploadSoundValues() {
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				String mode = dbHandler.getDataUploadMode();
				if(mode.equals(SettingsViewFragment.DATA_UPLOAD_MODE_WIFI)){
					if(!cd.isWifiConnected()){
						return;
					}
				}
				if (cd.isConnectingToInternet()) {
					ArrayList<NoiseObject> noiseObjectsArrayList = new ArrayList<NoiseObject>();
					noiseObjectsArrayList = dbHandler
							.getNoiseEntries(NoiseEntry.TABLE_NAME);
					Log.i("hi", "initiated uploading");
					if (noiseObjectsArrayList != null) {
						Log.i("hi", "not null");
						for (NoiseObject obj : noiseObjectsArrayList) {
							if (uploadNoiseDataEntry(obj).getStatusLine()
									.getStatusCode() == 200) {
								Log.i("sound upload", Double.toString(obj.getLongitude()));
								Log.i("sound upload", Double.toString(obj.getLatitude()));
								Log.i("sound upload", Double.toString(obj.getSoundLevel()));
								deleteEntityFromLocalStorage(obj);
								//clearLocalStorage();
							}
						}
					}
				}
			}
		}, 0, uploadIntervel);
	}

	public void deleteEntityFromLocalStorage(NoiseObject obj) {
		dbHandler.deleteEntityFromLocalStorage(obj);
	}
	
	public void clearLocalStorage(){
		dbHandler.clearLocalStorage();
	}

	public HttpResponse uploadNoiseDataEntry(NoiseObject obj) {
		JSONObject mainObject = new JSONObject();
		JSONObject metaDataObject = new JSONObject();
		JSONObject rawDataObject = new JSONObject();

		try {
			metaDataObject.put("service", "noise");
			metaDataObject.put("mode", "upload");
			metaDataObject.put("imei", imei);
			metaDataObject.put("feature", "noiseData");

			rawDataObject.put("longitude", obj.getLongitude());
			rawDataObject.put("latitude", obj.getLatitude());
			rawDataObject.put("noise_level", obj.getSoundLevel());
			rawDataObject.put("date_time", obj.getDateTime());

			mainObject.put("metadata", metaDataObject);
			mainObject.put("rawdata", rawDataObject);

			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httppostreq = new HttpPost(
					"http://156.56.93.34/CDME/request.php");
			StringEntity se = new StringEntity(mainObject.toString());
			se.setContentType("application/json;charset=UTF-8");
			se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json;charset=UTF-8"));
			httppostreq.setEntity(se);
			httpresponse = httpclient.execute(httppostreq);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return httpresponse;
	}

	public void cancelTimers() {
		timer.cancel();
	}
}
