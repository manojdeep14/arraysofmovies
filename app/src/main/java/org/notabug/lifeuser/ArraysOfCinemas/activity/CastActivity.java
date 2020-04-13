package org.notabug.lifeuser.ArraysOfCinemas.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.notabug.lifeuser.ArraysOfCinemas.ConfigHelper;
import org.notabug.lifeuser.ArraysOfCinemas.NotifyingScrollView;
import org.notabug.lifeuser.ArraysOfCinemas.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;

public class CastActivity extends BaseActivity {

	private RecyclerView castMovieView;
	private ArrayList<JSONObject> castMovieArrayList;

	private RecyclerView crewMovieView;
	private ArrayList<JSONObject> crewMovieArrayList;

	private Drawable mToolbarBackgroundDrawable;
	private Toolbar toolbar;

	private int actorId;
	private String actorImageId;

	private ImageView actorImage;
	private TextView actorName;
	private ImageView actorIcon;
	private TextView actorPlaceOfBirth;
	private TextView actorBirthday;
	private TextView actorBiography;

	private Activity mActivity;

	private SharedPreferences collapseViewPreferences;
	private SharedPreferences.Editor collapseViewEditor;
	private final static String COLLAPSE_VIEW = "collapseView";
	private final static String CAST_MOVIE_VIEW_PREFERENCE = "CastActivity.castMovieView";
	private final static String CREW_MOVIE_VIEW_PREFERENCE = "CastActivity.crewMovieView";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_cast);
		setNavigationDrawer();
		setBackButtons();


		mToolbarBackgroundDrawable = new ColorDrawable(ContextCompat
				.getColor(getApplicationContext(), R.color.colorPrimary));
		mToolbarBackgroundDrawable.setAlpha(0);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setBackgroundDrawable(mToolbarBackgroundDrawable);


		NotifyingScrollView notifyingScrollView = (NotifyingScrollView)
				findViewById(R.id.castScrollView);
		notifyingScrollView.setOnScrollChangedListener(mOnScrollChangedListener);

		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			mToolbarBackgroundDrawable.setCallback(drawableCallback);
		}

        mActivity = this;

		castMovieView = (RecyclerView) findViewById(R.id.castMovieRecyclerView);
		castMovieView.setHasFixedSize(true);

		crewMovieView = (RecyclerView) findViewById(R.id.crewMovieRecyclerView);
		crewMovieView.setHasFixedSize(true);

		LinearLayoutManager castLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		castMovieView.setLayoutManager(castLinearLayoutManager);

        crewMovieView = (RecyclerView) findViewById(R.id.crewMovieRecyclerView);
        crewMovieView.setHasFixedSize(true);

        LinearLayoutManager crewLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        crewMovieView.setLayoutManager(crewLinearLayoutManager);

		collapseViewPreferences = getApplicationContext()
				.getSharedPreferences(COLLAPSE_VIEW, Context.MODE_PRIVATE);

		if(collapseViewPreferences.getBoolean(CAST_MOVIE_VIEW_PREFERENCE, false)) {
			castMovieView.setVisibility(View.GONE);
		}

		if(collapseViewPreferences.getBoolean(CREW_MOVIE_VIEW_PREFERENCE, false)) {
			crewMovieView.setVisibility(View.GONE);
		}

		Intent intent = getIntent();
		try {
			setActorData(new JSONObject(intent.getStringExtra(("actorObject"))));

			castMovieArrayList = new ArrayList<JSONObject>();
					getApplicationContext();


            crewMovieArrayList = new ArrayList<JSONObject>();

		} catch(JSONException e) {
			e.printStackTrace();
		}

		new ActorMovieList().execute();

		new ActorDetails().execute();

		TextView castMovieTitle = (TextView) findViewById(R.id.castMovieTitle);
		TextView crewMovieTitle = (TextView) findViewById(R.id.crewMovieTitle);

		collapseViewEditor = collapseViewPreferences.edit();


		setTitleClickListener(castMovieTitle, castMovieView, CAST_MOVIE_VIEW_PREFERENCE);
		setTitleClickListener(crewMovieTitle, crewMovieView, CREW_MOVIE_VIEW_PREFERENCE);
	}


	private void setTitleClickListener(final TextView title, final RecyclerView view, final String preference) {
		title.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(collapseViewPreferences.getBoolean(preference, false)) {

					view.setVisibility(View.VISIBLE);


					collapseViewEditor.putBoolean(preference, false);
				} else {

					view.setVisibility(View.GONE);


					collapseViewEditor.putBoolean(preference, true);
				}

				collapseViewEditor.commit();
			}
		});
	}


	private void setActorData(JSONObject actorObject) {

		actorImage = (ImageView) findViewById(R.id.actorImage);
		actorName = (TextView) findViewById(R.id.actorName);
		actorIcon = (ImageView) findViewById(R.id.actorIcon);
		actorPlaceOfBirth = (TextView) findViewById(R.id.actorPlaceOfBirth);
		actorBirthday = (TextView) findViewById(R.id.actorBirthday);
		actorBiography = (TextView) findViewById(R.id.actorBiography);

		try {

			if(actorObject.has("id")) {
				actorId = Integer.parseInt(actorObject.getString("id"));
			}


			if(actorObject.has("profile_path") &&
					!actorObject.getString("profile_path").equals(actorImageId)) {

				Picasso.with(this).load("https://image.tmdb.org/t/p/h632" +
						actorObject.getString("profile_path"))
						.into(actorImage);
				Picasso.with(this).load("https://image.tmdb.org/t/p/w154" +
						actorObject.getString("profile_path"))
						.into(actorIcon);

				actorImageId = actorObject.getString("profile_path");
			}

			if(actorObject.has("name") && !actorObject.getString("name").equals(actorName.getText().toString())) {
				actorName.setText(actorObject.getString("name"));
			}

			if(actorObject.has("place_of_birth") && !actorObject.getString("place_of_birth").equals(actorPlaceOfBirth
					.getText().toString())) {
				actorPlaceOfBirth.setText(getString(R.string.place_of_birth) + actorObject.getString("place_of_birth"));
			}

			if(actorObject.has("birthday") && !actorObject.getString("birthday").equals(actorBirthday
					.getText().toString())) {
				actorBirthday.setText(getString(R.string.birthday) + actorObject.getString("birthday"));
			}

			if(actorObject.has("biography") &&
					!actorObject.getString("biography").equals(
							actorBiography.getText().toString())) {
				actorBiography.setText(actorObject.getString("biography"));
			}
			if(actorObject.getString("biography").equals("")) {
				new ActorDetails().execute("true");
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}

	private NotifyingScrollView.OnScrollChangedListener
			mOnScrollChangedListener = new NotifyingScrollView
			.OnScrollChangedListener() {
		public void onScrollChanged(ScrollView scrollView, int l, int t, int oldl, int oldt) {
			final int headerHeight = findViewById(R.id.actorImage).getHeight() -
					toolbar.getHeight();
			final float ratio = (float) Math.min(Math.max(t, 0), headerHeight) / headerHeight;
			final int newAlpha = (int) (ratio * 255);
			mToolbarBackgroundDrawable.setAlpha(newAlpha);
		}
	};

	private Drawable.Callback drawableCallback = new Drawable.Callback() {
		@Override
		public void invalidateDrawable(Drawable drawable) {
			toolbar.setBackgroundDrawable(drawable);
		}

		@Override
		public void scheduleDrawable(Drawable drawable, Runnable runnable, long when) {}

		@Override
		public void unscheduleDrawable(Drawable drawable, Runnable runnable) {}
	};

	private class ActorMovieList extends AsyncTask<String, Void, String> {

		private final String API_KEY = ConfigHelper.getConfigValue(getApplicationContext(), "api_key");

		protected String doInBackground(String... params) {
			String line;
			StringBuilder stringBuilder = new StringBuilder();

			try {
				URL url = new URL("https://api.themoviedb.org/3/person/" +
						actorId +"/combined_credits?api_key=" + API_KEY + getLanguageParameter());
				URLConnection urlConnection = url.openConnection();

				try {
					BufferedReader bufferedReader = new BufferedReader(
							new InputStreamReader(
									urlConnection.getInputStream()));

					while((line = bufferedReader.readLine()) != null) {
						stringBuilder.append(line).append("\n");
					}

					bufferedReader.close();
					return stringBuilder.toString();
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			} catch(MalformedURLException mue) {
				mue.printStackTrace();
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}

			return null;
		}

		protected void onPostExecute(String response) {
			if (response != null && !response.isEmpty()) {
				try {
					JSONObject reader = new JSONObject(response);

					if (reader.getJSONArray("cast").length() <= 0) {

						TextView textView = (TextView)
								mActivity.findViewById(R.id.castMovieTitle);
						View view = mActivity.findViewById(R.id.secondDivider);

						textView.setVisibility(View.GONE);
						view.setVisibility(View.GONE);
						castMovieView.setVisibility(View.GONE);
					} else {
						JSONArray castMovieArray = reader.getJSONArray("cast");
						for(int i = 0; i < castMovieArray.length(); i++) {
							JSONObject actorMovies = castMovieArray.getJSONObject(i);
							castMovieArrayList.add(actorMovies);
						}


					}

					if (reader.getJSONArray("crew").length() <= 0) {

						TextView textView = (TextView)
								mActivity.findViewById(R.id.crewMovieTitle);
						View view = mActivity.findViewById(R.id.thirdDivider);

						textView.setVisibility(View.GONE);
						view.setVisibility(View.GONE);
						crewMovieView.setVisibility(View.GONE);
					} else {
						JSONArray crewMovieArray = reader.getJSONArray("crew");
						for(int i = 0; i < crewMovieArray.length(); i++) {
							JSONObject crewMovies = crewMovieArray.getJSONObject(i);


							crewMovieArrayList.add(crewMovies);
						}

					}
				} catch(JSONException je) {
					je.printStackTrace();
				}
			} else {
					Toast.makeText(getApplicationContext(), getApplicationContext().getResources()
							.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
			}
		}
	}


	private class ActorDetails extends AsyncTask<String, Void, String> {

		private final String API_KEY = ConfigHelper.getConfigValue(getApplicationContext(), "api_key");
		private boolean missingOverview;

		protected String doInBackground(String... params) {
			if (params.length > 0) {
				missingOverview = params[0].equalsIgnoreCase("true");
			}

			String line;
			StringBuilder stringBuilder = new StringBuilder();

			try {
				URL url;
				if (missingOverview) {
					url = new URL("https://api.themoviedb.org/3/person/" +
							actorId + "?api_key=" + API_KEY);
				} else {
					url = new URL("https://api.themoviedb.org/3/person/" +
							actorId + "?api_key=" + API_KEY + getLanguageParameter());
				}
				URLConnection urlConnection = url.openConnection();

				try {
					BufferedReader bufferedReader = new BufferedReader(
							new InputStreamReader(
									urlConnection.getInputStream()));

					while((line = bufferedReader.readLine()) != null) {
						stringBuilder.append(line).append("\n");
					}

					bufferedReader.close();
					return stringBuilder.toString();
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			} catch(MalformedURLException mue) {
				mue.printStackTrace();
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}

			return null;
		}

		protected void onPostExecute(String response) {
			if (response != null && !response.isEmpty()) {
				try {
					JSONObject actorData = new JSONObject(response);
					if (missingOverview && actorData.has("biography") && !actorData.getString("biography").equals("")) {
						actorBiography.setText(actorData.getString("biography"));
					} else if (missingOverview && (actorData.optString("biography") == null
							|| (actorData.has("biography") && actorData.get("biography").equals("")))) {
						actorBiography.setText(getString(R.string.no_biography)
								+ actorData.getString("name") + ".");
					} else {
						setActorData(actorData);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(getApplicationContext(), getApplicationContext().getResources()
						.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
			}
		}
	}
}
