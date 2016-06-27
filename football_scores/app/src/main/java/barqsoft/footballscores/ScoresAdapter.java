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

package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class ScoresAdapter extends CursorAdapter {

    private final String LOG_TAG = ScoresAdapter.class.getSimpleName();

    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;
    public static final int COL_HOME_ID = 10;
    public static final int COL_AWAY_ID = 11;

    private ViewHolder mHolder;

    public double detail_match_id = 0;
    private String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";

    public ScoresAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        //Log.v(FetchScoreTask.LOG_TAG,"new View inflated");
        return mItem;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        mHolder = (ViewHolder) view.getTag();

        mHolder.home_name.setText(cursor.getString(COL_HOME));
        mHolder.away_name.setText(cursor.getString(COL_AWAY));
        mHolder.date.setText(cursor.getString(COL_MATCHTIME));
        mHolder.score.setText(Utilities.getScores(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)));
        mHolder.match_id = cursor.getDouble(COL_ID);

        // Tried to supply image urls from API, but 99% of images are SVGs there
        // That is why some crests are local, some png will be from urls, majority without images
        String homeUrl = Utilities.getCrestUrl(context, cursor.getInt(COL_LEAGUE), cursor.getInt(COL_HOME_ID));
        String awayUrl = Utilities.getCrestUrl(context, cursor.getInt(COL_LEAGUE), cursor.getInt(COL_AWAY_ID));

        Log.v(LOG_TAG, homeUrl + " " + awayUrl + " " + Utilities.getTeamCrestByTeamName(cursor.getString(COL_HOME)) + " " + cursor.getString(COL_HOME));

        if (Utilities.getTeamCrestByTeamName(cursor.getString(COL_HOME)) != R.drawable.no_icon) {
            mHolder.home_crest.setImageResource(Utilities.getTeamCrestByTeamName(cursor.getString(COL_HOME)));
        } else {
            if (homeUrl != null && homeUrl.length() > 0) {
                if (!homeUrl.contains("svg")) {
                    Picasso.with(context).load(homeUrl).error(R.drawable.no_icon).into(mHolder.home_crest);
                } else {
                    Log.d("ScoresAdapter", homeUrl + " " + Utilities.fixUrlIfSvg(homeUrl));
                    Picasso.with(context).load(Utilities.fixUrlIfSvg(homeUrl)).error(R.drawable.no_icon).into(mHolder.home_crest);
                }
            }

        }

        if (Utilities.getTeamCrestByTeamName(cursor.getString(COL_AWAY)) != R.drawable.no_icon) {
            mHolder.away_crest.setImageResource(Utilities.getTeamCrestByTeamName(cursor.getString(COL_AWAY)));
        } else {
            if (awayUrl != null && awayUrl.length() > 0) {
                if (!awayUrl.contains("svg")) {
                    Picasso.with(context).load(awayUrl).error(R.drawable.no_icon).into(mHolder.away_crest);
                } else {
                    Log.d("ScoresAdapter", awayUrl + " " + Utilities.fixUrlIfSvg(awayUrl));
                    Picasso.with(context).load(Utilities.fixUrlIfSvg(awayUrl)).error(R.drawable.no_icon).into(mHolder.away_crest);
                }
            }
        }

        //Log.v(FetchScoreTask.LOG_TAG,mHolder.home_name.getText() + " Vs. " + mHolder.away_name.getText() +" id " + String.valueOf(mHolder.match_id));
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(detail_match_id));
        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = vi.inflate(R.layout.detail_fragment, null);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);
        if (mHolder.match_id == detail_match_id) {
            //Log.v(FetchScoreTask.LOG_TAG,"will insert extraView");

            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
            match_day.setText(Utilities.getMatchDay(cursor.getInt(COL_MATCHDAY), cursor.getInt(COL_LEAGUE)));

            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(Utilities.getLeague(cursor.getInt(COL_LEAGUE)));

            Button share_button = (Button) v.findViewById(R.id.share_button);
            share_button.setContentDescription(context.getString(R.string.share_text));
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    context.startActivity(createShareIntent(mHolder.home_name.getText() + " "
                            + mHolder.score.getText() + " " + mHolder.away_name.getText() + " "));
                }
            });
        } else {
            container.removeAllViews();
        }

    }

    public Intent createShareIntent(String ShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

}
