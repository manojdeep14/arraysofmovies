package org.notabug.lifeuser.ArraysOfCinemas.fragment;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.notabug.lifeuser.ArraysOfCinemas.ConfigHelper;
import org.notabug.lifeuser.ArraysOfCinemas.R;
import org.notabug.lifeuser.ArraysOfCinemas.activity.BaseActivity;
import org.notabug.lifeuser.ArraysOfCinemas.adapter.PersonBaseAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class PersonFragment extends Fragment {

    private RecyclerView mPersonGridView;
    private PersonBaseAdapter mPersonAdapter;
    private PersonBaseAdapter mSearchPersonAdapter;
    private ArrayList<JSONObject> mPersonArrayList;
    private ArrayList<JSONObject> mSearchPersonArrayList;
    private GridLayoutManager mGridLayoutManager;
    private AsyncTask mSearchTask;
    private String mSearchQuery;
    private boolean mSearchView;

    // Variables for scroll detection
    private int visibleThreshold = 9; // Three times the amount of items in a row (with three items in a row being the default)
    private int currentPage = 0;
    private int currentSearchPage = 0;
    private int previousTotal = 0;
    private boolean loading = true;
    int pastVisibleItems, visibleItemCount, totalItemCount;

    protected SharedPreferences preferences;
    protected final static String GRID_SIZE_PREFERENCE = "key_grid_size_number";

    public PersonFragment() {
        // Required empty public constructor
    }

    /**
     * Creates a new ListFragment object and returns it.
     * @return the newly created ListFragment object.
     */
    public static PersonFragment newInstance() {
        PersonFragment fragment = new PersonFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createPersonList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_person, container, false);
        showPersonList(fragmentView);
        return fragmentView;
    }

    /**
     * Creates the PersonBaseAdapter with the (still empty) ArrayList.
     * Also starts an AsyncTask to load the items for the empty ArrayList.
     */
    private void createPersonList() {
        mPersonArrayList = new ArrayList<>();

        // Create the adapter
        mPersonAdapter = new PersonBaseAdapter(mPersonArrayList);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        visibleThreshold *= preferences.getInt(GRID_SIZE_PREFERENCE, 3);

        // Get the persons
        new PersonList().execute("1");
    }

    /**
     * Sets up and displays the grid view of people.
     * @param fragmentView the view of the fragment (that the person view will be placed in).
     */
    private void showPersonList(View fragmentView) {
        // RecyclerView to display all the popular persons in a grid.
        mPersonGridView = (RecyclerView) fragmentView.findViewById(R.id.personRecyclerView);

        // Use a GridLayoutManager
        mGridLayoutManager = new GridLayoutManager(getActivity(), 3); // For now three items in a row seems good, might be changed later on.
        mPersonGridView.setLayoutManager(mGridLayoutManager);

        if(mPersonAdapter != null) {
            mPersonGridView.setAdapter(mPersonAdapter);
        }

        mPersonGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0) { // Check for scroll down.
                    visibleItemCount = mGridLayoutManager.getChildCount();
                    totalItemCount = mGridLayoutManager.getItemCount();
                    pastVisibleItems = mGridLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if (totalItemCount > previousTotal) {
                            loading = false;
                            previousTotal = totalItemCount;

                            if (mSearchView) {
                                currentSearchPage++;
                            } else {
                                currentPage++;
                            }
                        }
                    }

                    // When no new pages are being loaded,
                    // but the user is at the end of the list, load the new page.
                    if (!loading && (visibleItemCount + pastVisibleItems + visibleThreshold) >= totalItemCount) {
                        // Load the next page of the content in the background.
                        if (mSearchView) {
                            mSearchTask = new SearchList().execute
                                    (Integer.toString(currentSearchPage + 1), mSearchQuery);
                        } else {
                            new PersonList().execute(Integer.toString(currentPage + 1));
                        }
                        loading = true;
                    }
                }
            }
        });
    }

    /**
     * Creates an empty ArrayList and an adapter based on it.
     * Cancels the previous search task if and starts a new one based on the new query.
     * @param query the name of the person to search for.
     */
    public void search(String query) {
        // Create a PersonBaseAdapter for the search results and load those results.
        mSearchPersonArrayList = new ArrayList<>();
        mSearchPersonAdapter = new PersonBaseAdapter
                (mSearchPersonArrayList);

        // Cancel old AsyncTask if exists.
        currentSearchPage = 1;
        if(mSearchTask != null) {
            mSearchTask.cancel(true);
        }
        mSearchTask = new SearchList().execute("1", query);
    }

    /**
     * Sets search boolean to false and sets original adapter in the RecyclerView.
     */
    public void cancelSearch() {
        // Replace the current list with the personList.
        mSearchView = false;
        mPersonGridView.setAdapter(mPersonAdapter);
    }

    /**
     * Uses AsyncTask to retrieve the list with popular people.
     */
    private class PersonList extends AsyncTask<String, Void, String> {

        private final String API_KEY = ConfigHelper.getConfigValue(
                getActivity().getApplicationContext(), "api_key");
        private int page;

        protected String doInBackground(String... params) {
            page = Integer.parseInt(params[0]);

            String line;
            StringBuilder stringBuilder = new StringBuilder();

            // Load the webpage with the popular persons
            try {
                URL url = new URL("https://api.themoviedb.org/3/person/"
                        + "popular?page=" + page + "&api_key=" + API_KEY
                        + BaseActivity.getLanguageParameter());

                URLConnection urlConnection = url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(
                                    urlConnection.getInputStream()));

                    // Create one long string of the webpage.
                    while((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }

                    // Close the connection and return the data from the webpage.
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
                // Convert the JSON data from the webpage into JSONObjects
                try {
                    JSONObject reader = new JSONObject(response);
                    JSONArray arrayData = reader.getJSONArray("results");
                    for (int i = 0; i < arrayData.length(); i++) {
                        JSONObject websiteData = arrayData.getJSONObject(i);
                        mPersonArrayList.add(websiteData);
                    }

                    if (page == 1 && mPersonGridView != null) {
                        mPersonGridView.setAdapter(mPersonAdapter);
                    } else {
                        // Reload the adapter (with the new page).
                        mPersonAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), getActivity().getResources()
                        .getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Load a list of persons that fulfill the search query.
     */
    private class SearchList extends AsyncTask<String, Void, String> {

        private final String API_KEY = ConfigHelper.getConfigValue
                (getActivity().getApplicationContext(), "api_key");
        private int page;

        protected String doInBackground(String... params) {
            // Extra parameter for the search query.
            page = Integer.parseInt(params[0]);
            String query = params[1];

            String line;
            StringBuilder stringBuilder = new StringBuilder();

            // Load the webpage with the popular persons
            try {
                URL url = new URL("https://api.themoviedb.org/3/search/person?"
                        + "query=" + query + "&page=" + page
                        + "&api_key=" + API_KEY + BaseActivity.getLanguageParameter());

                URLConnection urlConnection = url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(
                                    urlConnection.getInputStream()));

                    // If the user changed the search query, stop loading.
                    if(isCancelled()) {
                        return null;
                    }

                    // Create one long string of the webpage.
                    while((line = bufferedReader.readLine()) != null) {
                        // If the user changed the search query, stop loading.
                        if(isCancelled()) {
                            // There are two because I am not sure
                            // what the best place is for this clause.
                            break;
                        }
                        stringBuilder.append(line).append("\n");
                    }

                    // Close the connection and return the data from the webpage.
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
            // Keep the user at the same position in the list.
            int position;
            try {
                position = mGridLayoutManager.findFirstVisibleItemPosition();
            } catch(NullPointerException npe) {
                position = 0;
            }

            // Clear the ArrayList before adding new movies to it.
            if(currentSearchPage <= 0) {
                // Only if the list doesn't already contain search results.
                mSearchPersonArrayList.clear();
            }

            // Convert the JSON webpage to JSONObjects.
            // Add the JSONObjects to the list with persons.
            if(!(response == null || response.equals(""))) {
                try {
                    JSONObject reader = new JSONObject(response);
                    JSONArray arrayData = reader.getJSONArray("results");
                    for (int i = 0; i < arrayData.length(); i++) {
                        JSONObject websiteData = arrayData.getJSONObject(i);
                        mSearchPersonArrayList.add(websiteData);
                    }

                    // Reload the adapter (with the new page)
                    // and set the user to his old position.
                    mSearchView = true;
                    mPersonGridView.setAdapter(mSearchPersonAdapter);
                    mPersonGridView.scrollToPosition(position);
                } catch(JSONException je) {
                    je.printStackTrace();
                }
            }
        }
    }
}
