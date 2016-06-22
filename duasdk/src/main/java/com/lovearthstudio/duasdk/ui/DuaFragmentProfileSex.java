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

import java.util.Arrays;
import java.util.List;

import cn.qqtheme.framework.picker.OptionPicker;


public class DuaFragmentProfileSex extends Fragment implements View.OnClickListener{
    private Button button_pick_sex;
    private Button button_last_step;
    private Button button_next_step;
    private String[] sexs={"M","F"};
    private String[] sexArray={"男","女"};
    private List<String> sexList= Arrays.asList(sexArray);
    private String sex;
    public DuaFragmentProfileSex() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.dua_fragment_profile_sex, container, false);
        button_pick_sex=(Button) view.findViewById(R.id.dua_register_button_pick_sex);
        button_pick_sex.setOnClickListener(this);
        button_last_step=(Button)view.findViewById(R.id.dua_register_button_last_step);
        button_last_step.setOnClickListener(this);
        button_next_step=(Button)view.findViewById(R.id.dua_register_button_next_step);
        button_next_step.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.dua_register_button_pick_sex) {
            final OptionPicker picker=new OptionPicker(getActivity(),sexArray);
            picker.setOffset(2);
            picker.setSelectedIndex(0);
            picker.setTextSize(11);
            picker.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
                @Override
                public void onOptionPicked(String option) {
                    sex=sexs[sexList.indexOf(option)];
                }
            });
            picker.show();
        } else if (i == R.id.dua_register_button_last_step) {
            ((DuaActivityRegister)getActivity()).toLastPage();
        } else if (i == R.id.dua_register_button_next_step) {
            if (TextUtils.isEmpty(sex)) {
                AlertUtil.showToast(getContext(),"请选择性别");
                return;
            }
            DuaActivityRegister activity=(DuaActivityRegister)getActivity();
            activity.sex=sex;
            activity.toNextPage();
        }
    }
}
