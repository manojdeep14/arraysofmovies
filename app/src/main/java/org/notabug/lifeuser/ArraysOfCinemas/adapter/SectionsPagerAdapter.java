package org.notabug.lifeuser.ArraysOfCinemas.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import org.notabug.lifeuser.ArraysOfCinemas.R;
import org.notabug.lifeuser.ArraysOfCinemas.fragment.ListFragment;
import org.notabug.lifeuser.ArraysOfCinemas.fragment.PersonFragment;
import org.notabug.lifeuser.ArraysOfCinemas.fragment.ShowFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 3;
    private int currentFragmentPosition;

    private SharedPreferences preferences;
    private final static String HIDE_MOVIES_PREFERENCE = "key_hide_movies_tab";
    private final static String HIDE_SERIES_PREFERENCE = "key_hide_series_tab";
    private final static String HIDE_SAVED_PREFERENCE = "key_hide_saved_tab";
    private final static String HIDE_PERSON_PREFERENCE = "key_hide_person_tab";

    private String movieTabTitle;
    private String seriesTabTitle;
    private String savedTabTitle;
    private String personTabTitle;


    public SectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        NUM_ITEMS = 3;

        if (preferences.getBoolean(HIDE_MOVIES_PREFERENCE, false)) {
            NUM_ITEMS--;
        }

        if (preferences.getBoolean(HIDE_SERIES_PREFERENCE, false)) {
            NUM_ITEMS--;
        }

        if (preferences.getBoolean(HIDE_SAVED_PREFERENCE, false)) {
            NUM_ITEMS--;
        }

        if (preferences.getBoolean(HIDE_PERSON_PREFERENCE, false)) {
            NUM_ITEMS--;
        }

        movieTabTitle = context.getResources().getString(R.string.movie_tab_title);
        seriesTabTitle = context.getResources().getString(R.string.series_tab_title);
        savedTabTitle = context.getResources().getString(R.string.saved_tab_title);
        personTabTitle = context.getResources().getString(R.string.person_tab_title);
    }

    @Override
    public Fragment getItem(int position) {

        switch (getCorrectedPosition(position)) {
            case 0:
                    return ShowFragment.newInstance("movie");
            case 1:
                    return ShowFragment.newInstance("tv");
            case 2:
                    return ListFragment.newInstance();
            case 3:
                    return PersonFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (getCorrectedPosition(position)) {
            case 0:
                    return movieTabTitle;
            case 1:
                    return seriesTabTitle;
            case 2:
                    return savedTabTitle;
            case 3:
                    return personTabTitle;
            default:
                return null;
        }
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        currentFragmentPosition = position;
        super.setPrimaryItem(container, position, object);
    }


    public int getCurrentFragmentPosition() {
        return currentFragmentPosition;
    }


    private int getCorrectedPosition(int position) {
        boolean moviesHidden = preferences.getBoolean(HIDE_MOVIES_PREFERENCE, false);
        boolean seriesHidden = preferences.getBoolean(HIDE_SERIES_PREFERENCE, false);
        boolean savedHidden = preferences.getBoolean(HIDE_SAVED_PREFERENCE, false);
        boolean personHidden = preferences.getBoolean(HIDE_PERSON_PREFERENCE, false);

        switch (position) {
            case 0:
                if (!moviesHidden) {
                    return 0;
                }
            case 1:
                if ((position == 0 || !moviesHidden) && !seriesHidden) {
                    return 1;
                }
            case 2:
                if ((position < 2 || (!moviesHidden && !seriesHidden)) && !savedHidden) {
                    return 2;
                }
            case 3:
                if ((position < 3 || (!moviesHidden && !seriesHidden && !savedHidden))
                        && !personHidden) {
                    return 3;
                }
            default:
                return -1;
        }
    }
}
