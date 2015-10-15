package com.maxleap.photowall.activities;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import com.maxleap.MLLog;
import com.maxleap.MLUserManager;
import com.maxleap.RequestPasswordResetCallback;
import com.maxleap.exception.MLException;
import com.maxleap.photowall.BaseActivity;
import com.maxleap.photowall.R;
import com.maxleap.photowall.utils.Utils;

public class ResetPasswordActivity extends BaseActivity {

    public static final String TAG = ResetPasswordActivity.class.getName();

    private EditText mEmailEditText;
    private TextInputLayout mEmailTextInputLayout;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        setUpToolbar();
        mEmailEditText = (EditText) findViewById(R.id.email_edit_text);
        mEmailTextInputLayout = (TextInputLayout) findViewById(R.id.email_input_layout);
        mEmailTextInputLayout.setErrorEnabled(true);

        findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!validate()) return;

                MLUserManager.requestPasswordResetInBackground(mEmailEditText.getText().toString(), new RequestPasswordResetCallback() {
                    @Override
                    public void done(final MLException e) {
                        if (e != null) {
                            MLLog.e(TAG, e);
                            Utils.toast(ResetPasswordActivity.this, e.getMessage());
                            return;
                        }
                        Utils.toast(ResetPasswordActivity.this, getString(R.string.msg_confirm_email));
                    }
                });

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
        mEmailTextInputLayout.setError("");
        if (TextUtils.isEmpty(mEmailEditText.getText().toString())) {
            mEmailTextInputLayout.setError(getString(R.string.err_require_email));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(mEmailEditText.getText().toString()).matches()) {
            mEmailTextInputLayout.setError(getString(R.string.err_invalid_email));
            return false;
        }
        return true;
    }
}
