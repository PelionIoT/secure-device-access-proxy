// ----------------------------------------------------------------------------
//   The confidential and proprietary information contained in this file may
//   only be used by a person authorized under and to the extent permitted
//   by a subsisting licensing agreement from ARM Limited or its affiliates.
//
//          (C)COPYRIGHT 2018 ARM Limited or its affiliates.
//              ALL RIGHTS RESERVED
//
//   This entire notice must be reproduced on all copies of this file
//   and copies of this file may only be made by a person if such person is
//   permitted to do so under the terms of a subsisting license agreement
//   from ARM Limited or its affiliates.
// ----------------------------------------------------------------------------
package com.arm.armsda.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.arm.armsda.R;
import com.arm.armsda.data.ApiGwLoginDetails;
import com.arm.armsda.data.ApplicationData;
import com.arm.armsda.data.IDataHandler;
import com.arm.armsda.data.SharedPreferencesHandleData;
import com.arm.armsda.serial.DeviceConnection;
import com.arm.armsda.settings.ActionBarDrawerActivity;
import com.arm.armsda.utils.AndroidUtils;
import com.arm.mbed.sda.proxysdk.ProxyException;
import com.arm.mbed.sda.proxysdk.SecuredDeviceAccess;
import com.arm.mbed.sda.proxysdk.http.HttpErrorResponseException;
import com.arm.mbed.sda.proxysdk.server.UserPasswordServer;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends ActionBarDrawerActivity implements LoaderCallbacks<Cursor> {

    //TODO:: FIX - when attempting to login without network connected.

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    //Login details
    private static String accountId;
    private static String baseUrl;
    private static final String sharedPreferencesKeyValue = "apiGwLoginDetails";
    private static IDataHandler dataHandler = new SharedPreferencesHandleData();
    //TODO:: i dont want to pass interface to another activity so i am using the class itself, I need it here for login and on the other for get access token
    private static UserPasswordServer authServer;
    private ApiGwLoginDetails apiGwLoginDetails;

    private DeviceConnection dv = new DeviceConnection();
    private static final String appDataSharedPreferencesKeyValue = "appDataDetails";
    private ApplicationData applicationData;
    private static final String DEFAULT_DEMO_MODE = ApplicationData.HANNOVER_MESSE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting Drawer
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_login, null, false);
        mDrawerLayout.addView(contentView, 0);

        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Set up the login form.
        mEmailUsernameView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        /* Set Permissions */
        verifyStoragePermissions(this);

        /* Send filepath to SDK so it could initialize the keystore */
        String androidPath = this.getFilesDir().toString();
        SecuredDeviceAccess.setKeyStorePath(androidPath);

        loadLoginDetailsIfExist();

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onResume() {
        dv.registerDevice(this);
        initializeAppConfiguration();
        super.onResume();
    }

    @Override
    protected void onStop() {
        dv.unregisterDevice(this);
        super.onStop();
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private void initializeAppConfiguration() {

        JSONObject jsonStored =  dataHandler.getJsonStringData(
                appDataSharedPreferencesKeyValue,
                LoginActivity.this);

        if (null != jsonStored) {
            applicationData = new ApplicationData(jsonStored);
            String demo_mode = ((applicationData.getDemoMode() != null) ?
                    applicationData.getDemoMode() : DEFAULT_DEMO_MODE);
            baseUrl = ((applicationData.getCloudUrl() != null) ?
                    applicationData.getCloudUrl() : getString(R.string.env_url));
            accountId = ((applicationData.getAccountId() != null) ?
                    applicationData.getAccountId() : getString(R.string.account_id));

            //For safety we save the configuration again. Demo wont be empty in the next activity
            applicationData = new ApplicationData(
                    demo_mode,
                    accountId,
                    baseUrl);

            dataHandler.saveJsonStringData(
                    appDataSharedPreferencesKeyValue,
                    applicationData.toJsonObject(),
                    LoginActivity.this);

        } else {
            applicationData = new ApplicationData(
                    DEFAULT_DEMO_MODE,
                    getString(R.string.account_id),
                    getString(R.string.env_url));

            dataHandler.saveJsonStringData(
                    appDataSharedPreferencesKeyValue,
                    applicationData.toJsonObject(),
                    LoginActivity.this);
        }
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailUsernameView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String emailOrUsername = mEmailUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid emailOrUsername address.
        if (TextUtils.isEmpty(emailOrUsername)) {
            mEmailUsernameView.setError(getString(R.string.error_field_required));
            focusView = mEmailUsernameView;
            cancel = true;
        }
        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(emailOrUsername, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailUsernameView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private String httpError;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(100); //Originally was 2000
            } catch (InterruptedException e) {
                return false;
            }

            apiGwLoginDetails = new ApiGwLoginDetails(
                    mEmailUsernameView.getText().toString(),
                    mPasswordView.getText().toString(),
                    accountId);

            //If details changed before pressing the Go button - save the changes
            dataHandler.saveJsonStringData(
                    sharedPreferencesKeyValue,
                    apiGwLoginDetails.toJsonObject(),
                    LoginActivity.this);

            //Prepare authServer

            authServer = new UserPasswordServer(
                    baseUrl,
                    apiGwLoginDetails.getAccountId(),
                    apiGwLoginDetails.getUseranme(),
                    apiGwLoginDetails.getPassword());
            try {
                authServer.acquireJwt();
            } catch (HttpErrorResponseException e) {
                httpError = "Error: " + e.getHttpErrorStatusCode() + "\nMessage: " + e.getHttpErrorMessage();
                return false;
            } catch (ProxyException e) {
                httpError = "Error: Please check your Internet connection or URL configuration";
                Log.d("LoginActivity", "exception: " + e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            Context context = LoginActivity.this;

            if (success) {

                AndroidUtils.customToast(context, "Login Success", Color.GREEN);

                Intent accessTokenActivityIntent = new Intent(context, AccessTokenActivity.class);

                if (null != authServer) {
                    accessTokenActivityIntent.putExtra("authServer", authServer);
                }

                startActivityForResult(accessTokenActivityIntent, 1);
            } else {
                AndroidUtils.customToast(context, "Login Failed!!", Color.RED);

                if (!StringUtils.isEmpty(httpError)) {
                    AndroidUtils.customToastWithTimer(context, httpError, Color.GRAY, Toast.LENGTH_LONG);
                }

                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void loadLoginDetailsIfExist() {

        //Retrieve login details from SharedRef
        JSONObject jsonStored =  dataHandler.getJsonStringData(
                sharedPreferencesKeyValue,
                LoginActivity.this);

        //Set login details from SharedRef if exist
        if (null != jsonStored) {
            apiGwLoginDetails = new ApiGwLoginDetails(jsonStored);

            if (!apiGwLoginDetails.isEmpty()) {
                mEmailUsernameView.setText(apiGwLoginDetails.getUseranme());
                mPasswordView.setText(apiGwLoginDetails.getPassword());
            }
        }
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}

