package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by sandeep on 23-08-2015.
 */
public class DetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_WEATHER_HUMIDITY = 7;
    static final int COL_WEATHER_WIND_SPEED = 8;
    static final int COL_WEATHER_PRESSURE = 9;
    static final int COL_WEATHER_DEGREE = 10;
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final int FORECAST_DATE_LOADER = 1;
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES
    };
    @Bind(R.id.detail_day_textview)
    TextView dayTextView;
    @Bind(R.id.detail_date_textview)
    TextView dateTextView;
    @Bind(R.id.detail_forecast_textview)
    TextView forecastTextView;
    @Bind(R.id.detail_high_textview)
    TextView highTextView;
    @Bind(R.id.detail_low_textview)
    TextView lowTextView;
    @Bind(R.id.detail_humidity_textview)
    TextView humidityTextView;
    @Bind(R.id.detail_wind_textview)
    TextView windTextView;
    @Bind(R.id.detail_pressure_textview)
    TextView pressureTextView;
    @Bind(R.id.detail_icon)
    ImageView iconImageView;

    private Uri mForecastURI;
    private String mForecastStr;


    public DetailFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_DATE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        ShareActionProvider mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mForecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        // The detail Activity called via intent.  Inspect the intent for forecast data.
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mForecastURI = intent.getData();

            return new CursorLoader(getActivity(),
                    mForecastURI,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    sortOrder);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            populateData(data);
        } else {
            mForecastStr = "";
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastStr = "";
    }

    public void populateData(Cursor cursor) {
        boolean isMetric = Utility.isMetric(getActivity());

        float humidity = cursor.getFloat(COL_WEATHER_HUMIDITY);
        float wind = cursor.getFloat(COL_WEATHER_WIND_SPEED);
        float pressure = cursor.getFloat(COL_WEATHER_PRESSURE);
        float degree = cursor.getFloat(COL_WEATHER_DEGREE);

        long date = cursor.getLong(COL_WEATHER_DATE);
        String dayStr = Utility.getDayName(getActivity(), date);
        String dateStr = Utility.getFormattedMonthDay(getActivity(), date);
        String forecastStr = cursor.getString(COL_WEATHER_DESC);
        String highStr = Utility.formatTemperature(getActivity(), cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String lowStr = Utility.formatTemperature(getActivity(), cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        String humidityStr = getActivity().getString(R.string.format_humidity, humidity);
        String windStr = Utility.getFormattedWind(getActivity(), wind, degree);
        String pressureStr = getActivity().getString(R.string.format_pressure, pressure);
        int weatherId = cursor.getInt(COL_WEATHER_CONDITION_ID);

        dayTextView.setText(dayStr);
        dateTextView.setText(dateStr);
        forecastTextView.setText(forecastStr);
        highTextView.setText(highStr);
        lowTextView.setText(lowStr);
        humidityTextView.setText(humidityStr);
        windTextView.setText(windStr);
        pressureTextView.setText(pressureStr);
        iconImageView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
    }

}

