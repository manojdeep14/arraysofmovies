package org.notabug.lifeuser.ArraysOfCinemas.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.notabug.lifeuser.ArraysOfCinemas.ConfigHelper;
import org.notabug.lifeuser.ArraysOfCinemas.MovieDatabaseHelper;
import org.notabug.lifeuser.ArraysOfCinemas.NotifyingScrollView;
import org.notabug.lifeuser.ArraysOfCinemas.R;
import org.notabug.lifeuser.ArraysOfCinemas.adapter.CastBaseAdapter;
import org.notabug.lifeuser.ArraysOfCinemas.fragment.ListFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.lang.Runnable;

public class DetailActivity extends BaseActivity {

	private RecyclerView castView;
    private RecyclerView crewView;
	private CastBaseAdapter castAdapter;
    private CastBaseAdapter crewAdapter;
	private ArrayList<JSONObject> castArrayList;
    private ArrayList<JSONObject> crewArrayList;

	private RecyclerView similarMovieView;
	private ArrayList<JSONObject> similarMovieArrayList;

	private Drawable mToolbarBackgroundDrawable;
	private Toolbar toolbar;

	private SQLiteDatabase database;
	private MovieDatabaseHelper databaseHelper;
	private int movieId;
	private String movieImageId;
	private String moviePosterId;
	private String voteAverage;
	private String movieName;
	private Integer totalEpisodes;

	private float amountStars;
	private boolean isMovie = true;

	private ImageView movieImage;
	private TextView movieTitle;
	private ImageView moviePoster;
	private TextView movieGenres;
	private TextView movieStartDate;
	private TextView movieFinishDate;
	private TextView movieRewatched;
	private TextView movieEpisodes;
	private RatingBar movieRating;
	private TextView movieDescription;

	private JSONObject jMovieObject;
	private String genres;

	private String startDate;
	private String finishDate;

	private int defaultButton;

    private Activity mActivity;

	private boolean added = false;

	private SpannableString showTitle;
	private AlphaForegroundColorSpan alphaForegroundColorSpan;

