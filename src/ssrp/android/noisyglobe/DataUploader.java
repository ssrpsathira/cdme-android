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
import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DataUploader {

	protected TelephonyManager tlphnyMngr;
	protected DataBaseHandler dbHandler;
	protected Integer uploadIntervel = 2000;
	protected String imei;
	protected HttpResponse httpresponse;
	protected ConnectionDetector cd;

	public DataUploader(Activity _activity) {
		dbHandler = new DataBaseHandler(_activity);
		tlphnyMngr = (TelephonyManager) _activity
				.getSystemService(Context.TELEPHONY_SERVICE);
		imei = tlphnyMngr.getDeviceId();
		this.cd = new ConnectionDetector(_activity);
	}

	public void uploadSoundValues() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (cd.isConnectingToInternet()) {
					ArrayList<NoiseObject> noiseObjectsArrayList = new ArrayList<NoiseObject>();
					noiseObjectsArrayList = dbHandler
							.getNoiseEntries(NoiseEntry.TABLE_NAME);
					Log.v("hi", "hmm");
					if (noiseObjectsArrayList != null) {
						Log.v("hi", "not null");
						for (NoiseObject obj : noiseObjectsArrayList) {
							if (uploadNoiseDataEntry(obj).getStatusLine()
									.getStatusCode() == 200) {
								Log.v("sound upload", Double.toString(obj.getLongitude()));
								Log.v("sound upload", Double.toString(obj.getLatitude()));
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
}
