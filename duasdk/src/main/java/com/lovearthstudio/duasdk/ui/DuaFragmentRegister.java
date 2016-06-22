package com.lovearthstudio.duasdk.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.lovearthstudio.duasdk.Dua;
import com.lovearthstudio.duasdk.DuaCallback;
import com.lovearthstudio.duasdk.DuaConfig;
import com.lovearthstudio.duasdk.R;
import com.lovearthstudio.duasdk.util.AlertUtil;
import com.lovearthstudio.duasdk.util.LogUtil;

import net.rimoto.intlphoneinput.IntlPhoneInput;

public class DuaFragmentRegister extends Fragment implements View.OnClickListener{
    private IntlPhoneInput intlPhoneInput;
    private EditText editText_vf_code;
    private Button button_get_vf_code;
    private EditText editText_pwd;
    private EditText editText_repwd;
    private Button button_next_step;

    private ImageView iv_show;
    private ImageView iv_hide;

    private DuaActivityRegister activity;
    private String launchMode;
    private static final int UPDATE_TIMER_TICK =10010;
    private static final int UPDATE_TIMER_FINISH=10086;
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TIMER_TICK:
                    button_get_vf_code.setEnabled(false);
                    button_get_vf_code.setText((String)msg.obj);
                    break;
                case UPDATE_TIMER_FINISH:
                    button_get_vf_code.setEnabled(true);
                    button_get_vf_code.setText(R.string.dua_button_get_vf_code);
                    intlPhoneInput.setEnabled(true);
                    break;
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.dua_fragment_register, container, false);
        Bundle bundle=getArguments();
        if(bundle!=null){
            launchMode=bundle.getString("LaunchMode");
        }
        intlPhoneInput=(IntlPhoneInput) view.findViewById(R.id.dua_register_input_phone);
        editText_vf_code=(EditText)view.findViewById(R.id.dua_register_input_vf_code);
        button_get_vf_code=(Button)view.findViewById(R.id.dua_register_button_get_vf_code);
        button_next_step=(Button)view.findViewById(R.id.dua_register_button_next_step);
//        editText_pwd=(EditText)view.findViewById(R.id.dua_register_input_pwd);
//        editText_repwd=(EditText)view.findViewById(R.id.dua_register_input_repwd);
        editText_pwd=(EditText)view.findViewById(R.id.dua_et_password);
        iv_show=(ImageView)view.findViewById(R.id.iv_show);
        iv_hide=(ImageView)view.findViewById(R.id.iv_hide);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        button_get_vf_code.setEnabled(false);
        button_get_vf_code.setOnClickListener(this);
        button_next_step.setEnabled(false);
        button_next_step.setOnClickListener(this);
        if(launchMode!=null&&launchMode.equals(DuaConfig.DUA_LAUNCH_MODE_RESET_PWD)){
            button_next_step.setText(R.string.dua_action_ensure);
        }else {
            intlPhoneInput.setDefault();
        }
        intlPhoneInput.setOnValidityChange(new IntlPhoneInput.IntlPhoneInputListener() {
            @Override
            public void done(View view, boolean isValid) {
                if(isValid){
                    button_get_vf_code.setEnabled(true);
                    editText_vf_code.requestFocus();
                    button_next_step.setEnabled(true);
                }else{
                    button_get_vf_code.setEnabled(false);
                    intlPhoneInput.requestFocus();
                    button_next_step.setEnabled(false);
                }
            }
        });
        iv_hide.setOnClickListener(this);
        iv_show.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.dua_register_button_get_vf_code) {
            intlPhoneInput.setEnabled(false);
            Dua.getInstance().getVfCode(getUstr(),new DuaCallback() {
                @Override
                public void onSuccess(String str) {
                    startCountDownTimer();
                }
                @Override
                public void onError(String str) {
                    updateUI(UPDATE_TIMER_FINISH,null);
                    AlertUtil.showToast(getContext(),str);
                }
            });
        } else if (i == R.id.dua_register_button_next_step) {
            String vf_code=editText_vf_code.getText().toString().trim();
            if (TextUtils.isEmpty(vf_code)) {
                editText_vf_code.setError("请输入短信验证码");
                editText_vf_code.requestFocus();
                return;
            }
            String pwd=editText_pwd.getText().toString().trim();
            if (TextUtils.isEmpty(pwd)) {
                editText_pwd.setError("请输入密码");
                editText_pwd.requestFocus();
                return;
            }
//            String repwd=editText_repwd.getText().toString().trim();
//            if (TextUtils.isEmpty(repwd)) {
//                editText_repwd.setError("请再输入一次密码");
//                editText_repwd.requestFocus();
//                return;
//            }
//            if(!pwd.equals(repwd)){
//                editText_repwd.setError("两次密码必须相同");
//                editText_repwd.requestFocus();
//                return;
//            }
            activity=(DuaActivityRegister) getActivity();
            activity.ustr=getUstr();
            activity.vf_code=vf_code;
            activity.pwd=pwd;
            activity.toNextPage();
        }else if(i==R.id.iv_show){
            showPwd(true);
        }else if (i==R.id.iv_hide){
            showPwd(false);
        }
    }

    public String getUstr(){
        String phone="";
        if(intlPhoneInput.isValid()){
            phone=intlPhoneInput.getText();
            String prefix=String.valueOf(intlPhoneInput.getSelectedCountry().getDialCode());
            phone=phone.replaceFirst(prefix,prefix+"-");
        }
        return phone;
    }

    public void showPwd(boolean bl){
        if(bl){
            iv_show.setVisibility(View.GONE);
            iv_hide.setVisibility(View.VISIBLE);
            editText_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }else {
            iv_hide.setVisibility(View.GONE);
            iv_show.setVisibility(View.VISIBLE);
            editText_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
        // 切换后将EditText光标置于末尾
        CharSequence charSequence = editText_pwd.getText();
        if (charSequence instanceof Spannable) {
            Spannable spanText = (Spannable) charSequence;
            Selection.setSelection(spanText, charSequence.length());
        }
    }

    private CountDownTimer timer;
    private void startCountDownTimer(){
        if(timer==null){
            timer=new CountDownTimer(60000,1000){
                @Override
                public void onTick(long millisUntilFinished) {
                    updateUI(UPDATE_TIMER_TICK,millisUntilFinished/1000+"秒");
                }
                @Override
                public void onFinish() {
                    updateUI(UPDATE_TIMER_FINISH,null);
                }
            };
        }
        if(Looper.myLooper()==null){
            Looper.prepare();
        }
        timer.start(); //CountDownTimer只能在有消息队列的线程中启动
    }
    private void cancelCountDownTimer(){
        if(timer!=null){
            timer.cancel();
        }
    }
    private void updateUI(int what,Object obj){
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj=obj;
        msg.sendToTarget();
    }
}
