package com.maxleap.photowall.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.maxleap.*;
import com.maxleap.exception.MLException;
import com.maxleap.photowall.BaseActivity;
import com.maxleap.photowall.R;

public class LoginActivity extends BaseActivity {

    public static final String TAG = LoginActivity.class.getName();

    private EditText emailEditText;
    private EditText passwordEditText;
    private TextInputLayout emailTextInputLayout;
    private TextInputLayout passwordTextInputLayout;

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
        emailEditText = (EditText) findViewById(R.id.email_edit_text);
        emailTextInputLayout = (TextInputLayout) findViewById(R.id.email_input_layout);
        passwordEditText = (EditText) findViewById(R.id.password_edit_text);
        passwordTextInputLayout = (TextInputLayout) findViewById(R.id.password_input_layout);
        emailTextInputLayout.setErrorEnabled(true);
        passwordTextInputLayout.setErrorEnabled(true);

        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (validate()) {
                    MLUserManager.logInInBackground(emailEditText.getText().toString(),
                            passwordEditText.getText().toString(),
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
                    user.setUserName(emailEditText.getText().toString());
                    user.setPassword(passwordEditText.getText().toString());
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
        emailTextInputLayout.setError("");
        passwordTextInputLayout.setError("");
        if (TextUtils.isEmpty(emailEditText.getText().toString())) {
            emailTextInputLayout.setError(getString(R.string.err_require_email));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches()) {
            emailTextInputLayout.setError(getString(R.string.err_invalid_email));
            return false;
        }
        if (TextUtils.isEmpty(passwordEditText.getText().toString())) {
            passwordTextInputLayout.setError(getString(R.string.err_require_password));
            return false;
        }
        return true;
    }
}
