package io.systemic.blairbash.dev;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener, BrowseFragment.OnBrowseFragmentReady {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link FragmentPagerAdapter} derivative, which will keep every
	 * loaded fragment in memory. If this becomes too memory intensive, it
	 * may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	AsyncTask<Integer, Void, String> currentFetchTask;

	int currentQuote = 1;

	@Override
	public void onBrowseFragmentReady() {
		fetchQuote();
	}

	public void submitQuote(View view) {
		final String quote = ((EditText)(findViewById(R.id.submit_quote))).getText().toString();
		final String notes = ((EditText)(findViewById(R.id.submit_notes))).getText().toString();
		final String tags = ((EditText)(findViewById(R.id.submit_tags))).getText().toString();

		if (quote.length() != 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Submit Quote?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							new SubmitQuote().execute(quote, notes, tags);
						}
					})
					.setNegativeButton("No", null);
			builder.create().show();
		}
	}

	public void goToQuote(View view) {
		EditText editText = (EditText)(findViewById(R.id.goto_quote));
		String value = editText.getText().toString();
		if (value.length() != 0) {
			currentQuote = Integer.parseInt(value);
			fetchQuote();
		}
	}

	public void upVote(View view) {

	}

	public void downVote(View view) {

	}

	public void nextQuote(View view) {
		currentQuote++;
		fetchQuote();
	}

	public void previousQuote(View view) {
		if (currentQuote > 1) {
			currentQuote--;
			fetchQuote();
		}
	}

	public void fetchQuote() {
		if (currentFetchTask != null)
			currentFetchTask.cancel(true);
		TextView textView = (TextView)(findViewById(R.id.quote_view));
		textView.setText("#" + currentQuote);
		currentFetchTask = new Quote().execute(currentQuote);
	}

	public void showToast(String toast, boolean duration) {
		Toast.makeText(getApplicationContext(), toast, (duration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT)).show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager)(findViewById(R.id.pager));
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(
					actionBar.newTab()
							.setText(mSectionsPagerAdapter.getPageTitle(i))
							.setTabListener(this)
			);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment;

			switch (position) {
				case 0:
					fragment = new BrowseFragment();
					break;
				case 1:
					fragment = new SubmitFragment();
					break;
				default:
					fragment = new Fragment();
					break;
			}

			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
				case 0:
					return getString(R.string.tab_browse).toUpperCase(l);
				case 1:
					return getString(R.string.tab_submit).toUpperCase(l);
			}
			return null;
		}
	}

	class Quote extends AsyncTask<Integer, Void, String> {
		public String text;
		public String notes;
		public String tags;
		public String timestamp;
		public int id;
		public int rating;

		public Quote() {
			text = "";
			notes = "";
			tags = "";
			timestamp = "";
			id = -1;
		}

		public String toString() {
			String string = "";

			if (id != -1) {
				string += ("#" + id + " [" + rating + "] " + timestamp + "\n\n") + (text + "\n");
				if (notes.length() != 0) {
					string += ("\nNotes: " + notes + "\n");
				}
				if (tags.length() != 0) {
					string += ("\nTags: " + tags);
				}
			}

			return string;
		}

		@Override
		protected String doInBackground(Integer... quoteId) {
			try {
				Document document = Jsoup.connect("http://blairbash.org/" + quoteId[0]).get();

				Element info;
				Element quotebox = document.select(".quote").get(0);
				Elements quote = quotebox.select(".quote-body").select("p");
				quote.html(quote.html().replaceAll("(?i)<br[^>]*>", "\u1234"));

				text = quote.text().replaceAll("\u1234 ", "\n");
				id = quoteId[0];
				rating = Integer.parseInt(quotebox.select(".quote-rating").text().replaceAll("−", "-").replaceAll("\\+", ""));
				timestamp = quotebox.select(".quote-date").get(0).text();

				try {
					info = quotebox.select(".quote-notes").get(0);
					info.select(".quote-notes-title").remove();
					notes = info.text();
				}
				catch (Exception e) {
				}

				try {
					info = quotebox.select(".quote-tags").get(0);
					info.select(".quote-tags-title").remove();
					tags = info.text();
				}
				catch (Exception e) {
				}
			}
			catch (Exception e) {
				id = -1;
			}

			return this.toString();
		}

		@Override
		protected void onPostExecute(String result) {
			TextView textView = (TextView)(findViewById(R.id.quote_view));
			if (result.length() != 0)
				textView.setText(result);
			else {
				textView.setText(textView.getText() + "\nCould not fetch quote.");
			}
		}
	}

	class SubmitQuote extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... data) {
			try {
				HttpURLConnection conn = (HttpURLConnection)(new URL("http://blairbash.org/submit").openConnection());

				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setDoInput(true);

				DataOutputStream out = new DataOutputStream(conn.getOutputStream());

				out.writeBytes("quote=" + URLEncoder.encode(data[0], "UTF-8") + "&notes=" + URLEncoder.encode(data[1], "UTF-8") + "&tags=" + URLEncoder.encode(data[2], "UTF-8"));
				out.flush();
				out.close();

				new BufferedReader(new InputStreamReader(conn.getInputStream())).close();

				return true;
			}
			catch (Exception e) {
				return false;
			}

		}

		@Override
		protected void onPostExecute(Boolean success) {
			if (success) {
				showToast("Quote Submitted", false);
			}
			else {
				showToast("Could not submit quote.", false);
			}
		}
	}
}
