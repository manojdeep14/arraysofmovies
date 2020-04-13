package org.notabug.lifeuser.ArraysOfCinemas.activity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.notabug.lifeuser.ArraysOfCinemas.MovieDatabaseHelper;
import org.notabug.lifeuser.ArraysOfCinemas.R;
import org.notabug.lifeuser.ArraysOfCinemas.adapter.SectionsPagerAdapter;
import org.notabug.lifeuser.ArraysOfCinemas.fragment.ListFragment;
import org.notabug.lifeuser.ArraysOfCinemas.fragment.PersonFragment;
import org.notabug.lifeuser.ArraysOfCinemas.fragment.ShowFragment;

import java.io.File;


public class MainActivity extends BaseActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;


    private ViewPager mViewPager;

    private final static int SETTINGS_REQUEST_CODE = 0001;
    private final static int REQUEST_CODE_ASK_PERMISSIONS_EXPORT = 123;
    private final static int REQUEST_CODE_ASK_PERMISSIONS_IMPORT = 124;

    protected MenuItem mSearchAction;
    protected MenuItem mFilterAction;
    protected boolean isSearchOpened = false;
    protected EditText editSearch;

    private SharedPreferences preferences;
    private final static String LIVE_SEARCH_PREFERENCE = "key_live_search";

    private final static String REWATCHED_FIELD_CHANGE_PREFERENCE = "key_rewatched_field_change";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);


        if (!preferences.getBoolean(REWATCHED_FIELD_CHANGE_PREFERENCE, false)) {
            File dbFile = getDatabasePath(MovieDatabaseHelper.getDatabaseFileName());

            if (dbFile.exists()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.watched_upgrade_dialog_message))
                       .setTitle(getString(R.string.watched_upgrade_dialog_title));

                builder.setPositiveButton(getString(R.string.watched_upgrade_dialog_positive),
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        MovieDatabaseHelper databaseHelper
                                = new MovieDatabaseHelper(getApplicationContext());
                        SQLiteDatabase database = databaseHelper.getWritableDatabase();
                        databaseHelper.onCreate(database);

                        Cursor cursor = database.rawQuery("SELECT * FROM " +
                                databaseHelper.TABLE_MOVIES, null);

                        cursor.moveToFirst();
                        while (!cursor.isAfterLast()) {
                            if (!cursor.isNull(cursor.getColumnIndex(MovieDatabaseHelper.COLUMN_PERSONAL_REWATCHED))) {
                                int rewatchedValue = cursor.getInt(cursor.getColumnIndex
                                        (MovieDatabaseHelper.COLUMN_PERSONAL_REWATCHED));
                                // In case of a value of zero, check if the show is watched.
                                if ((rewatchedValue == 0 && cursor.getInt(cursor.getColumnIndex(
                                        MovieDatabaseHelper.COLUMN_CATEGORIES)) == 1) || rewatchedValue != 0) {
                                    ContentValues watchedValues = new ContentValues();
                                    watchedValues.put(MovieDatabaseHelper.COLUMN_PERSONAL_REWATCHED,
                                            (rewatchedValue + 1));
                                    database.update(MovieDatabaseHelper.TABLE_MOVIES, watchedValues,
                                            MovieDatabaseHelper.COLUMN_MOVIES_ID + "="
                                                    + cursor.getInt(cursor.getColumnIndex
                                                    (MovieDatabaseHelper.COLUMN_MOVIES_ID)), null);
                                }
                            }
                            cursor.moveToNext();
                        }

                        database.close();
                    }
                });

                builder.setNegativeButton(getString(R.string.watched_upgrade_dialog_negative), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                builder.show();

                preferences.edit().putBoolean(REWATCHED_FIELD_CHANGE_PREFERENCE, true).commit();
            }
        }
    }

    @Override
    public void onBackPressed() {

        if(isSearchOpened) {
            handleMenuSearch();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_search) {
            handleMenuSearch();
            return true;
        }

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivityForResult(intent, SETTINGS_REQUEST_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment mCurrentFragment = getSupportFragmentManager().getFragments()
                .get(mSectionsPagerAdapter.getCurrentFragmentPosition());
        mCurrentFragment.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchAction = menu.findItem(R.id.action_search);
        mFilterAction = menu.findItem(R.id.action_filter);
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS_EXPORT:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    MovieDatabaseHelper databaseHelper = new MovieDatabaseHelper(getApplicationContext());
                    databaseHelper.exportDatabase(getApplicationContext());
                } else {
                }
                break;
            case REQUEST_CODE_ASK_PERMISSIONS_IMPORT:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MovieDatabaseHelper databaseHelper = new MovieDatabaseHelper(getApplicationContext());
                    databaseHelper.importDatabase(getApplicationContext());
                } else {
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    protected void handleMenuSearch() {
        ActionBar action = getSupportActionBar();

        final boolean liveSearch = preferences.getBoolean(LIVE_SEARCH_PREFERENCE, true);

        if(isSearchOpened) {
            if(editSearch.getText().toString().equals("")) {
                action.setDisplayShowCustomEnabled(true);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);

                mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_search));

                isSearchOpened = false;

                action.setCustomView(null);
                action.setDisplayShowTitleEnabled(true);

                cancelSearchInFragment();
            } else {
                editSearch.setText("");
            }
        } else {
            action.setDisplayShowCustomEnabled(true);
            action.setCustomView(R.layout.search_bar);
            action.setDisplayShowTitleEnabled(false);

            editSearch = (EditText) action.getCustomView().findViewById(R.id.editSearch);

            editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                        searchInFragment(editSearch.getText().toString());
                        return true;
                    }
                    return false;
                }
            });

            editSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (liveSearch) {
                        searchInFragment(editSearch.getText().toString());
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });

            editSearch.requestFocus();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT);

            mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_close));

            isSearchOpened = true;
        }
    }


    private void searchInFragment(String query) {
        @SuppressLint("RestrictedApi") Fragment mCurrentFragment = getSupportFragmentManager()
                .getFragments().get(mSectionsPagerAdapter.getCurrentFragmentPosition());

        if(mCurrentFragment != null) {
            if (mCurrentFragment instanceof ShowFragment) {
                ((ShowFragment) mCurrentFragment).search(query);
            }

            if (mCurrentFragment instanceof ListFragment) {
                ((ListFragment) mCurrentFragment).search(query);
            }

            if (mCurrentFragment instanceof PersonFragment) {
                ((PersonFragment) mCurrentFragment).search(query);
            }
        }
    }


    private void cancelSearchInFragment() {
        @SuppressLint("RestrictedApi") Fragment mCurrentFragment = getSupportFragmentManager()
                .getFragments().get(mSectionsPagerAdapter.getCurrentFragmentPosition());

        if(mCurrentFragment != null) {
            if (mCurrentFragment instanceof ShowFragment) {
                ((ShowFragment) mCurrentFragment).cancelSearch();
            }

            if (mCurrentFragment instanceof ListFragment) {
                ((ListFragment) mCurrentFragment).cancelSearch();
            }

            if (mCurrentFragment instanceof PersonFragment) {
                ((PersonFragment) mCurrentFragment).cancelSearch();
            }
        }
    }
}
