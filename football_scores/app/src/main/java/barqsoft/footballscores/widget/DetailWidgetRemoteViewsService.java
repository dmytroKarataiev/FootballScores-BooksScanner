package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.R;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] FORECAST_COLUMNS = {
            DatabaseContract.SCORES_TABLE + "." + DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL
    };
    // these indices must match the projection
    static final int INDEX_WEATHER_ID = 0;
    static final int INDEX_DATE = 1;
    static final int INDEX_LEAGUE = 2;
    static final int INDEX_HOME_GOAL = 3;
    static final int INDEX_AWAY_GOAL = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                //String location = Utility.getPreferredLocation(DetailWidgetRemoteViewsService.this);
                //Uri weatherForLocationUri = WeatherContract.WeatherEntry
                //        .buildWeatherLocationWithStartDate(location, System.currentTimeMillis());
                data = getContentResolver().query(DatabaseContract.BASE_CONTENT_URI,
                        FORECAST_COLUMNS,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);
//                int weatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
//                //int weatherArtResourceId = Utility.getIconResourceForWeatherCondition(weatherId);
//                Bitmap weatherArtImage = null;
//
//                String weatherArtResourceUrl = Utility.getArtUrlForWeatherCondition(
//                        DetailWidgetRemoteViewsService.this, weatherId);
//                try {
//                    weatherArtImage = Picasso
//                            .with(DetailWidgetRemoteViewsService.this)
//                            .load(weatherArtResourceUrl)
//                            .error(weatherArtResourceId)
//                            .get();
//
//                } catch (IOException e) {
//                    Log.e(LOG_TAG, "Error retrieving large icon from " + weatherArtResourceUrl, e);
//                }
//
//                String description = data.getString(INDEX_WEATHER_DESC);
//                long dateInMillis = data.getLong(INDEX_WEATHER_DATE);
//                String formattedDate = Utility.getFriendlyDayString(
//                        DetailWidgetRemoteViewsService.this, dateInMillis, false);
//                double maxTemp = data.getDouble(INDEX_WEATHER_MAX_TEMP);
//                double minTemp = data.getDouble(INDEX_WEATHER_MIN_TEMP);
//                String formattedMaxTemperature =
//                        Utility.formatTemperature(DetailWidgetRemoteViewsService.this, maxTemp, Utility.isMetric(getBaseContext()));
//                String formattedMinTemperature =
//                        Utility.formatTemperature(DetailWidgetRemoteViewsService.this, minTemp, Utility.isMetric(getBaseContext()));


                views.setImageViewResource(R.id.widget_icon, R.drawable.arsenal);

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
//                    setRemoteContentDescription(views, description);
//                }

                String date = data.getString(INDEX_DATE);
                int league = data.getInt(INDEX_LEAGUE);
                String homeGoals = data.getString(INDEX_HOME_GOAL);
                String awayGoals = data.getString(INDEX_AWAY_GOAL);


                views.setTextViewText(R.id.widget_date, date);
                views.setTextViewText(R.id.widget_description, Integer.toString(league));
                views.setTextViewText(R.id.widget_high_temperature, homeGoals);
                views.setTextViewText(R.id.widget_low_temperature, awayGoals);

                final Intent fillInIntent = new Intent();
                //String locationSetting =
                //        Utility.getPreferredLocation(DetailWidgetRemoteViewsService.this);
                Uri weatherUri = DatabaseContract.scores_table.buildScoreWithId();
                fillInIntent.setData(weatherUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_icon, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_WEATHER_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}