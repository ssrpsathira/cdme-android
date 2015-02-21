package ssrp.android.noisyglobe;

import java.util.ArrayList;

import ssrp.android.noisyglobe.CdmeNoiseData.NoiseEntry;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHandler extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	protected Context applicationContext;
	protected SQLiteDatabase noisyGlobeDataBase;
	public static String noisyGlobeDataBaseName = "noisyGlobe";

	public DataBaseHandler(Context context) {
		super(context, noisyGlobeDataBaseName, null, DATABASE_VERSION);
		this.applicationContext = context;
		createOrOpenDatabase();
	}

	protected void createOrOpenDatabase() {
		noisyGlobeDataBase = applicationContext.openOrCreateDatabase(
				noisyGlobeDataBaseName, Context.MODE_PRIVATE, null);
	}

	public void insertTableDataRow(String tableName, String param[]) {
		try {
			String query = "INSERT INTO " + tableName + " (`"
					+ NoiseEntry.COLUMN_NAME_SOUND_LEVEL + "`,`"
					+ NoiseEntry.COLUMN_NAME_LONGITUDE + "`,`"
					+ NoiseEntry.COLUMN_NAME_LATITUDE + "`,`"
					+ NoiseEntry.COLUMN_NAME_UNIXTIME + "`,`"
					+ NoiseEntry.COLUMN_NAME_IS_UPLOADED + "`) VALUES ("
					+ param[0] + "," + param[1] + "," + param[2] + ","
					+ param[3] + ", 0);";
			noisyGlobeDataBase.execSQL(query);
		} catch (SQLiteException s) {
			s.printStackTrace();
		}
	}

	public void createTables() {
		String query = "CREATE TABLE IF NOT EXISTS `" + NoiseEntry.TABLE_NAME
				+ "` (`" + NoiseEntry.COLUMN_NAME_ID
				+ "` INTEGER PRIMARY KEY, `"
				+ NoiseEntry.COLUMN_NAME_SOUND_LEVEL + "` TEXT, `"
				+ NoiseEntry.COLUMN_NAME_LONGITUDE + "` TEXT, `"
				+ NoiseEntry.COLUMN_NAME_LATITUDE + "` TEXT, `"
				+ NoiseEntry.COLUMN_NAME_UNIXTIME + "` TEXT, `"
				+ NoiseEntry.COLUMN_NAME_IS_UPLOADED + "` INT DEFAULT 0);";
		noisyGlobeDataBase.execSQL(query);

		query = "CREATE TABLE IF NOT EXISTS `" + NoiseEntry.SETTINGS_TABLE_NAME
				+ "` (`property` VARCHAR(50) PRIMARY KEY, `value` VARCHAR(50))";
		noisyGlobeDataBase.execSQL(query);
	}

	public ArrayList<NoiseObject> getNoiseEntries(String tableName) {
		try {
			String query = "SELECT * FROM " + NoiseEntry.TABLE_NAME
					+ " WHERE `" + NoiseEntry.COLUMN_NAME_IS_UPLOADED
					+ "` = 0 ORDER BY `" + NoiseEntry.COLUMN_NAME_UNIXTIME
					+ "`;";
			ArrayList<NoiseObject> noiseObjectsArrayList = new ArrayList<NoiseObject>();
			Cursor c = noisyGlobeDataBase.rawQuery(query, null);
			if (c.moveToFirst()) {
				do {
					String soundLevel = c.getString(1);
					String longitude = c.getString(2);
					String latitude = c.getString(3);
					String dateTime = c.getString(4);
					String[] param = { soundLevel, longitude, latitude,
							dateTime };
					NoiseObject noiseObject = new NoiseObject(param);
					noiseObjectsArrayList.add(noiseObject);

				} while (c.moveToNext());
				return noiseObjectsArrayList;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void deleteEntityFromLocalStorage(NoiseObject obj) {
		try {
			String query = "DELETE FROM " + NoiseEntry.TABLE_NAME + " WHERE `"
					+ NoiseEntry.COLUMN_NAME_SOUND_LEVEL + "` = "
					+ obj.getSoundLevel() + " AND `"
					+ NoiseEntry.COLUMN_NAME_LONGITUDE + "` = "
					+ obj.getLongitude() + " AND `"
					+ NoiseEntry.COLUMN_NAME_LATITUDE + "` = "
					+ obj.getLatitude() + " AND `"
					+ NoiseEntry.COLUMN_NAME_UNIXTIME + "` = "
					+ obj.getDateTime() + ";";
			noisyGlobeDataBase.execSQL(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void clearLocalStorage() {
		try {
			String query = "DELETE FROM `" + NoiseEntry.TABLE_NAME + "`;";
			noisyGlobeDataBase.execSQL(query);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setOperationalMode(String mode) {
		String query = "REPLACE INTO `" + NoiseEntry.SETTINGS_TABLE_NAME
				+ "` (`property`, `value`) VALUES ('operational_mode', '"
				+ mode + "');";
		noisyGlobeDataBase.execSQL(query);
	}

	public String getOperationalMode() {
		String value = null;
		String query = "SELECT `value` FROM `" + NoiseEntry.SETTINGS_TABLE_NAME
				+ "` WHERE `property` = 'operational_mode';";
		Cursor c = noisyGlobeDataBase.rawQuery(query, null);
		if (!c.equals(null) && c.moveToFirst()) {
			do {
				value = c.getString(0);
			} while (c.moveToNext());
		}
		if (value.equals(null) || value.equals("application")) {
			value = SettingsViewFragment.OPERATIONAL_MODE_APPLICATION;
		} else if (value.equals("service")) {
			value = SettingsViewFragment.OPERATIONAL_MODE_SERVICE;
		}
		return value;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String query = "CREATE TABLE IF NOT EXISTS `" + NoiseEntry.TABLE_NAME
				+ "` (`" + NoiseEntry.COLUMN_NAME_ID
				+ "` INTEGER PRIMARY KEY, `"
				+ NoiseEntry.COLUMN_NAME_SOUND_LEVEL + "` TEXT, `"
				+ NoiseEntry.COLUMN_NAME_LONGITUDE + "` TEXT, `"
				+ NoiseEntry.COLUMN_NAME_LATITUDE + "` TEXT, `"
				+ NoiseEntry.COLUMN_NAME_UNIXTIME + "` TEXT, `"
				+ NoiseEntry.COLUMN_NAME_IS_UPLOADED + "` INT DEFAULT 0);";
		db.execSQL(query);

		query = "CREATE TABLE IF NOT EXISTS `"
				+ NoiseEntry.SETTINGS_TABLE_NAME
				+ "` (`property` VARCHAR(50) PRIMARY KEY, `value` VARCHAR(50));";
		db.execSQL(query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public void setDataUploadMode(String mode) {
		String query = "REPLACE INTO `" + NoiseEntry.SETTINGS_TABLE_NAME
				+ "` (`property`, `value`) VALUES ('data_upload_mode', '"
				+ mode + "');";
		noisyGlobeDataBase.execSQL(query);
	}

	public String getDataUploadMode() {
		String value = null;
		String query = "SELECT `value` FROM `" + NoiseEntry.SETTINGS_TABLE_NAME
				+ "` WHERE `property` = 'data_upload_mode';";
		Cursor c = noisyGlobeDataBase.rawQuery(query, null);
		if (!c.equals(null) && c.moveToFirst()) {
			do {
				value = c.getString(0);
			} while (c.moveToNext());
		}
		if (value == null || value.equals("wifi")) {
			value = SettingsViewFragment.DATA_UPLOAD_MODE_WIFI;
		} else if (value.equals("wifi_if_available")) {
			value = SettingsViewFragment.DATA_UPLOAD_MODE_WIFI_IF_AVAILABLE;
		}
		return value;
	}
}
