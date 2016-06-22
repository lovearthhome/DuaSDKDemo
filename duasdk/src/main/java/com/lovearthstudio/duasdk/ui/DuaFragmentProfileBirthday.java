package com.lovearthstudio.duasdk.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lovearthstudio.duasdk.R;
import com.lovearthstudio.duasdk.util.AlertUtil;

import cn.qqtheme.framework.picker.DatePicker;

public class DuaFragmentProfileBirthday extends Fragment implements View.OnClickListener{
    private Button button_pick_birthday;
    private Button button_last_step;
    private Button button_complete;

    private DuaActivityRegister activity;
    private String birthday;
    public DuaFragmentProfileBirthday() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.dua_fragment_profile_birthday, container, false);
        button_pick_birthday=(Button) view.findViewById(R.id.dua_register_button_pick_birthday);
        button_pick_birthday.setOnClickListener(this);
        button_last_step=(Button)view.findViewById(R.id.dua_register_button_last_step);
        button_last_step.setOnClickListener(this);
        button_complete=(Button)view.findViewById(R.id.dua_register_button_complete);
        button_complete.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.dua_register_button_pick_birthday) {
            DatePicker picker = new DatePicker(getActivity(), DatePicker.YEAR_MONTH_DAY);
            picker.setRange(1900, 2016);//年份范围
            picker.setOnDatePickListener(new DatePicker.OnYearMonthDayPickListener() {
                @Override
                public void onDatePicked(String year, String month, String day) {
                    birthday=year+month+day;
                }
            });
            picker.show();
        } else if (i == R.id.dua_register_button_last_step) {
            ((DuaActivityRegister)getActivity()).toLastPage();
        } else if (i == R.id.dua_register_button_complete) {
            if (TextUtils.isEmpty(birthday)) {
                AlertUtil.showToast(getContext(),"请选择性别");
                return;
            }
            DuaActivityRegister activity=(DuaActivityRegister)getActivity();
            activity.birthday=birthday;
            activity.toNextPage();
        }
    }
}
