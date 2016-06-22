package com.lovearthstudio.duasdk.demo.duasdkdemo;

/**
 * Author：Mingyu Yi on 2016/6/8 09:55
 * Email：461072496@qq.com
 */
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.lovearthstudio.duasdk.demo.R;


public class ShareWindow extends PopupWindow {

    private Context mContext;

    private View view;

    private ImageButton btn_share_qq, btn_share_mm, btn_share_weibo,btn_share_cancel;


    public ShareWindow(Context mContext, View.OnClickListener itemsOnClick) {

        this.mContext=mContext;
        this.view = LayoutInflater.from(mContext).inflate(R.layout.share_main, null);

        btn_share_qq = (ImageButton) view.findViewById(R.id.btn_share_qq);
        btn_share_mm = (ImageButton) view.findViewById(R.id.btn_share_mm);
        btn_share_weibo = (ImageButton) view.findViewById(R.id.btn_share_weibo);
        btn_share_cancel = (ImageButton) view.findViewById(R.id.btn_share_cancel);
        btn_share_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btn_share_weibo.setOnClickListener(itemsOnClick);
        btn_share_mm.setOnClickListener(itemsOnClick);
        btn_share_qq.setOnClickListener(itemsOnClick);

        // 设置外部可点击
        this.setOutsideTouchable(true);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        this.view.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = view.findViewById(R.id.grid_share_main).getTop();

                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });


    /* 设置弹出窗口特征 */
        // 设置视图
        this.setContentView(this.view);
        // 设置弹出窗体的宽和高
        this.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
        this.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);

        // 设置弹出窗体可点击
        this.setFocusable(true);

        ColorDrawable dw = new ColorDrawable(0xFFFF6666);
        // 设置弹出窗体的背景
        this.setBackgroundDrawable(dw);

        // 设置弹出窗体显示时的动画，从底部向上弹出
        this.setAnimationStyle(R.style.share_main_anim);
    }


}
