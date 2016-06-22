package com.lovearthstudio.duasdk.demo.duasdkdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;

import com.lovearthstudio.duasdk.Dua;
import com.lovearthstudio.duasdk.demo.R;
import com.lovearthstudio.duasdk.ui.DuaActivityLogin;
import com.lovearthstudio.duasdk.ui.DuaActivityProfile;
import com.lovearthstudio.duasdk.util.JsonUtil;
import com.lovearthstudio.duasdk.util.LogUtil;
import com.lovearthstudio.duasdk.util.TimeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 * 添加
 */
public class TestActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

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

    private View mProgressView;
    private Dua dua;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        dua=Dua.init(getApplicationContext());


        Button startButton = (Button) findViewById(R.id.auth);
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                dua.auth(null);
                Intent intent = new Intent(TestActivity.this, DuaActivityLogin.class);
               // intent.setComponent(new ComponentName("com.lovearthstudio.duasdk.demo.duasdkdemo", "com.lovearthstudio.duasdk.TestActivity"));
                intent.putExtra("callbackPackage",getPackageName());
                intent.putExtra("loginCallbackActivity",AboutActivity.class.getSimpleName());

                startActivity(intent);
            }
        });

        Button duaIdButton = (Button) findViewById(R.id.duaId);
        duaIdButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    JSONObject jo1=new JSONObject();
                    jo1.put("张三","\"\\\"李四\\\"\"");
                    JSONObject jo2=new JSONObject();
                    jo2.put("\"\\\"王麻子\\\"\"",jo1);
                    JSONArray ja=new JSONArray();
                    ja.put(jo1);
                    ja.put(jo2);
                    String str1=ja.toString();
                    JSONObject jo3=new JSONObject();
                    jo3.put("\"fuck\"",str1);
                    JSONArray jsonArray=new JSONArray();
                    jsonArray.put(jo1);
                    jsonArray.put(jo2);
                    jsonArray.put(jo3);

                    ShareWindow shareWindow=new ShareWindow(TestActivity.this, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LogUtil.e("点击了"+v.getId());
                        }
                    });
                    setBackgroundAlpha(0.1f);
                    shareWindow.setOnDismissListener(new PopupWindow.OnDismissListener(){

                        @Override
                        public void onDismiss() {
                            setBackgroundAlpha(1f);
                        }
                    });
                    shareWindow.showAsDropDown(view);















                    String str2=jsonArray.toString();
                    String str3="\"[{\\\"张三\\\":\\\"\\\\\\\"\\\\\\\\\\\\\\\"李四\\\\\\\\\\\\\\\"\\\\\\\"\\\"},{\\\"\\\\\\\"\\\\\\\\\\\\\\\"王麻子\\\\\\\\\\\\\\\"\\\\\\\"\\\":{\\\"张三\\\":\\\"\\\\\\\"\\\\\\\\\\\\\\\"李四\\\\\\\\\\\\\\\"\\\\\\\"\\\"}},{\\\"\\\\\\\"fuck\\\\\\\"\\\":\\\"[{\\\\\\\"张三\\\\\\\":\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"李四\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\"},{\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"王麻子\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\":{\\\\\\\"张三\\\\\\\":\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"李四\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\\\\\\\\"\\\\\\\"}}]\\\"}]\"";
                    LogUtil.e(str3);
                    JSONArray jsa= JsonUtil.toJsonArray(str3);
                    LogUtil.e("深层解析  "+jsa.toString());
                    LogUtil.e("深层解析结果  "+jsa.getJSONObject(2).getJSONArray("fuck").getJSONObject(1).getJSONObject("王麻子").getString("张三"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        Button vfButton = (Button) findViewById(R.id.vfcode);
        vfButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TestActivity.this, DuaActivityProfile.class));
            }
        });

        Button registerButton = (Button) findViewById(R.id.register);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dua.uploadSleepData("{\"count_petty\":3,\"count_large\":21}",null);
            }
        });

        mProgressView = findViewById(R.id.login_progress);
    }

    public void setBackgroundAlpha(float bgAlpha)
    {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }




    protected void onResume(){
        super.onResume();
        Log.d("Activity", "onResume is called " + TimeUtil.getCurrentTimeString());
        dua.duaAwake();
    }
    protected void onStop(){
        super.onStop();
        Log.d("Activity", "onStop is called " + TimeUtil.getCurrentTimeString());
        dua.duaSleep();
    }
    protected void onDestroy(){
        super.onDestroy();
        dua.duaExit();
        Log.d("Activity", "onDestroy is called " + TimeUtil.getCurrentTimeString());
    }











    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
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

        boolean cancel = false;
        View focusView = null;

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
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
                new ArrayAdapter<>(TestActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);
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

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

