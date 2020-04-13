package org.notabug.lifeuser.ArraysOfCinemas.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.notabug.lifeuser.ArraysOfCinemas.ConfigHelper;
import org.notabug.lifeuser.ArraysOfCinemas.R;
import org.notabug.lifeuser.ArraysOfCinemas.activity.BaseActivity;
import org.notabug.lifeuser.ArraysOfCinemas.adapter.ShowBaseAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class contains some basic functionality that would
 * otherwise be duplicated in multiple fragments.
 */
public class BaseFragment extends Fragment {

    protected RecyclerView mShowView;
    protected ShowBaseAdapter mShowAdapter;
    protected ShowBaseAdapter mSearchShowAdapter;
    protected ArrayList<JSONObject> mShowArrayList;
    protected ArrayList<JSONObject> mSearchShowArrayList;
    protected LinearLayoutManager mShowLinearLayoutManager;
    protected HashMap<String, String> mShowGenreList;

    protected boolean mSearchView;

    protected SharedPreferences preferences;
    protected final static String SHOWS_LIST_PREFERENCE = "key_show_shows_grid";
    protected final static String GRID_SIZE_PREFERENCE = "key_grid_size_number";

    protected final static int FILTER_REQUEST_CODE = 0002;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.filter_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Sets up and displays the grid or list view of shows.
     * @param fragmentView the view of the fragment (that the show view will be placed in).
     */
    protected void showShowList(View fragmentView) {
        mShowView = (RecyclerView) fragmentView.findViewById(R.id.showRecyclerView);

        // Set the layout of the RecyclerView.
        if(preferences.getBoolean(SHOWS_LIST_PREFERENCE, true)) {
            // If the user changed from a list layout to a grid layout, reload the ShowBaseAdapter.
            if(!(mShowView.getLayoutManager() instanceof GridLayoutManager)) {
                mShowAdapter = new ShowBaseAdapter(mShowArrayList, mShowGenreList,
                        preferences.getBoolean(SHOWS_LIST_PREFERENCE, true));
            }
            GridLayoutManager mShowGridView = new GridLayoutManager(getActivity(),
                    preferences.getInt(GRID_SIZE_PREFERENCE, 3));
            mShowView.setLayoutManager(mShowGridView);
            mShowLinearLayoutManager = mShowGridView;
        } else {
            // If the user changed from a list layout to a grid layout, reload the ShowBaseAdapter.
            if(!(mShowView.getLayoutManager() instanceof LinearLayoutManager)) {
                mShowAdapter = new ShowBaseAdapter(mShowArrayList, mShowGenreList,
                        preferences.getBoolean(SHOWS_LIST_PREFERENCE, true));
            }
            mShowLinearLayoutManager = new LinearLayoutManager(getActivity(),
                    LinearLayoutManager.VERTICAL, false);
            mShowView.setLayoutManager(mShowLinearLayoutManager);
        }

        if(mShowAdapter != null) {
            mShowView.setAdapter(mShowAdapter);
        }
    }

    /**
     * Sets the search view and adapter back to normal.
     */
    public void cancelSearch() {
        mSearchView = false;
        mShowView.setAdapter(mShowAdapter);
    }

    /**
     * Uses AsyncTask to retrieve the id to genre mapping.
     */
    protected class GenreList extends AsyncTask<String, Void, String> {

        private final String API_KEY = ConfigHelper.getConfigValue(
                getActivity().getApplicationContext(), "api_key");

        protected String doInBackground(String... params) {
            String genreType = params[0];

            String line;
            StringBuilder stringBuilder = new StringBuilder();

            // Load the genre webpage.
            try {
                URL url = new URL("https://api.themoviedb.org/3/genre/"
                        + genreType + "/list?api_key=" +
                        API_KEY + BaseActivity.getLanguageParameter());

                URLConnection urlConnection = url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(
                                    urlConnection.getInputStream()));

                    // Create one long string of the webpage.
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }

                    // Close connection and return the data from the webpage.
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

            // Loading the dataset failed, return null.
            return null;
        }

        protected void onPostExecute(String response) {

            if (response != null && !response.isEmpty()) {
                // Save GenreList to sharedPreferences, this way it can be used anywhere.
                SharedPreferences sharedPreferences = getActivity().getApplicationContext()
                        .getSharedPreferences("GenreList", Context.MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor =
                        sharedPreferences.edit();

                // Convert the JSON data from the webpage to JSONObjects in an ArrayList.
                try {
                    JSONObject reader = new JSONObject(response);
                    JSONArray genreArray = reader.getJSONArray("genres");
                    for (int i = 0; genreArray.optJSONObject(i) != null; i++) {
                        JSONObject websiteData = genreArray.getJSONObject(i);
                        mShowGenreList.put(websiteData.getString("id"),
                                websiteData.getString("name"));

                        // Temporary fix until I find a way to handle this efficiently.
                        prefsEditor.putString(websiteData.getString("id"),
                                websiteData.getString("name"));
                        prefsEditor.commit();
                    }

                    prefsEditor.putString("genreJSONArrayList", genreArray.toString());
                    prefsEditor.commit();

                    mShowAdapter.notifyDataSetChanged();
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), getActivity().getResources()
                        .getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
