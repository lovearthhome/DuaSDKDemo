package com.lovearthstudio.duasdk.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;

import com.lovearthstudio.duasdk.Dua;
import com.lovearthstudio.duasdk.DuaCallback;
import com.lovearthstudio.duasdk.DuaConfig;
import com.lovearthstudio.duasdk.R;
import com.lovearthstudio.duasdk.util.AlertUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class DuaActivityRegister extends AppCompatActivity{
    public CustomViewPager viewPager;
    private Toolbar toolbar;
    public List<Fragment> fragments;

    public String ustr;
    public String vf_code;
    public String pwd;
    public String sex;
    public String birthday;

    private String launchMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dua_activity_register);
        toolbar=(Toolbar)findViewById(R.id.dua_toolbar);
        toolbar.setTitle("");
        TextView title=(TextView)findViewById(R.id.dua_toolbar_title);
        setSupportActionBar(toolbar);
//        ActionBar actionBar=getSupportActionBar();
//        if(actionBar!=null){
//            actionBar.setDisplayShowTitleEnabled(false);
//            actionBar.setHomeButtonEnabled(true);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }


        viewPager=(CustomViewPager)findViewById(R.id.dua_register_pager);
        fragments = new ArrayList<>();
        String titleStr="注册";
        Intent intent=getIntent();
        if(intent!=null){
            Bundle extra=intent.getExtras();
            if(extra!=null){
                launchMode=extra.getString("LaunchMode");
            }
            if(launchMode!=null){
                if(launchMode.equals(DuaConfig.DUA_LAUNCH_MODE_RESET_PWD)){
                    titleStr="修改密码";

                    Bundle bundle=new Bundle();
                    bundle.putString("LaunchMode",launchMode);
                    DuaFragmentRegister fragmentResetPwd=new DuaFragmentRegister();
                    fragmentResetPwd.setArguments(bundle);
                    fragments.add(fragmentResetPwd);
                }else if (launchMode.equals(DuaConfig.DUA_LAUNCH_MODE_MODIFY_PROFILE)){
                    titleStr="修改资料";

                    fragments.add(new DuaFragmentProfileSex());
                    fragments.add(new DuaFragmentProfileBirthday());
                }
            } else {
                fragments.add(new DuaFragmentRegister());
                fragments.add(new DuaFragmentProfileSex());
                fragments.add(new DuaFragmentProfileBirthday());
            }
        }else{
            fragments.add(new DuaFragmentRegister());
            fragments.add(new DuaFragmentProfileSex());
            fragments.add(new DuaFragmentProfileBirthday());
        }

        title.setText(titleStr);
        viewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        viewPager.setSwipEnabled(false);
    }
    public void toNextPage(){
        int next=viewPager.getCurrentItem()+1;
        if(next==fragments.size()){
            if(launchMode!=null&&launchMode.equals(DuaConfig.DUA_LAUNCH_MODE_RESET_PWD)) {
                startResetPwd();
            } else if(launchMode!=null&&launchMode.equals(DuaConfig.DUA_LAUNCH_MODE_MODIFY_PROFILE)) {
                startModifyProfile();
            } else {
                startRegister();
            }
        }else {
            viewPager.setCurrentItem(next);
        }
    }
    public void toLastPage(){
        int last=viewPager.getCurrentItem()-1;
        if(last==-1){
            finish();
        }else {
            viewPager.setCurrentItem(last);
        }
    }

    public void startRegister(){
        String role="member";
        String in_vode="";
        String type="T";
        DuaCallback duaCallback=new DuaCallback() {
            @Override
            public void onSuccess(String str) {
                Intent intent=DuaConfig.getRegisterCallbackIntent();
                if(intent==null){
                    finish();
                }else {
                    startActivity(intent);
                }
            }

            @Override
            public void onError(String str) {
                AlertUtil.showToast(DuaActivityRegister.this,str);
            }
        };
        Dua.getInstance().register(ustr,pwd,role,vf_code,in_vode,type,sex,birthday,duaCallback);
    }
    public void startModifyProfile(){
    }
    public void startResetPwd(){
        AlertUtil.showToast(DuaActivityRegister.this,"修改/找回密功能暂未实现");
        finish();
    }
    @Override
    public void onBackPressed() {
        toLastPage();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
