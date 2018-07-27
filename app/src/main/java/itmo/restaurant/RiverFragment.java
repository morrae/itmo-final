package itmo.restaurant;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RiverFragment extends Fragment{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		int position = getArguments().getInt("position");
		String[] rivers = getResources().getStringArray(R.array.rivers);
		View v = inflater.inflate(R.layout.fragment_layout, container, false);
		TextView tv = (TextView) v.findViewById(R.id.tv_content);
		tv.setText(rivers[position]);
		getActivity().getActionBar().setTitle(rivers[position]);
		return v;
	}
}