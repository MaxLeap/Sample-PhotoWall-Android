package com.maxleap.photowall.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.maxleap.LogInCallback;
import com.maxleap.MLLog;
import com.maxleap.MLUser;
import com.maxleap.MLUserManager;
import com.maxleap.SignUpCallback;
import com.maxleap.exception.MLException;
import com.maxleap.photowall.BaseActivity;
import com.maxleap.photowall.R;

public class LoginActivity extends BaseActivity {

    public static final String TAG = LoginActivity.class.getName();

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private TextInputLayout mEmailTextInputLayout;
    private TextInputLayout mPasswordTextInputLayout;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MLUser user = MLUser.getCurrentUser();
        if (null != user) {
            startActivity(new Intent(LoginActivity.this, PhotoListActivity.class));
            finish();
        }

        setUpToolbar();
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mEmailEditText = (EditText) findViewById(R.id.email_edit_text);
        mEmailTextInputLayout = (TextInputLayout) findViewById(R.id.email_input_layout);
        mPasswordEditText = (EditText) findViewById(R.id.password_edit_text);
        mPasswordTextInputLayout = (TextInputLayout) findViewById(R.id.password_input_layout);
        mEmailTextInputLayout.setErrorEnabled(true);
        mPasswordTextInputLayout.setErrorEnabled(true);

        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (validate()) {
                    MLUserManager.logInInBackground(mEmailEditText.getText().toString(),
                            mPasswordEditText.getText().toString(),
                            new LogInCallback<MLUser>() {
                                @Override
                                public void done(final MLUser mlUser, final MLException e) {
                                    if (e != null) {
                                        MLLog.e(TAG, e);
                                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    startActivity(new Intent(LoginActivity.this, PhotoListActivity.class));
                                    finish();
                                }
                            });
                }
            }
        });

        findViewById(R.id.signup_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (validate()) {
                    MLUser user = new MLUser();
                    user.setUserName(mEmailEditText.getText().toString());
                    user.setPassword(mPasswordEditText.getText().toString());
                    MLUserManager.signUpInBackground(user, new SignUpCallback() {
                        @Override
                        public void done(final MLException e) {
                            if (e != null) {
                                MLLog.e(TAG, e);
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            startActivity(new Intent(LoginActivity.this, PhotoListActivity.class));
                            finish();
                        }
                    });
                }
            }
        });
        findViewById(R.id.reset_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(mContext, ResetPasswordActivity.class));
            }
        });
    }

    private boolean validate() {
        mEmailTextInputLayout.setError("");
        mPasswordTextInputLayout.setError("");
        if (TextUtils.isEmpty(mEmailEditText.getText().toString())) {
            mEmailTextInputLayout.setError(getString(R.string.err_require_email));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(mEmailEditText.getText().toString()).matches()) {
            mEmailTextInputLayout.setError(getString(R.string.err_invalid_email));
            return false;
        }
        if (TextUtils.isEmpty(mPasswordEditText.getText().toString())) {
            mPasswordTextInputLayout.setError(getString(R.string.err_require_password));
            return false;
        }
        return true;
    }
}
