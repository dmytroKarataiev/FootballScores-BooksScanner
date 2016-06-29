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

package it.jaschke.alexandria.api;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;

/**
 * Created by saj on 11/01/15.
 */
public class BookListAdapter extends CursorAdapter {

    private static int sLoaderID;

    public static class ViewHolder {
        public final ImageView bookCover;
        public final TextView bookTitle;
        public final TextView bookSubTitle;

        public ViewHolder(View view) {
            bookCover = (ImageView) view.findViewById(R.id.fullBookCover);
            bookTitle = (TextView) view.findViewById(R.id.listBookTitle);
            bookSubTitle = (TextView) view.findViewById(R.id.listBookSubTitle);
        }
    }

    public BookListAdapter(Context context, Cursor c, int flags, int loaderId) {
        super(context, c, flags);
        sLoaderID = loaderId;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String imgUrl = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            Picasso.with(context).load(imgUrl).into(viewHolder.bookCover);
        }

        String bookTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        viewHolder.bookTitle.setText(bookTitle);

        String bookSubTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        viewHolder.bookSubTitle.setText(bookSubTitle);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.book_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }
}
