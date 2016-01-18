package barqsoft.footballscores.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class DatabaseContract
{
    public static final String SCORES_TABLE = "scores_table";
    public static final String TEAMS_TABLE = "teams_table";

    public static final class teams_table implements BaseColumns
    {
        //Table data
        public static final String COL_TEAM_ID = "team_id";
        public static final String COL_TEAM_CREST_PATH = "crest_path";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TEAMS_TABLE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TEAMS_TABLE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TEAMS_TABLE;

        public static Uri buildCrestUri(int id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class scores_table implements BaseColumns
    {
        //Table data
        public static final String LEAGUE_COL = "league";
        public static final String DATE_COL = "date";
        public static final String TIME_COL = "time";
        public static final String HOME_COL = "home";
        public static final String AWAY_COL = "away";
        public static final String HOME_GOALS_COL = "home_goals";
        public static final String AWAY_GOALS_COL = "away_goals";
        public static final String MATCH_ID = "match_id";
        public static final String MATCH_DAY = "match_day";
        public static final String HOME_ID = "homeTeam";
        public static final String AWAY_ID = "awayTeam";
        public static final String HOME_CREST_URL = "homeTeamCrestUrl";
        public static final String AWAY_CREST_URL = "awayTeamCrestUrl";

        //public static Uri SCORES_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH)
                //.build();

        //Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;

        public static Uri buildScoreWithLeague()
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("league").build();
        }
        public static Uri buildScoreWithId()
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("id").build();
        }
        public static Uri buildScoreWithDate()
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("date").build();
        }
    }
    //URI data
    public static final String CONTENT_AUTHORITY = "barqsoft.footballscores";
    public static final String PATH = "scores";
    public static Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);
}
