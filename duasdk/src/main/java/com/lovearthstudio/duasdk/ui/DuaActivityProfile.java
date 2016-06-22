package com.lovearthstudio.duasdk.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lovearthstudio.duasdk.Dua;
import com.lovearthstudio.duasdk.DuaCallback;
import com.lovearthstudio.duasdk.R;
import com.lovearthstudio.duasdk.util.AlertUtil;
import com.lovearthstudio.duasdk.util.FileUtil;
import com.lovearthstudio.duasdk.util.IntentUtil;
import com.lovearthstudio.duasdk.util.LogUtil;
import com.lovearthstudio.duasdk.util.TimeUtil;

import java.util.HashMap;
import java.util.Map;


public class DuaActivityProfile extends AppCompatActivity {
    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果
    private static final String LOCAL_IMG_PATH="/sdcard/dua/img/local/";

    private String imageName;
    private Uri  uri;
    private RelativeLayout rl_avatar;
    private RelativeLayout rl_sex;
    private ImageView iv_avatar;
    private Dua.DuaUser user;
    private int RESOURCE_ID_START;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dua_activity_profile);
        RESOURCE_ID_START=View.generateViewId();
        Toolbar toolbar=(Toolbar)findViewById(R.id.dua_toolbar);
        toolbar.setTitle("");
        TextView title = (TextView) findViewById(R.id.dua_toolbar_title);
        title.setText("个人信息");

        rl_sex=(RelativeLayout)findViewById(R.id.rl_sex);
        rl_sex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSexDialog();
            }
        });

        rl_avatar=(RelativeLayout)findViewById(R.id.rl_avatar);
        rl_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotoDialog();
            }
        });
        iv_avatar=(ImageView)findViewById(R.id.iv_avatar);

        user=Dua.getInstance().getCurrentDuaUser();
        String avatar=user.avatar;
        if(avatar!=null&&!avatar.equals("")){
            Glide.with(DuaActivityProfile.this)
                    .load(avatar)
//                    .placeholder(R.mipmap.head)
                    //.error(R.mipmap.ic_launcher)
//                .override((int) (Constant.screenwith - Constant.mainItemPadding - Constant.mainPadding), (int) (img_height * ratio))
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(iv_avatar);
        }
    }


    private void showSexDialog() {
        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        String title="性别";
        final int startId=RESOURCE_ID_START;
        String[] content={"男","女"};
        AlertUtil.showDialog(dlg, title,startId,content,new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id=v.getId();
                if(id==startId){
                }else if (id==startId+1){
                }else if (id==startId+2) {
                }
                dlg.cancel();
            }
        });

    }

    private void showPhotoDialog() {
        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        final int startId=RESOURCE_ID_START+3;
        String[] content={"拍照","相册"};
        AlertUtil.showDialog(dlg, "",startId,content,new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id=v.getId();
                if(id==startId){
                }else if (id==startId+1){
                    imageName =  TimeUtil.getCurrentTimeString("yyyyMMddHHmmssSS")+".png";
                    uri=Uri.fromFile(FileUtil.newFile(LOCAL_IMG_PATH,imageName));
                    IntentUtil.startPhotoShot(DuaActivityProfile.this,uri);
                }else if (id==startId+2) {
                    imageName = TimeUtil.getCurrentTimeString("yyyyMMddHHmmssSS") + ".png";
                    uri=Uri.fromFile(FileUtil.newFile(LOCAL_IMG_PATH,imageName));
                    IntentUtil.startPhotoGallery(DuaActivityProfile.this);
                }
                dlg.cancel();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case IntentUtil.PHOTO_REQUEST_TAKEPHOTO:
                    IntentUtil.startPhotoZoom(this,uri,uri,480);
                    break;

                case IntentUtil.PHOTO_REQUEST_GALLERY:
                    if (data != null) {
                        IntentUtil.startPhotoZoom(this, data.getData(),uri,480);
                    }
                    break;

                case IntentUtil.PHOTO_REQUEST_CUT:
                    // BitmapFactory.Options options = new BitmapFactory.Options();
                    //
                    // /**
                    // * 最关键在此，把options.inJustDecodeBounds = true;
                    // * 这里再decodeFile()，返回的bitmap为空
                    // * ，但此时调用options.outHeight时，已经包含了图片的高了
                    // */
                    // options.inJustDecodeBounds = true;
                    Bitmap bitmap = BitmapFactory.decodeFile(LOCAL_IMG_PATH + imageName);
                    iv_avatar.setImageBitmap(bitmap);
                    Dua.getInstance().updateAvatar(imageName,LOCAL_IMG_PATH+imageName,null);
                    break;

            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
