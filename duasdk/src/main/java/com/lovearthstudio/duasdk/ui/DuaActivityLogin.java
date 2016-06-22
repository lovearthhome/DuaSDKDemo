package com.lovearthstudio.duasdk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lovearthstudio.duasdk.DuaConfig;
import com.lovearthstudio.duasdk.Dua;
import com.lovearthstudio.duasdk.DuaCallback;
import com.lovearthstudio.duasdk.R;
import com.lovearthstudio.duasdk.util.AlertUtil;
import com.lovearthstudio.duasdk.util.LogUtil;

import net.rimoto.intlphoneinput.IntlPhoneInput;

public class DuaActivityLogin extends AppCompatActivity {
    private Toolbar toolbar;
    private Button mSignInButton;
    private IntlPhoneInput phoneInputView;
    private EditText mPasswordView;
    private Intent callbackIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dua_activity_login);
        toolbar=(Toolbar)findViewById(R.id.dua_toolbar);
        toolbar.setTitle("");
        TextView title = (TextView) findViewById(R.id.dua_toolbar_title);
        title.setText("登录");
        setSupportActionBar(toolbar);
//        ActionBar actionBar=getSupportActionBar();
//        if(actionBar!=null){
//            actionBar.setDisplayShowTitleEnabled(false);
//            actionBar.setHomeButtonEnabled(true);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
        DuaConfig.loadConfig(getIntent());
        callbackIntent=DuaConfig.getLoginCallbackIntent();

        mSignInButton = (Button) findViewById(R.id.dua_login_button_sign_in);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mPasswordView = (EditText) findViewById(R.id.dua_login_input_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.dua_action_ime_login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        phoneInputView = (IntlPhoneInput) findViewById(R.id.dua_login_input_phone);
        Dua.DuaUser user=Dua.init(getApplicationContext()).getCurrentDuaUser();
        phoneInputView.setNumber(user.tel);
        phoneInputView.setOnValidityChange(new IntlPhoneInput.IntlPhoneInputListener() {
            @Override
            public void done(View view, boolean isValid) {
                if(isValid){
                    mSignInButton.setEnabled(true);
                    mPasswordView.requestFocus();
                }else{
                    mSignInButton.setEnabled(false);
                    AlertUtil.showToast(DuaActivityLogin.this,"Phone number is invalid");
                }
            }
        });
        TextView textView_register=(TextView)findViewById(R.id.dua_login_button_sign_up);
        textView_register.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DuaActivityLogin.this,DuaActivityRegister.class));
            }
        });
        TextView textView_reset_pwd=(TextView)findViewById(R.id.dua_login_button_forget_pwd);
        textView_reset_pwd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DuaActivityLogin.this,DuaActivityRegister.class).putExtra("LaunchMode",DuaConfig.DUA_LAUNCH_MODE_RESET_PWD));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void attemptLogin(){
        String phone="";
        if(phoneInputView.isValid()){
            phone=phoneInputView.getText();
            String prefix=String.valueOf(phoneInputView.getSelectedCountry().getDialCode());
            phone=phone.replaceFirst(prefix,prefix+"-");
        }
        String password = mPasswordView.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            AlertUtil.showToast(DuaActivityLogin.this,"Phone number is invalid");
            phoneInputView.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("请输入密码");
            mPasswordView.requestFocus();
            return;
        }
        Dua.getInstance().login(phone,password,"member", new DuaCallback() {
            @Override
            public void onSuccess(String s) {
                if(callbackIntent!=null){
                    startActivity(callbackIntent);
                }else{
                    finish();
                }
            }
            @Override
            public void onError(String s) {
                AlertUtil.showToast(DuaActivityLogin.this,s);
            }
        });
    }
}

