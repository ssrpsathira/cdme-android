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
	private Spinner spinner;
	private static final String[] operationalModeChoises = { "Foreground Only",
			"Service on Close" };

	public SettingsViewFragment(Context applicationContext) {
		this.context = applicationContext;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_settings, container,
				false);
		spinner = (Spinner) view.findViewById(R.id.spinner_operational_mode);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				R.layout.custom_spinner_item, operationalModeChoises);

		adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		Toast.makeText(parent.getContext(), 
		        "OnItemSelectedListener : " + parent.getItemAtPosition(position).toString(),
		        Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}
}
