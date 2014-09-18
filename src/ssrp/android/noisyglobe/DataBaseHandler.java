package ssrp.android.noisyglobe;

import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DataBaseHandler {
	protected Context applicationContext;

	protected SQLiteDatabase noisyGlobeDataBase;
	protected String noisyGlobeDataBaseName = "noisyGlobe";
	protected String noiseDataTable = "noisy_globe_noise_data";

	public DataBaseHandler(Context context) {
		this.applicationContext = context;
		createOrOpenDatabase();
		createDataStorageTable();
	}

	protected void createDataStorageTable() {
		noisyGlobeDataBase.execSQL("CREATE TABLE IF NOT EXISTS "
				+ noiseDataTable + " (id BIGINT NOT NULL AUTOINCREMENT, "
				+ "sound_level VARCHAR(10), " + "longitude VARCHAR(50), "
				+ "latitude VARCHAR(50),"
				+ "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP);");
	}

	protected void createOrOpenDatabase() {
		noisyGlobeDataBase = applicationContext.openOrCreateDatabase(
				noisyGlobeDataBaseName, Context.MODE_PRIVATE, null);
	}

	public void insertTableDataRow(String tableName, Map<String, String> param) {
		String fieldSet = null;
		String valueSet = null;
		for (Map.Entry<String, String> entry : param.entrySet()) {
			fieldSet += entry.getKey() + ",";
			valueSet += "\'"+entry.getValue() + "\',";
		}
		fieldSet = (fieldSet.replaceAll(",$", "")).trim();
		valueSet = (valueSet.replaceAll(",$", "")).trim();
		String query = "INSERT INTO " + tableName + "(" + fieldSet
				+ ") VALUES (" + valueSet + ");";
		noisyGlobeDataBase.execSQL(query);
	}
}
