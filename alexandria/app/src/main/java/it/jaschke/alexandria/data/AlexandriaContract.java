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

/**
 * Created by saj on 22/12/14.
 */

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class AlexandriaContract{

    public static final String CONTENT_AUTHORITY = "it.jaschke.alexandria";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_BOOKS = "books";
    public static final String PATH_AUTHORS = "authors";
    public static final String PATH_CATEGORIES = "categories";

    public static final String PATH_FULLBOOK = "fullbook";

    public static final class BookEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).build();

        public static final Uri FULL_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FULLBOOK).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        public static final String TABLE_NAME = "books";

        public static final String TITLE = "title";

        public static final String IMAGE_URL = "imgurl";

        public static final String SUBTITLE = "subtitle";

        public static final String DESC = "description";

        public static Uri buildBookUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildFullBookUri(long id) {
            return ContentUris.withAppendedId(FULL_CONTENT_URI, id);
        }

    }

    public static final class AuthorEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_AUTHORS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_AUTHORS;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_AUTHORS;

        public static final String TABLE_NAME = "authors";

        public static final String AUTHOR = "author";

        public static Uri buildAuthorUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class CategoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORIES).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORIES;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORIES;

        public static final String TABLE_NAME = "categories";

        public static final String CATEGORY = "category";

        public static Uri buildCategoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}