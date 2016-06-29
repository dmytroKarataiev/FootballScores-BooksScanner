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

package it.jaschke.alexandria;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import it.jaschke.alexandria.CameraPreview.BarcodeGraphic;
import it.jaschke.alexandria.CameraPreview.CameraSourcePreview;
import it.jaschke.alexandria.CameraPreview.GraphicOverlay;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = AddBook.class.getSimpleName();

    private EditText ean;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT = "eanContent";

    private static final int RC_HANDLE_GMS = 9001;

    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    public AddBook() {
    }

    // Network status to stop fetching the data if the phone is offline
    private boolean isOnline(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }

        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (ean != null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);

        mPreview = (CameraSourcePreview) rootView.findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) rootView.findViewById(R.id.overlay);
        //mPreview.setVisibility(View.GONE);
        //mGraphicOverlay.setVisibility(View.GONE);
        mPreview.setVisibility(View.INVISIBLE);
        mGraphicOverlay.setVisibility(View.INVISIBLE);

        ean = (EditText) rootView.findViewById(R.id.ean);

        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need

            }

            @Override
            public void afterTextChanged(Editable s) {

                // hide Preview and release a camera on text change
                Log.v(LOG_TAG, "afterTextChanged");

                String ean = s.toString();
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }
                if (ean.length() < 13) {
                    clearFields();
                    return;
                }

                if (isOnline(getActivity())) {
                    //Once we have an ISBN, start a book intent
                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, ean);
                    bookIntent.setAction(BookService.FETCH_BOOK);
                    getActivity().startService(bookIntent);
                    AddBook.this.restartLoader();
                } else {
                    Toast toast = Toast.makeText(getActivity(), "Please try again while connected to the internet", Toast.LENGTH_LONG);
                    toast.show();
                }

            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the callback method that the system will invoke when your button is
                // clicked. You might do this by launching another app or by including the
                //functionality directly in this app.
                // Hint: Use a Try/Catch block to handle the Intent dispatch gracefully, if you
                // are using an external app.
                //when you're done, remove the toast below.

                Context context = getActivity();
                CharSequence text = "This button should let you scan a book for its barcode!";
                int duration = Toast.LENGTH_SHORT;

                if (mCameraSource == null && isGooglePlayServicesAvailable()) {
                    BarcodeDetector detector = new BarcodeDetector.Builder(context)
                            .setBarcodeFormats(Barcode.ISBN | Barcode.EAN_13)
                            .build();

                    if (!detector.isOperational()) {
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        return;
                    }

                    detector.setProcessor(
                            new MultiProcessor.Builder<>(new GraphicBarcodeTrackerFactory())
                                    .build());

                    mCameraSource = new CameraSource.Builder(context, detector)
                            .setAutoFocusEnabled(true)
                            .setFacing(CameraSource.CAMERA_FACING_BACK)
                            .setRequestedFps(15.0f)
                            .setRequestedPreviewSize(1600, 900)
                            .build();
                    try {
                        startCameraSource();
                        mCameraSource.start();
                    } catch (IOException e) {
                        Log.e("LOG", "" + e);
                    }
                }
            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ean.setText("");
            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                ean.setText("");
            }
        });

        if (savedInstanceState != null) {
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");
        }

        return rootView;
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (ean.getText().length() == 0) {
            return null;
        }
        String eanStr = ean.getText().toString();
        if (eanStr.length() == 10 && !eanStr.startsWith("978")) {
            eanStr = "978" + eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        if (bookSubTitle != null) {
            ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);
        }

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authors != null) {
            String[] authorsArr = authors.split(",");
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
        }

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
            ImageView imageView = (ImageView) rootView.findViewById(R.id.bookCover);
            Picasso.with(getActivity()).load(imgUrl).into(imageView);
            imageView.setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        if (categories != null) {
            ((TextView) rootView.findViewById(R.id.categories)).setText(categories);
        }

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields() {
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    //==============================================================================================
    // Graphic Barcode Tracker
    //==============================================================================================

    /**
     * Factory for creating a barcode tracker to be associated with a new barcode.  The multiprocessor
     * uses this factory to create barcode trackers as needed -- one for each code.
     */
    private class GraphicBarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {
        @Override
        public Tracker<Barcode> create(Barcode barcode) {
            return new GraphicBarcodeTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicBarcodeTracker extends Tracker<Barcode> {
        private GraphicOverlay mOverlay;
        private BarcodeGraphic mBarcodeGraphic;

        GraphicBarcodeTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mBarcodeGraphic = new BarcodeGraphic(overlay);
        }

        /**
         * Start tracking the detected barcode instance within the barcode overlay.
         */
        @Override
        public void onNewItem(int barcodeId, Barcode item) {
            mBarcodeGraphic.setId(barcodeId);
            final Barcode barcode = item;

            // Send a message to the main thread with a code
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ean.setText(barcode.rawValue);
                    mPreview.setVisibility(View.GONE);
                    mGraphicOverlay.setVisibility(View.GONE);
                }
            });

        }

        /**
         * Update the position/characteristics of the barcode within the overlay.
         */
        @Override
        public void onUpdate(BarcodeDetector.Detections<Barcode> detectionResults, Barcode barcode) {
            mOverlay.add(mBarcodeGraphic);
            mBarcodeGraphic.updateBarcode(barcode);
        }

        /**
         * Hide the graphic when the corresponding barcode was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the barcode was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(BarcodeDetector.Detections<Barcode> detectionResults) {
            mOverlay.remove(mBarcodeGraphic);
        }

        /**
         * Called when the barcode is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mBarcodeGraphic);
        }

    }

    //starting the preview
    private void startCameraSource() {

        try {
            mPreview.setVisibility(View.VISIBLE);
            mGraphicOverlay.setVisibility(View.VISIBLE);
            mPreview.start(mCameraSource, mGraphicOverlay);
        } catch (IOException e) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mPreview.stop();
    }

    private boolean isGooglePlayServicesAvailable() {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getActivity().getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), code, RC_HANDLE_GMS);
            dlg.show();
            return false;
        } else {
            return true;
        }
    }


}
