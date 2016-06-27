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

package barqsoft.footballscores.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Scores.db";
    private static final int DATABASE_VERSION = 5;

    public ScoresDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CreateScoresTable = "CREATE TABLE " + DatabaseContract.SCORES_TABLE + " ("
                + DatabaseContract.scores_table._ID + " INTEGER PRIMARY KEY,"
                + DatabaseContract.scores_table.DATE_COL + " TEXT NOT NULL,"
                + DatabaseContract.scores_table.TIME_COL + " INTEGER NOT NULL,"
                + DatabaseContract.scores_table.HOME_COL + " TEXT NOT NULL,"
                + DatabaseContract.scores_table.AWAY_COL + " TEXT NOT NULL,"
                + DatabaseContract.scores_table.LEAGUE_COL + " INTEGER NOT NULL,"
                + DatabaseContract.scores_table.HOME_GOALS_COL + " TEXT NOT NULL,"
                + DatabaseContract.scores_table.AWAY_GOALS_COL + " TEXT NOT NULL,"
                + DatabaseContract.scores_table.MATCH_ID + " INTEGER NOT NULL,"
                + DatabaseContract.scores_table.MATCH_DAY + " INTEGER NOT NULL,"
                + DatabaseContract.scores_table.HOME_ID + " INTEGER NOT NULL,"
                + DatabaseContract.scores_table.AWAY_ID + " INTEGER NOT NULL,"
                + DatabaseContract.scores_table.HOME_CREST_URL + " TEXT,"
                + DatabaseContract.scores_table.AWAY_CREST_URL + " TEXT,"
                + " UNIQUE (" + DatabaseContract.scores_table.MATCH_ID + ") ON CONFLICT REPLACE"
                + " );";
        db.execSQL(CreateScoresTable);

        final String CreateTeamsTable = "CREATE TABLE " + DatabaseContract.TEAMS_TABLE + " ("
                + DatabaseContract.teams_table._ID + " INTEGER PRIMARY KEY,"
                + DatabaseContract.teams_table.COL_TEAM_ID + " INTEGER NOT NULL,"
                + DatabaseContract.teams_table.COL_TEAM_FULLNAME + " TEXT NOT NULL,"
                + DatabaseContract.teams_table.COL_TEAM_NAME + " TEXT NOT NULL,"
                + DatabaseContract.teams_table.COL_TEAM_CREST_PATH + " TEXT,"
                + DatabaseContract.teams_table.COL_LEAGUE_ID + " INTEGER NOT NULL" + ");";

        db.execSQL(CreateTeamsTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Remove old values when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.SCORES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.TEAMS_TABLE);
        // If we drop the table on update we have to create it again
        onCreate(db);
    }
}
