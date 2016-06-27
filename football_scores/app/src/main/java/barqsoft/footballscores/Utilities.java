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
import android.database.Cursor;
import android.util.Log;

import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilities {

    private final static String LOG_TAG = Utilities.class.getSimpleName();

    public static final int BUNDESLIGA_1 = 394;
    public static final int BUNDESLIGA_2 = 395;
    public static final int LIGUE_1 = 396;
    public static final int LIGUE_2 = 397;
    public static final int PREMIER_LEGAUE = 398;
    public static final int PRIMERA = 399;
    public static final int SEGUNDA = 400;
    public static final int SERIA_A = 401;
    public static final int PRIMEIRA = 402;
    public static final int BUNDESLIGA_3 = 403;
    public static final int EREDIVISIE = 404;
    public static final int CHAMPIONS_LEAGUE = 405;

    public static String getLeague(int league_num) {
        switch (league_num) {
            case BUNDESLIGA_1:
                return "1. Bundesliga 2015/16";
            case BUNDESLIGA_2:
                return "2. Bundesliga 2015/16";
            case LIGUE_1:
                return "Ligue 1 2015/16";
            case LIGUE_2:
                return "Ligue 2 2015/16";
            case PREMIER_LEGAUE:
                return "Premier League 2015/16";
            case PRIMERA:
                return "Primera Division 2015/16";
            case SEGUNDA:
                return "Segunda Division 2015/16";
            case SERIA_A:
                return "Serie A 2015/16";
            case PRIMEIRA:
                return "Primeira Liga 2015/16";
            case BUNDESLIGA_3:
                return "3. Bundesliga 2015/16";
            case EREDIVISIE:
                return "Eredivisie 2015/16";
            case CHAMPIONS_LEAGUE:
                return "Champions League 2015/16";

            default:
                return "Not known League Please report";
        }
    }

    public static String getMatchDay(int match_day, int league_num) {
        if (league_num == CHAMPIONS_LEAGUE) {
            if (match_day <= 6) {
                return "Group Stages, Matchday : 6";
            } else if (match_day == 7 || match_day == 8) {
                return "First Knockout round";
            } else if (match_day == 9 || match_day == 10) {
                return "QuarterFinal";
            } else if (match_day == 11 || match_day == 12) {
                return "SemiFinal";
            } else {
                return "Final";
            }
        } else {
            return "Matchday : " + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals, int awaygoals) {
        if (home_goals < 0 || awaygoals < 0) {
            return " - ";
        } else {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName(String teamname) {
        if (teamname == null) {
            return R.drawable.no_icon;
        }

        Log.v(LOG_TAG, teamname);

        switch (teamname) { //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            case "Arsenal London FC":
                return R.drawable.arsenal;
            case "Manchester United FC":
                return R.drawable.manchester_united;
            case "Swansea City":
                return R.drawable.swansea_city_afc;
            case "Leicester City":
                return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC":
                return R.drawable.everton_fc_logo1;
            case "West Ham United FC":
                return R.drawable.west_ham;
            case "Tottenham Hotspur FC":
                return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion":
                return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC":
                return R.drawable.sunderland;
            case "Stoke City FC":
                return R.drawable.stoke_city;
            case "Bayer Leverkusen":
                return R.drawable.bayer;
            case "Borussia Dortmund":
                return R.drawable.borussia;
            default:
                return R.drawable.no_icon;
        }
    }

    /**
     * Method to get URLs from the database for Crests
     *
     * @param context  from which call is being made
     * @param leagueId id of the League
     * @param teamId   id of the Team
     * @return String URL for the crest from the database
     */
    public static String getCrestUrl(Context context, int leagueId, int teamId) {

        Cursor cursor1 = context
                .getContentResolver()
                .query(DatabaseContract.teams_table.buildScoreWithId(),
                        null,
                        null,
                        new String[]{Integer.toString(leagueId), Integer.toString(teamId)},
                        null);

        if (cursor1 != null && cursor1.moveToFirst()) {
            int INDEX_URL = cursor1.getColumnIndex(DatabaseContract.teams_table.COL_TEAM_CREST_PATH);
            String url = cursor1.getString(INDEX_URL);
            cursor1.close();

            return url;
        }

        if (cursor1 != null) {
            cursor1.close();
        }

        return "";

    }

    /**
     * Conversion documented at https://meta.wikimedia.org/wiki/SVG_image_support
     * Borrowed this ingenious method from rahall4405 gitHub
     * @param UrlString initial wikipedia url to svg image
     * @return fixed link to the png version of the image
     */
    public static String fixUrlIfSvg(String UrlString) {
        String svgName = UrlString.substring(UrlString.lastIndexOf("/")+ 1, UrlString.length());
        int toEndWikipediaInt = UrlString.indexOf("wikipedia/") + 10;

        String toEndWikipedia = UrlString.substring(0,toEndWikipediaInt);
        String fromEndWikipedia = UrlString.substring(toEndWikipediaInt);

        String toStartThumb = fromEndWikipedia.substring(0, fromEndWikipedia.indexOf("/"));
        String partAfterThumb = fromEndWikipedia.substring(fromEndWikipedia.indexOf("/"), fromEndWikipedia.length());
        // 144px was the max size in our  samples,  we will use this
        String lastPart = "/144px-" + svgName + ".png";
        return toEndWikipedia + toStartThumb + "/thumb" + partAfterThumb + lastPart;

    }

}
