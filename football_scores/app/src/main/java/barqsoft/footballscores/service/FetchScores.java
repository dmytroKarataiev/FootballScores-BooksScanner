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

package barqsoft.footballscores.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.BuildConfig;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class FetchScores extends JobService {

    public static final String LOG_TAG = FetchScores.class.getSimpleName();
    public static final String ACTION_DATA_UPDATE = "barqsoft.footballscores.app.ACTION_DATA_UPDATED";

    private static final int NOTIFICATION_ID = 3004;

    final String[] LEAGUES = {"394", "395", "396", "397", "398", "399", "400", "401", "402", "403", "404", "405", "424"};

    @Override
    public boolean onStartJob(JobParameters params) {

        getData("n7");
        getData("p7");

        // Adding crest urls only once
        for (String league : LEAGUES) {
            // To prevent excessive calls to the API we check if we have teams in the db and links to their crests
            Cursor cursor = getApplicationContext()
                    .getContentResolver()
                    .query(DatabaseContract.teams_table.CONTENT_URI,
                            null,
                            DatabaseContract.teams_table.COL_LEAGUE_ID + " = ?",
                            new String[]{league},
                            null);

            if (cursor != null && !cursor.moveToFirst()) {
                getCrestUrl(league);
            } else {
                Cursor cursor1 = getApplicationContext().getContentResolver().query(DatabaseContract.teams_table.CONTENT_URI,
                        null,
                        DatabaseContract.teams_table.COL_LEAGUE_ID + " = ?",
                        new String[]{league},
                        null);
                cursor1.moveToFirst();

                while (!cursor1.isLast()) {
                    cursor1.moveToNext();
                }
                cursor1.close();
            }

            if (cursor != null) {
                cursor.close();
            }
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private void getData(String timeFrame) {
        MyFetchService myFetchService = new MyFetchService();
        myFetchService.execute(timeFrame);
    }

    private void processJSONdata(String JSONdata, Context mContext, boolean isReal) {
        Log.d("FetchScores", JSONdata);
        //JSON data
        // This set of league codes is for the 2015/2016 season. In fall of 2016, they will need to
        // be updated. Feel free to use the codes
        final String BUNDESLIGA1 = "394";
        final String BUNDESLIGA2 = "395";
        final String LIGUE1 = "396";
        final String LIGUE2 = "397";
        final String PREMIER_LEAGUE = "398";
        final String PRIMERA_DIVISION = "399";
        final String SEGUNDA_DIVISION = "400";
        final String SERIE_A = "401";
        final String PRIMERA_LIGA = "402";
        final String Bundesliga3 = "403";
        final String EREDIVISIE = "404";
        final String CHAMPIONS_LEAGUE = "405";


        final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
        final String TEAM_LINK = "http://api.football-data.org/alpha/teams/";
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";
        final String HOME_ID = "homeTeam";
        final String AWAY_ID = "awayTeam";
        final String CREST_URL = "crestUrl";

        //Match data
        String League = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String Away = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;
        String home_id = null;
        String away_id = null;
        String home_crest_url = null;
        String away_crest_url = null;

        try {
            JSONArray matches = new JSONObject(JSONdata).getJSONArray(FIXTURES);
            Log.d("FetchScores", "matches.length():" + matches.length());

            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector<>(matches.length());
            for (int i = 0; i < matches.length(); i++) {

                JSONObject match_data = matches.getJSONObject(i);
                League = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString("href");
                League = League.replace(SEASON_LINK, "");

                home_id = match_data.getJSONObject(LINKS).getJSONObject(HOME_ID).getString("href");
                home_id = home_id.replace(TEAM_LINK, "");

                away_id = match_data.getJSONObject(LINKS).getJSONObject(AWAY_ID).getString("href");
                away_id = away_id.replace(TEAM_LINK, "");

                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.

                match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                        getString("href");
                match_id = match_id.replace(MATCH_LINK, "");
                if (!isReal) {
                    //This if statement changes the match ID of the dummy data so that it all goes into the database
                    match_id = match_id + Integer.toString(i);
                }

                mDate = match_data.getString(MATCH_DATE);
                mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                mDate = mDate.substring(0, mDate.indexOf("T"));
                SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                match_date.setTimeZone(TimeZone.getTimeZone("UTC"));

                try {
                    Date parseddate = match_date.parse(mDate + mTime);
                    SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                    new_date.setTimeZone(TimeZone.getDefault());
                    mDate = new_date.format(parseddate);
                    mTime = mDate.substring(mDate.indexOf(":") + 1);
                    mDate = mDate.substring(0, mDate.indexOf(":"));

                    if (!isReal) {
                        //This if statement changes the dummy data's date to match our current date range.
                        Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
                        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                        mDate = mformat.format(fragmentdate);
                    }
                } catch (Exception e) {
                    Log.d(LOG_TAG, "error here!");
                    Log.e(LOG_TAG, e.getMessage());
                }
                Home = match_data.getString(HOME_TEAM);
                Away = match_data.getString(AWAY_TEAM);
                Home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                Away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                match_day = match_data.getString(MATCH_DAY);
                ContentValues match_values = new ContentValues();
                match_values.put(DatabaseContract.scores_table.MATCH_ID, match_id);
                match_values.put(DatabaseContract.scores_table.DATE_COL, mDate);
                match_values.put(DatabaseContract.scores_table.TIME_COL, mTime);
                match_values.put(DatabaseContract.scores_table.HOME_COL, Home);
                match_values.put(DatabaseContract.scores_table.AWAY_COL, Away);
                match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL, Home_goals);
                match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL, Away_goals);
                match_values.put(DatabaseContract.scores_table.LEAGUE_COL, League);
                match_values.put(DatabaseContract.scores_table.HOME_ID, home_id);
                match_values.put(DatabaseContract.scores_table.AWAY_ID, away_id);
                match_values.put(DatabaseContract.scores_table.MATCH_DAY, match_day);

                values.add(match_values);
            }

            int inserted_data = 0;
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            inserted_data = mContext.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI, insert_data);
            Log.d("FetchScores", "inserted_data:" + inserted_data);
            // Update widgets data
            updateWidgets();

            // Schedule a notification to the user
            notifyUser(Integer.toString(inserted_data));

            //Log.v(LOG_TAG,"Succesfully Inserted : " + String.valueOf(inserted_data));
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

    }

    /**
     * Method to get JSON with teams for each league
     * and then parse for crest urls and put in the team_table
     *
     * @param leagueId id of the league
     */
    private void getCrestUrl(String leagueId) {

        FetchCrests myFetchService = new FetchCrests();
        myFetchService.execute(leagueId);

    }

    /**
     * Method to make a notification
     */
    public void notifyUser(String info) {
        Context context = getApplicationContext();
        Resources resources = context.getResources();

        //build your notification here.
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setColor(resources.getColor(R.color.green01))
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(getString(R.string.updated) + " " + info)
                .setContentText(getString(R.string.updated_descr))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.updated_descr)))
                .setAutoCancel(true);

        // Intent ti open the app
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        //stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * Send intent to update connected widgets
     */
    public void updateWidgets() {
        // Send intent to the Widget to notify that the data was updated
        Intent dataUpdated = new Intent(ACTION_DATA_UPDATE)
                // Ensures that only components in the app will receive the broadcast
                .setPackage(getApplicationContext().getPackageName());
        getApplicationContext().sendBroadcast(dataUpdated);
    }

    public class MyFetchService extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {

            //Creating fetch URL
            final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL
            final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days
            //final String QUERY_MATCH_DAY = "matchday";
            final String API_PARAM = "X-Auth-Token";
            final String API_KEY = BuildConfig.FOOTBALL_API_KEY;

            Uri fetch_build = Uri.parse(BASE_URL)
                    .buildUpon()
                    .appendQueryParameter(QUERY_TIME_FRAME, params[0])
                    .build();
            String JSON_data = null;

            OkHttpClient client = new OkHttpClient();

            try {
                URL fetch = new URL(fetch_build.toString());
                Request request = new Request.Builder()
                        .url(fetch)
                        .addHeader(API_PARAM, API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                JSON_data = response.body().string();
                Log.d("MyFetchService", JSON_data);
                response.body().close();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception here" + e.getMessage());
            }

            try {
                if (JSON_data != null) {
                    //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                    JSONArray matches = new JSONObject(JSON_data).getJSONArray("fixtures");
                    if (matches.length() == 0) {
                        //if there is no data, call the function on dummy data
                        //this is expected behavior during the off season.
                        //processJSONdata(getString(R.string.dummy_data), getApplicationContext(), false);
                        return null;
                    }

                    processJSONdata(JSON_data, getApplicationContext(), true);
                } else {
                    //Could not Connect
                    Log.d(LOG_TAG, "Could not connect to server.");
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            return null;
        }
    }

    private class FetchCrests extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            String leagueId = params[0];

            final String API_PARAM = "X-Auth-Token";
            final String API_KEY = BuildConfig.FOOTBALL_API_KEY;
            String jsonUrl = "http://api.football-data.org/alpha/soccerseasons/" + leagueId + "/teams";

            Uri fetchCrest = Uri.parse(jsonUrl)
                    .buildUpon()
                    .build();

            String JSON_data = null;

            OkHttpClient client = new OkHttpClient();

            try {
                URL fetch = new URL(fetchCrest.toString());
                // Log.v(LOG_TAG, fetch.toString());

                Request request = new Request.Builder()
                        .url(fetch)
                        .addHeader(API_PARAM, API_KEY)
                        .build();
                Response response = client.newCall(request).execute();
                JSON_data = response.body().string();

                // Log.v(LOG_TAG, JSON_data);

                response.body().close();

                final String TEAMS = "teams";

                // For each team
                final String FULLNAME = "name";
                final String NAME = "shortName";
                final String CREST_URL = "crestUrl";
                final String LINKS = "_links";
                final String SELF = "self";
                final String HREF = "href";
                final String TEAM_LINK = "http://api.football-data.org/alpha/teams/";

                JSONArray teams = new JSONObject(JSON_data).getJSONArray(TEAMS);

                Vector<ContentValues> values = new Vector<>(teams.length());
                for (int i = 0, n = teams.length(); i < n; i++) {
                    JSONObject team = teams.getJSONObject(i);

                    String fullName = team.getString(FULLNAME);
                    String name = team.getString(NAME);
                    String url = team.getString(CREST_URL);
                    String teamId = team.getJSONObject(LINKS).getJSONObject(SELF).getString(HREF);
                    teamId = teamId.replace(TEAM_LINK, "");

                    ContentValues team_values = new ContentValues();
                    team_values.put(DatabaseContract.teams_table.COL_TEAM_ID, teamId);
                    team_values.put(DatabaseContract.teams_table.COL_TEAM_FULLNAME, fullName);
                    team_values.put(DatabaseContract.teams_table.COL_TEAM_NAME, name);
                    team_values.put(DatabaseContract.teams_table.COL_TEAM_CREST_PATH, url);
                    team_values.put(DatabaseContract.teams_table.COL_LEAGUE_ID, leagueId);

                    values.add(team_values);

                }

                int inserted_data = 0;
                ContentValues[] insert_data = new ContentValues[values.size()];
                values.toArray(insert_data);
                inserted_data = getApplicationContext().getContentResolver().bulkInsert(
                        DatabaseContract.teams_table.CONTENT_URI, insert_data);

                Log.v(LOG_TAG, "Succesfully Inserted : " + String.valueOf(inserted_data));

            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception here " + e.getMessage());
            }

            return null;
        }
    }
}