	private SharedPreferences preferences;
	private final static String CAST_VIEW_PREFERENCE = "key_show_cast";
	private final static String CREW_VIEW_PREFERENCE = "key_show_crew";
	private final static String SIMILAR_MOVIE_VIEW_PREFERENCE = "key_show_similar_movies";
	private final static String SHOW_SAVE_DIALOG_PREFERENCE = "key_show_save_dialog";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_detail);
		setNavigationDrawer();
		setBackButtons();

		mToolbarBackgroundDrawable = new ColorDrawable(ContextCompat
				.getColor(getApplicationContext(), R.color.colorPrimary));
		mToolbarBackgroundDrawable.setAlpha(0);
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setBackgroundDrawable(mToolbarBackgroundDrawable);

		NotifyingScrollView notifyingScrollView = (NotifyingScrollView)
				findViewById(R.id.scrollView);
		notifyingScrollView.setOnScrollChangedListener(mOnScrollChangedListener);

		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			mToolbarBackgroundDrawable.setCallback(drawableCallback);
		}

        mActivity = this;

		castView = (RecyclerView) findViewById(R.id.castRecyclerView);
		castView.setHasFixedSize(true);

		LinearLayoutManager castLinearLayoutManager = new LinearLayoutManager(this, 
				LinearLayoutManager.HORIZONTAL, false);
		castView.setLayoutManager(castLinearLayoutManager);



		similarMovieView = (RecyclerView) findViewById(R.id.movieRecyclerView);
		similarMovieView.setHasFixedSize(true);
		LinearLayoutManager movieLinearLayoutManager = new LinearLayoutManager(this,
				LinearLayoutManager.HORIZONTAL, false);
		similarMovieView.setLayoutManager(movieLinearLayoutManager);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		if(!preferences.getBoolean(CAST_VIEW_PREFERENCE, false)) {
			castView.setVisibility(View.GONE);

			TextView castTitle = (TextView) findViewById(R.id.castTitle);
			castTitle.setVisibility(View.GONE);
		}

		if(!preferences.getBoolean(SIMILAR_MOVIE_VIEW_PREFERENCE, false)) {
			similarMovieView.setVisibility(View.GONE);

			TextView similarMovieTitle = (TextView) findViewById(R.id.similarMovieTitle);
			similarMovieTitle.setVisibility(View.GONE);
		}

		movieImage = (ImageView) findViewById(R.id.movieImage);
		movieTitle = (TextView) findViewById(R.id.movieTitle);
		moviePoster = (ImageView) findViewById(R.id.moviePoster);
		movieGenres = (TextView) findViewById(R.id.movieGenres);
		movieStartDate = (TextView) findViewById(R.id.movieStartDate);
		movieFinishDate = (TextView) findViewById(R.id.movieFinishDate);
		movieRewatched = (TextView) findViewById(R.id.movieRewatched);
		movieEpisodes = (TextView) findViewById(R.id.movieEpisodes);
		movieRating = (RatingBar) findViewById(R.id.movieRating);
		movieDescription = (TextView) findViewById(R.id.movieDescription);


		Intent intent = getIntent();
		isMovie = intent.getBooleanExtra("isMovie", true);
		try {
			setMovieData(new JSONObject(intent.getStringExtra("movieObject")));
			jMovieObject = new JSONObject(intent.getStringExtra("movieObject"));

			castArrayList = new ArrayList<JSONObject>();
			castAdapter = new CastBaseAdapter(castArrayList, getApplicationContext());
			castView.setAdapter(castAdapter);

            crewArrayList = new ArrayList<JSONObject>();
            crewAdapter = new CastBaseAdapter(crewArrayList, getApplicationContext());
            crewView.setAdapter(crewAdapter);
		} catch(JSONException e) {
			e.printStackTrace();
		}

		new CastList().execute();
		new MovieDetails().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.save_menu, menu);

		databaseHelper = new MovieDatabaseHelper(getApplicationContext());
		database = databaseHelper.getWritableDatabase();
		databaseHelper.onCreate(database);

		Cursor cursor = database.rawQuery("SELECT * FROM " +
				MovieDatabaseHelper.TABLE_MOVIES +
				" WHERE " + MovieDatabaseHelper.COLUMN_MOVIES_ID +
				"=" + movieId + " LIMIT 1", null);

		if (cursor.getCount() > 0) {
			MenuItem item = menu.findItem(R.id.action_save);
			item.setIcon(R.drawable.ic_star);
			added = true;
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		int id = item.getItemId();

		if (id == R.id.action_save) {
			databaseHelper = new MovieDatabaseHelper(getApplicationContext());
			database = databaseHelper.getWritableDatabase();
			databaseHelper.onCreate(database);
			if (added) {
				database.delete(MovieDatabaseHelper.TABLE_MOVIES,
						MovieDatabaseHelper.COLUMN_MOVIES_ID + "=" + movieId, null);
				added = false;
				item.setIcon(R.drawable.ic_star_border);
				ListFragment.databaseUpdate();
			} else {
				final ContentValues showValues = new ContentValues();

				if (preferences.getBoolean(SHOW_SAVE_DIALOG_PREFERENCE, false)) {

					final AlertDialog.Builder categoriesDialog = new AlertDialog.Builder(this);
					categoriesDialog.setTitle(getString(R.string.category_picker));
					categoriesDialog.setItems(R.array.categories, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							showValues.put(MovieDatabaseHelper.COLUMN_CATEGORIES, getCategoryNumber(which));

							addMovieToDatabase(showValues, item);
						}
					});

					categoriesDialog.show();
				} else {
					showValues.put(MovieDatabaseHelper.COLUMN_CATEGORIES, MovieDatabaseHelper.CATEGORY_WATCHING);
					addMovieToDatabase(showValues, item);
				}
			}
		}

		return super.onOptionsItemSelected(item);
	}

	public static int getCategoryNumber(int index) {
		switch(index) {
			case 0:
				return MovieDatabaseHelper.CATEGORY_WATCHING;
			case 1:
				return MovieDatabaseHelper.CATEGORY_PLAN_TO_WATCH;
			case 2:
				return MovieDatabaseHelper.CATEGORY_WATCHED;
			case 3:
				return MovieDatabaseHelper.CATEGORY_ON_HOLD;
			case 4:
				return MovieDatabaseHelper.CATEGORY_DROPPED;
			default:
				return MovieDatabaseHelper.CATEGORY_WATCHING;
		}
	}


	public static int getCategoryNumber(String category) {
		switch(category) {
			case "watching":
				return MovieDatabaseHelper.CATEGORY_WATCHING;
			case "plan_to_watch":
				return MovieDatabaseHelper.CATEGORY_PLAN_TO_WATCH;
			case "watched":
				return MovieDatabaseHelper.CATEGORY_WATCHED;
			case "on_hold":
				return MovieDatabaseHelper.CATEGORY_ON_HOLD;
			case "dropped":
				return MovieDatabaseHelper.CATEGORY_DROPPED;
			default:
				return MovieDatabaseHelper.CATEGORY_WATCHING;
		}
	}


	private void addMovieToDatabase(ContentValues showValues, MenuItem item) {
		try {
			showValues.put(MovieDatabaseHelper.COLUMN_MOVIES_ID,
					Integer.parseInt(jMovieObject.getString("id")));
			showValues.put(MovieDatabaseHelper.COLUMN_IMAGE,
					jMovieObject.getString("backdrop_path"));
			showValues.put(MovieDatabaseHelper.COLUMN_ICON,
					jMovieObject.getString("poster_path"));
			String title = (isMovie) ? "title" : "name";
			showValues.put(MovieDatabaseHelper.COLUMN_TITLE, jMovieObject.getString(title));
			showValues.put(MovieDatabaseHelper.COLUMN_SUMMARY, jMovieObject.getString("overview"));
			showValues.put(MovieDatabaseHelper.COLUMN_GENRES, genres);
			showValues.put(MovieDatabaseHelper.COLUMN_GENRES_IDS,
					jMovieObject.getString("genre_ids"));
			showValues.put(MovieDatabaseHelper.COLUMN_MOVIE, isMovie);
			showValues.put(MovieDatabaseHelper.COLUMN_RATING,
					jMovieObject.getString("vote_average"));
			String releaseDate = (isMovie) ? "release_date" : "first_air_date";
			showValues.put(MovieDatabaseHelper.COLUMN_RELEASE_DATE,
					jMovieObject.getString(releaseDate));

			database.insert(MovieDatabaseHelper.TABLE_MOVIES, null, showValues);

			added = true;
			item.setIcon(R.drawable.ic_star);
			if (isMovie) {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.movie_added), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.series_added), Toast.LENGTH_SHORT).show();
			}
		} catch (JSONException je) {
			je.printStackTrace();
			Toast.makeText(this, getResources().getString(R.string.show_added_error),
					Toast.LENGTH_SHORT).show();
		}

		ListFragment.databaseUpdate();
	}

	@Override
	public void onBackPressed() {
		LinearLayout editShowDetails = (LinearLayout) findViewById(R.id.editShowDetails);

		if(editShowDetails.getVisibility() != View.GONE) {

			Spinner categoriesView = (Spinner) findViewById(R.id.categories);
			EditText timesWatched = (EditText) findViewById(R.id.timesWatched);
			EditText episodesSeen = (EditText) findViewById(R.id.episodesSeen);
			EditText showRating = (EditText) findViewById(R.id.showRating);

			categoriesView.clearFocus();
			timesWatched.clearFocus();
			episodesSeen.clearFocus();
			showRating.clearFocus();
		}

		setResult(RESULT_CANCELED);
		finish();
	}

	private void setMovieData(JSONObject movieObject) {

		try {
			movieId = Integer.parseInt(movieObject.getString("id"));


			if(movieObject.has("backdrop_path") && 
					!movieObject.getString("backdrop_path").equals(movieImageId)) {
				Picasso.with(this).load("https://image.tmdb.org/t/p/w780" + 
						movieObject.getString("backdrop_path"))
						.into(movieImage);

				Animation animation = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.fade_in);
				movieImage.startAnimation(animation);

				movieImageId = movieObject.getString("backdrop_path");
			}

			if(movieObject.has("poster_path") &&
					!movieObject.getString("poster_path").equals(moviePosterId)) {
				Picasso.with(this).load("https://image.tmdb.org/t/p/w500" +
						movieObject.getString("poster_path"))
						.into(moviePoster);

				moviePosterId = movieObject.getString("poster_path");
			}

			String title = (movieObject.has("title")) ? "title" : "name";

			if(movieObject.has(title) && 
					!movieObject.getString(title).equals(movieTitle
					.getText().toString())) {
				movieTitle.setText(movieObject.getString(title));

				showTitle = new SpannableString(movieObject.getString(title));
				alphaForegroundColorSpan = new AlphaForegroundColorSpan(0xffffffff);
			}

			databaseHelper = new MovieDatabaseHelper(getApplicationContext());
			database = databaseHelper.getWritableDatabase();

			databaseHelper.onCreate(database);

			Cursor cursor = database.rawQuery("SELECT * FROM " +
					MovieDatabaseHelper.TABLE_MOVIES + 
					" WHERE " + MovieDatabaseHelper.COLUMN_MOVIES_ID + 
					"=" + movieId + " LIMIT 1", null);


			if(movieObject.has("overview") &&
					!movieObject.getString("overview").equals(movieDescription
					.getText().toString()) && !movieObject.getString("overview")
					.equals("") && !movieObject.getString("overview").equals("null")) {
				movieDescription.setText(movieObject.getString("overview"));
				if(movieObject.getString("overview").equals("")) {
					new MovieDetails().execute("true");
				}
			}

			cursor.close();
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}
	private NotifyingScrollView.OnScrollChangedListener 
			mOnScrollChangedListener = new NotifyingScrollView
			.OnScrollChangedListener() {
		public void onScrollChanged(ScrollView scrollView,
				int l, int t, int oldl, int oldt) {
			final int headerHeight = findViewById(R.id.movieImage).getHeight() - 
					toolbar.getHeight();
			final float ratio = (float) Math.min(Math.max(t, 0),
					headerHeight) / headerHeight;
			final int newAlpha = (int) (ratio * 255);
			mToolbarBackgroundDrawable.setAlpha(newAlpha);

			alphaForegroundColorSpan.setAlpha(256 - newAlpha);
			showTitle.setSpan(alphaForegroundColorSpan, 0, showTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			getSupportActionBar().setTitle(showTitle);
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

	private class CastList extends AsyncTask<String, Void, String> {

		private final String API_KEY = ConfigHelper.getConfigValue(
				getApplicationContext(), "api_key");

		protected String doInBackground(String ... params) {
			String line;
			StringBuilder stringBuilder = new StringBuilder();

			String movie = (isMovie) ? "movie" : "tv";
			
			try {
				URL url = new URL("https://api.themoviedb.org/3/" + movie + "/" + 
						movieId + "/credits?api_key=" + API_KEY + 
						getLanguageParameter());
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
								mActivity.findViewById(R.id.castTitle);
						View view = mActivity.findViewById(R.id.secondDivider);

						textView.setVisibility(View.GONE);
						view.setVisibility(View.GONE);
						castView.setVisibility(View.GONE);
					} else {
						JSONArray castArray = reader.getJSONArray("cast");

						for (int i = 0; i < castArray.length(); i++) {
							JSONObject castData = castArray.getJSONObject(i);
							castArrayList.add(castData);
						}
						castAdapter = new CastBaseAdapter(castArrayList,
								getApplicationContext());

						castView.setAdapter(castAdapter);
					}

					if (reader.getJSONArray("crew").length() <= 0) {

						TextView textView = (TextView)
								mActivity.findViewById(R.id.crewTitle);
						View view = mActivity.findViewById(R.id.thirdDivider);

						textView.setVisibility(View.GONE);
						view.setVisibility(View.GONE);
						crewView.setVisibility(View.GONE);
					} else {
						JSONArray crewArray = reader.getJSONArray("crew");

						for (int i = 0; i < crewArray.length(); i++) {
							JSONObject crewData = crewArray.getJSONObject(i);


							crewData.put("character", crewData.getString("job"));

							crewArrayList.add(crewData);
						}

						crewAdapter = new CastBaseAdapter(crewArrayList,
								getApplicationContext());

						crewView.setAdapter(crewAdapter);
					}
				} catch (JSONException je) {
					je.printStackTrace();
				}
			} else {
				Toast.makeText(getApplicationContext(), getApplicationContext().getResources()
						.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class MovieDetails extends AsyncTask<String, Void, String> {

		private final String API_KEY = ConfigHelper.getConfigValue(
				getApplicationContext(), "api_key");
		private boolean missingOverview;

		protected String doInBackground(String... params) {
			if(params.length > 0) {
				missingOverview = params[0].equalsIgnoreCase("true");
			}

			String line;
			StringBuilder stringBuilder = new StringBuilder();

			try {
				String type = (isMovie) ? "movie" : "tv";
				URL url;
				if(missingOverview) {
					url = new URL("https://api.themoviedb.org/3/" + type + 
							"/" + movieId + "?api_key=" + API_KEY);
				} else {
					url = new URL("https://api.themoviedb.org/3/" + type + 
							"/" + movieId + "?api_key=" + API_KEY 
							+ getLanguageParameter());
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
					JSONObject movieData = new JSONObject(response);

					if (missingOverview) {
						movieDescription.setText(movieData.getString("overview"));
					} else {
						setMovieData(movieData);
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

	@SuppressLint("ParcelCreator")
	public class AlphaForegroundColorSpan extends ForegroundColorSpan {
		private float mAlpha;

		public AlphaForegroundColorSpan(int color) {
			super(color);
		}

		public AlphaForegroundColorSpan(Parcel src) {
			super(src);
			mAlpha = src.readFloat();
		}

		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeFloat(mAlpha);
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setColor(getAlphaColor());
		}

		public void setAlpha(float alpha) {
			mAlpha = alpha;
		}

		public float getAlpha() {
			return mAlpha;
		}

		private int getAlphaColor() {
			int foregroundColor = getForegroundColor();
			return Color.argb((int) (mAlpha * 255), Color.red(foregroundColor), Color.green(foregroundColor), Color.blue(foregroundColor));
		}
	}
}
