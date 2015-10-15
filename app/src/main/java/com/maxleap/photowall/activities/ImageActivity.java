package com.maxleap.photowall.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.maxleap.*;
import com.maxleap.exception.MLException;
import com.maxleap.photowall.BaseActivity;
import com.maxleap.photowall.R;
import com.maxleap.photowall.models.Photo;
import com.maxleap.photowall.utils.Utils;
import com.maxleap.utils.FileHandle;
import com.maxleap.utils.FileHandles;

import java.io.File;
import java.io.IOException;

public class ImageActivity extends BaseActivity {

    public static final String TAG = ImageActivity.class.getName();

    private EditText mTitleEditText;
    private TextInputLayout mTitleTextInputLayout;
    private File mFile;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        setUpToolbar();
        mTitleEditText = (EditText) findViewById(R.id.title_edit_text);
        mTitleTextInputLayout = (TextInputLayout) findViewById(R.id.title_input_layout);
        mTitleTextInputLayout.setErrorEnabled(true);

        ImageView imageView = (ImageView) findViewById(R.id.image);
        try {
            mFile = PhotoListActivity.getImageFile();

            Glide.with(this)
                    .load(mFile)
                    .into(imageView);
        } catch (IOException e) {
            MLLog.e(TAG, e);
        }

        findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (validate()) {
                    if (mFile == null || !mFile.exists()) {
                        Utils.toast(mContext, getString(R.string.err_image_not_exist));
                        return;
                    }

                    FileHandle fileHandle = FileHandles.absolute(mFile);
                    byte[] data = fileHandle.tryReadBytes();
                    if (data == null) {
                        Utils.toast(mContext, getString(R.string.err_image_not_exist));
                        return;
                    }

                    final Photo photo = new Photo();
                    photo.setTitle(mTitleEditText.getText().toString());

                    final MLFile mlFile = new MLFile(photo.getTitle(), data);

                    final ProgressDialog progressDialog = createProgressDialog();
                    progressDialog.show();
                    MLFileManager.saveInBackground(mlFile, new SaveCallback() {
                        @Override
                        public void done(final MLException e) {
                            if (e != null) {
                                progressDialog.dismiss();
                                MLLog.e(TAG, e);
                                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            photo.setAttachment(mlFile);
                            MLGeoPoint location=Utils.getPreviousLocation();
                            if (location != null) {
                                photo.setLocation(location);
                            }
                            MLDataManager.saveInBackground(photo, new SaveCallback() {
                                @Override
                                public void done(final MLException e) {
                                    progressDialog.dismiss();
                                    if (e != null) {
                                        MLLog.e(TAG, e);
                                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    finish();
                                }
                            });
                        }
                    });

                }
            }
        });

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });
    }


    private boolean validate() {
        mTitleTextInputLayout.setError("");
        if (TextUtils.isEmpty(mTitleEditText.getText().toString())) {
            mTitleTextInputLayout.setError(getString(R.string.err_require_title));
            return false;
        }
        return true;
    }

    private ProgressDialog createProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgress(0);
        progressDialog.setMessage("Uploading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        return progressDialog;
    }
}
