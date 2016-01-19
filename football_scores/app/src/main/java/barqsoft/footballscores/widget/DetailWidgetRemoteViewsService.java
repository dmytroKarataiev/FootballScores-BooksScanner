package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

    // these indices must match the projection


    public static final int COL_DATE = 1;
    public static final int COL_MATCHTIME = 2;
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_LEAGUE = 5;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_ID = 8;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_HOME_ID = 10;
    public static final int COL_AWAY_ID = 11;

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
                Date fragmentdate = new Date(System.currentTimeMillis()+(86400000));
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");

                data = getContentResolver().query(DatabaseContract.BASE_CONTENT_URI,
                        null,
                        null,
                        //new String[]{ mformat.format(fragmentdate) },
                        null,
                        null);

                Log.v(LOG_TAG, Boolean.toString(data != null) + " AAAAA!!");

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

                views.setImageViewResource(R.id.home_crest, R.drawable.arsenal);
                views.setImageViewResource(R.id.away_crest, R.drawable.arsenal);

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
//                    setRemoteContentDescription(views, description);
//                }

                String date = data.getString(COL_DATE);
                String league = Utilies.getLeague(data.getInt(COL_LEAGUE));
                String scores = Utilies.getScores(data.getInt(COL_HOME_GOALS), data.getInt(COL_AWAY_GOALS));
                String homeTeam = data.getString(COL_HOME);
                String awayTeam = data.getString(COL_AWAY);


                views.setTextViewText(R.id.data_textview, date);
                views.setTextViewText(R.id.score_textview, scores);
                views.setTextViewText(R.id.league_textview, league);
                views.setTextViewText(R.id.home_name, homeTeam);
                views.setTextViewText(R.id.away_name, awayTeam);

                final Intent fillInIntent = new Intent();

                Uri weatherUri = DatabaseContract.scores_table.buildScoreWithId();
                fillInIntent.setData(weatherUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.home_crest, description);
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
                    return data.getLong(0);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}