package org.notabug.lifeuser.ArraysOfCinemas.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apmem.tools.layouts.FlowLayout;
import org.notabug.lifeuser.ArraysOfCinemas.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class FilterActivity extends AppCompatActivity {

    ArrayList withGenres = new ArrayList();
    ArrayList withoutGenres = new ArrayList();

    public static final String FILTER_PREFERENCES = "filter_preferences";
    public static final String FILTER_SORT = "filter_sort";
    public static final String FILTER_CATEGORIES = "filter_categories";
    public static final String FILTER_DATES = "filter_dates";
    public static final String FILTER_START_DATE = "filter_start_date";
    public static final String FILTER_END_DATE = "filter_end_date";
    public static final String FILTER_WITH_GENRES = "filter_with_genres";
    public static final String FILTER_WITHOUT_GENRES = "filter_without_genres";
    public static final String FILTER_WITH_KEYWORDS = "filter_with_keywords";
    public static final String FILTER_WITHOUT_KEYWORDS = "filter_without_keywords";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        Intent intent = getIntent();

        if (intent.getBooleanExtra("categories", false)) {
            LinearLayout categoriesLayout = (LinearLayout) findViewById(R.id.categoriesLayout);
            categoriesLayout.setVisibility(View.VISIBLE);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("GenreList", Context.MODE_PRIVATE);
        String stringGenreArray = sharedPreferences.getString("genreJSONArrayList", null);

        if (stringGenreArray != null) {
            try {
                FlowLayout flowLayout = (FlowLayout) findViewById(R.id.genreButtons);
                JSONArray genreArray = new JSONArray(stringGenreArray);
                for (int i = 0; i < genreArray.length(); i++) {
                    JSONObject genre = genreArray.getJSONObject(i);

                    Button button = new Button(this);
                    button.setText(genre.getString("name"));
                    button.setId(Integer.parseInt(genre.getString("id")));
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Button genreButton = (Button) v;
                            int buttonId = genreButton.getId();

                            if(withGenres.contains(buttonId)) {
                                withGenres.remove((Integer) buttonId);
                                withoutGenres.add(buttonId);

                                genreButton.getBackground().setColorFilter(getResources().getColor(R.color.colorRed), PorterDuff.Mode.SRC_ATOP);
                                genreButton.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_close), null, null, null);
                            } else if(withoutGenres.contains(buttonId)) {
                                withoutGenres.remove((Integer) buttonId);

                                genreButton.getBackground().clearColorFilter();
                                genreButton.setCompoundDrawablesWithIntrinsicBounds(null,
                                        null, null, null);
                                genreButton.setTextColor(Color.BLACK);
                            } else {
                                withGenres.add(buttonId);

                                genreButton.getBackground().setColorFilter(getResources().getColor(R.color.colorGreen), PorterDuff.Mode.SRC_ATOP);
                                genreButton.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_check), null, null, null);
                                genreButton.setTextColor(Color.WHITE);
                            }
                        }
                    });
                    flowLayout.addView(button);
                }
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }


    }




    private String getSelectedCheckBox(CheckBox... checkBoxes) {
        for(CheckBox checkBox : checkBoxes) {
            if(checkBox.isChecked()) {
                return checkBox.getTag().toString();
            }
        }

        return null;
    }

    private ArrayList<String> getSelectedCheckBoxes(CheckBox... checkBoxes) {
        ArrayList<String> checkBoxArray = new ArrayList<String>();
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                checkBoxArray.add(checkBox.getTag().toString());
            }
        }

        return checkBoxArray;
    }


    private String getSelectedRadioButton(RadioGroup radioGroup) {
        if (radioGroup.getCheckedRadioButtonId() != -1) {
            int id = radioGroup.getCheckedRadioButtonId();
            RadioButton radioButton = (RadioButton) radioGroup.findViewById(id);
            return radioButton.getTag().toString();
        } else {
            return null;
        }
    }


    private void selectRadioButtonByTag(String tag, RadioGroup radioGroup) {
        int count = radioGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
            if (radioButton.getTag().toString().equals(tag)) {
                radioGroup.check(radioButton.getId());
            }
        }
    }


    private void selectCheckBoxByTag(ArrayList<String> arrayList, ViewGroup parent) {
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            CheckBox checkBox = (CheckBox) parent.getChildAt(i);
            if (arrayList.contains(checkBox.getTag().toString())) {
                checkBox.setChecked(true);
            }
        }
    }
    public void checkBoxSelected(View view) {
        CheckBox theaterCheckBox = (CheckBox) findViewById(R.id.theaterCheckBox);
        CheckBox twoDatesCheckBox = (CheckBox) findViewById(R.id.twoDatesCheckBox);

        TableLayout tableLayout = (TableLayout) findViewById(R.id.dateDetailsLayout);
        if(view.getId() == twoDatesCheckBox.getId()) {
            if(twoDatesCheckBox.isChecked()) {
                if(theaterCheckBox.isChecked()) {
                    theaterCheckBox.setChecked(false);
                }

                tableLayout.setVisibility(View.VISIBLE);
            } else {
                tableLayout.setVisibility(View.GONE);
            }
        } else {
            if(twoDatesCheckBox.isChecked()) {
                twoDatesCheckBox.setChecked(false);

                tableLayout.setVisibility(View.GONE);
            }
        }
    }



    private void setDatePickerDate(DatePicker datePicker, String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(simpleDateFormat.parse(date));
            datePicker.init(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH), null);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public void collapseAdvanced(View view) {
        final RelativeLayout advancedView = (RelativeLayout) findViewById(R.id.advancedView);
        ImageView collapseIcon = (ImageView) findViewById(R.id.collapseIcon);

        if (advancedView.getVisibility() == View.GONE) {
            expandAnimation(advancedView);
            collapseIcon.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
        } else {
            collapseAnimation(advancedView);
            collapseIcon.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
        }
    }


    private void collapseAnimation(final View view) {
        final int initialHeight = view.getMeasuredHeight();

        Animation collapse = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        collapse.setDuration((int) (initialHeight / view.getContext().getResources().getDisplayMetrics().density));
        view.startAnimation(collapse);
    }


    private void expandAnimation(final View view) {
        view.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();

        view.getLayoutParams().height = 1;
        view.setVisibility(View.VISIBLE);

        Animation expand = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
                    scrollView.scrollTo(0, scrollView.getHeight());
                } else {
                    view.getLayoutParams().height = (int) (targetHeight * interpolatedTime);
                }

                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        expand.setDuration((int) (targetHeight / view.getContext().getResources().getDisplayMetrics().density));
        view.startAnimation(expand);
    }
}
