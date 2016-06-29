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

package it.jaschke.alexandria.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by saj on 22/12/14.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "alexandria.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_BOOK_TABLE = "CREATE TABLE " + AlexandriaContract.BookEntry.TABLE_NAME + " ("+
                AlexandriaContract.BookEntry._ID + " INTEGER PRIMARY KEY," +
                AlexandriaContract.BookEntry.TITLE + " TEXT NOT NULL," +
                AlexandriaContract.BookEntry.SUBTITLE + " TEXT ," +
                AlexandriaContract.BookEntry.DESC + " TEXT ," +
                AlexandriaContract.BookEntry.IMAGE_URL + " TEXT, " +
                "UNIQUE ("+ AlexandriaContract.BookEntry._ID +") ON CONFLICT IGNORE)";

        final String SQL_CREATE_AUTHOR_TABLE = "CREATE TABLE " + AlexandriaContract.AuthorEntry.TABLE_NAME + " ("+
                AlexandriaContract.AuthorEntry._ID + " INTEGER," +
                AlexandriaContract.AuthorEntry.AUTHOR + " TEXT," +
                " FOREIGN KEY (" + AlexandriaContract.AuthorEntry._ID + ") REFERENCES " +
                AlexandriaContract.BookEntry.TABLE_NAME + " (" + AlexandriaContract.BookEntry._ID + "))";

        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " + AlexandriaContract.CategoryEntry.TABLE_NAME + " ("+
                AlexandriaContract.CategoryEntry._ID + " INTEGER," +
                AlexandriaContract.CategoryEntry.CATEGORY + " TEXT," +
                " FOREIGN KEY (" + AlexandriaContract.CategoryEntry._ID + ") REFERENCES " +
                AlexandriaContract.BookEntry.TABLE_NAME + " (" + AlexandriaContract.BookEntry._ID + "))";


        Log.d("sql-statements",SQL_CREATE_BOOK_TABLE);
        Log.d("sql-statements",SQL_CREATE_AUTHOR_TABLE);
        Log.d("sql-statements",SQL_CREATE_CATEGORY_TABLE);

        db.execSQL(SQL_CREATE_BOOK_TABLE);
        db.execSQL(SQL_CREATE_AUTHOR_TABLE);
        db.execSQL(SQL_CREATE_CATEGORY_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
