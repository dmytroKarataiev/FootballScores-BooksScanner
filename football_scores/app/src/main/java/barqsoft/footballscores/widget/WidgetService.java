/*
 * MIT License
 *
 * Copyright (c) 2016. Dmytro Karataiev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * RemoteViewsService controlling the data being shown in the scrollable detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetService extends RemoteViewsService {

    public final String LOG_TAG = WidgetService.class.getSimpleName();

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
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {

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
                final long identityToken = Binder.clearCallingIdentity();

                // Dates between which we will be fetching data
                Date fragmentdate = new Date(System.currentTimeMillis()+(-2 * 86400000));
                Date fragmentdatePlus = new Date(System.currentTimeMillis()+(5 * 86400000));
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");

                data = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate5(),
                        null,
                        null,
                        new String[]{ mformat.format(fragmentdate), mformat.format(fragmentdatePlus) },
                        DatabaseContract.scores_table.DATE_COL + " DESC");

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
            public RemoteViews getViewAt(final int position) {
                if (position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position)) {
                    return null;
                }

                final RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);

                Context context = getApplicationContext();

                String date = data.getString(COL_DATE);
                String league = Utilities.getLeague(data.getInt(COL_LEAGUE));
                String scores = Utilities.getScores(data.getInt(COL_HOME_GOALS), data.getInt(COL_AWAY_GOALS));
                String homeTeam = data.getString(COL_HOME);
                String awayTeam = data.getString(COL_AWAY);

                Log.v(LOG_TAG, date + " " + league + " " + scores + " " + homeTeam + " " + awayTeam);

                // Content Description for each element
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, getString(R.string.app_name) + " " + homeTeam + " vs " + awayTeam);
                }

                views.setTextViewText(R.id.data_textview, date);
                views.setTextViewText(R.id.score_textview, scores);
                views.setTextViewText(R.id.league_textview, league);
                views.setTextViewText(R.id.home_name, homeTeam);
                views.setTextViewText(R.id.away_name, awayTeam);

                String homeUrl = Utilities.getCrestUrl(context, data.getInt(COL_LEAGUE), data.getInt(COL_HOME_ID));
                if (homeUrl != null && homeUrl.length() > 0) {
                    if (homeUrl.contains("svg")) {
                        homeUrl = Utilities.fixUrlIfSvg(homeUrl);
                    }
                    try {
                        Bitmap bitmap = Picasso.with(context).load(homeUrl).get();
                        views.setImageViewBitmap(R.id.home_crest, bitmap);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "e:" + e);
                    }
                } else {
                    views.setImageViewResource(R.id.home_crest, R.drawable.no_icon);
                }

                String awayUrl = Utilities.getCrestUrl(context, data.getInt(COL_LEAGUE), data.getInt(COL_AWAY_ID));
                if (awayUrl != null && awayUrl.length() > 0) {
                    if (awayUrl.contains("svg")) {
                        awayUrl = Utilities.fixUrlIfSvg(awayUrl);
                    }
                    try {
                        Bitmap bitmap = Picasso.with(context).load(awayUrl).get();
                        views.setImageViewBitmap(R.id.away_crest, bitmap);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "e:" + e);
                    }
                } else {
                    views.setImageViewResource(R.id.away_crest, R.drawable.no_icon);
                }

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