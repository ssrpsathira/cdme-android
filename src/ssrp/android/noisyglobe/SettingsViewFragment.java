package ssrp.android.noisyglobe;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingsViewFragment extends Fragment implements
		OnItemSelectedListener {
	protected Context context;
	protected DataBaseHandler dbHandler;
	private Spinner spinnerOperationalMode;
	private Spinner spinnerDataUploadMode;

	public static final String OPERATIONAL_MODE_APPLICATION = "Foreground Only";
	public static final String OPERATIONAL_MODE_SERVICE = "Service on Close";
	public static final String DATA_UPLOAD_MODE_WIFI = "WiFi";
	public static final String DATA_UPLOAD_MODE_WIFI_IF_AVAILABLE = "WiFi if Available";
	private static final String[] operationalModeChoises = {
			OPERATIONAL_MODE_APPLICATION, OPERATIONAL_MODE_SERVICE };
	private static final String[] dataUploadModeChoises = {
			DATA_UPLOAD_MODE_WIFI, DATA_UPLOAD_MODE_WIFI_IF_AVAILABLE };

	public SettingsViewFragment(Context applicationContext) {
		this.context = applicationContext;
		dbHandler = new DataBaseHandler(context);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_settings, container,
				false);
		spinnerOperationalMode = (Spinner) view
				.findViewById(R.id.spinner_operational_mode);
		ArrayAdapter<String> adapterOperationalMode = new ArrayAdapter<String>(
				context, R.layout.custom_spinner_item, operationalModeChoises);
		spinnerDataUploadMode = (Spinner) view
				.findViewById(R.id.spinner_data_upload_mode);
		ArrayAdapter<String> adapterDataUploadMode = new ArrayAdapter<String>(
				context, R.layout.custom_spinner_item, dataUploadModeChoises);

		adapterOperationalMode
				.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
		spinnerOperationalMode.setAdapter(adapterOperationalMode);
		String operationalMode = dbHandler.getOperationalMode();
		if (!operationalMode.equals(null)) {
			int spinnerPostion = adapterOperationalMode
					.getPosition(operationalMode);
			spinnerOperationalMode.setSelection(spinnerPostion);
			spinnerPostion = 0;
		}
		spinnerOperationalMode.setOnItemSelectedListener(this);

		adapterDataUploadMode
				.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
		spinnerDataUploadMode.setAdapter(adapterDataUploadMode);
		String dataUploadMode = dbHandler.getDataUploadMode();
		if (!dataUploadMode.equals(null)) {
			int spinnerPostion = adapterDataUploadMode
					.getPosition(dataUploadMode);
			spinnerDataUploadMode.setSelection(spinnerPostion);
			spinnerPostion = 0;
		}
		spinnerDataUploadMode.setOnItemSelectedListener(this);
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		String option = parent.getItemAtPosition(position).toString();
		if (option.equals(OPERATIONAL_MODE_APPLICATION)) {
			setOperationalMode("application");
			Toast.makeText(parent.getContext(),
					"Operational Mode : " + option, Toast.LENGTH_SHORT)
					.show();
		} else if (option.equals(OPERATIONAL_MODE_SERVICE)) {
			setOperationalMode("service");
			Toast.makeText(parent.getContext(),
					"Operational Mode : " + option, Toast.LENGTH_SHORT)
					.show();
		} else if (option.equals(DATA_UPLOAD_MODE_WIFI)) {
			setDataUploadMode("wifi");
			Toast.makeText(parent.getContext(),
					"Data Upload Mode : " + option, Toast.LENGTH_SHORT)
					.show();
		} else if (option.equals(DATA_UPLOAD_MODE_WIFI_IF_AVAILABLE)) {
			setDataUploadMode("wifi_if_available");
			Toast.makeText(parent.getContext(),
					"Data Upload Mode : " + option, Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	protected void setOperationalMode(String mode) {
		dbHandler.setOperationalMode(mode);
	}

	protected void setDataUploadMode(String mode) {
		dbHandler.setDataUploadMode(mode);
	}
}
