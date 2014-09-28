package io.systemic.blairbash.dev;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BrowseFragment extends Fragment {
	public static interface OnBrowseFragmentReady {
		public abstract void onBrowseFragmentReady();
	}

	private OnBrowseFragmentReady mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.mListener = (OnBrowseFragmentReady)(activity);
		}
		catch (final ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnBrowseFragmentReady");
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mListener.onBrowseFragmentReady();
	}

	@Override
	public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
		return layoutInflater.inflate(R.layout.fragment_browse, container, false);
	}
}
