package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    /**
     * Copy/paste note: Replace existing newView() method in ForecastAdapter with this one.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        switch (viewType) {
            case VIEW_TYPE_TODAY:
                layoutId = R.layout.list_item_forecast_today;
                break;
            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.list_item_forecast;
                break;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        view.setTag(new ViewHolder(view));

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        int viewType = getItemViewType(cursor.getPosition());

        ViewHolder holder = (ViewHolder) view.getTag();

        boolean isMetric = Utility.isMetric(mContext);

        String dateStr = Utility.getFriendlyDayString(context, cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
        String forecastStr = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        String highStr = Utility.formatTemperature(context, cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), isMetric);
        String lowStr = Utility.formatTemperature(context, cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), isMetric);
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int resId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY:
                resId = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            case VIEW_TYPE_FUTURE_DAY:
                resId = Utility.getIconResourceForWeatherCondition(weatherId);
                break;
        }

        holder.dateTextView.setText(dateStr);
        holder.forecastTextView.setText(forecastStr);
        holder.highTextView.setText(highStr);
        holder.lowTextView.setText(lowStr);
        holder.iconImageView.setImageResource(resId);

    }

    class ViewHolder {
        @Bind(R.id.list_item_date_textview)
        TextView dateTextView;
        @Bind(R.id.list_item_forecast_textview)
        TextView forecastTextView;
        @Bind(R.id.list_item_high_textview)
        TextView highTextView;
        @Bind(R.id.list_item_low_textview)
        TextView lowTextView;
        @Bind(R.id.list_item_icon)
        ImageView iconImageView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

    }
}