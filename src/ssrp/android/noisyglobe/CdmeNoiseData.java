package ssrp.android.noisyglobe;

import android.provider.BaseColumns;

public final class CdmeNoiseData {

	public CdmeNoiseData(){}
	
	/* Inner class that defines the table contents */
    public static abstract class NoiseEntry implements BaseColumns {
        public static final String TABLE_NAME = "cdme_noise_data";
        public static final String SETTINGS_TABLE_NAME = "noisy_globe_settings";
        
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_SOUND_LEVEL = "sound_level";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_UNIXTIME = "timestamp";
        public static final String COLUMN_NAME_IS_UPLOADED = "is_uploaded";
    }
}
