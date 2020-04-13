package org.notabug.lifeuser.ArraysOfCinemas.activity;

import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.notabug.lifeuser.ArraysOfCinemas.R;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

    protected void setNavigationDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    protected void setBackButtons() {
		ActionBar actionBar = getSupportActionBar();
		if(actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeButtonEnabled(true);
		}
	}

	public static String getLanguageParameter() {
		if(!Locale.getDefault().getLanguage().equals("en")) {
			return "&language=" + Locale.getDefault().getLanguage();
		}
		return "";
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
			this.finish();
			return true;
		}

        return super.onOptionsItemSelected(item);
    }

}
