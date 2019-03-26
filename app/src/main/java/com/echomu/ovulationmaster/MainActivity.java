package com.echomu.ovulationmaster;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.echomu.ovulationmaster.ovulation.activity.OvulationCameraActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public final static String EXTRA_CROP_PATH = "extra_crop_path";
    public final static String EXTRA_DATELINE = "extra_dateline";

    public final static int REQUEST_CODE = 0X01;
    public static final int REQUEST_PICK = 0X02;

    private ImageView ivShizhi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivShizhi=findViewById(R.id.iv_testStrip);
        findViewById(R.id.bt_addStripByCamera).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_addStripByCamera:
                takePhoto();
                break;
            default:
                break;
        }
    }

    /**
     * 拍摄照片
     */
    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0x11);
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x12);
            return;
        }
        OvulationCameraActivity.startActivityForResult(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case MainActivity.REQUEST_CODE:
                    //获取试纸编辑或拍摄的文件路径
                    String path = data.getStringExtra(EXTRA_CROP_PATH);
                    String dateline = data.getStringExtra(EXTRA_DATELINE);
                    getCropFilePath(path, dateline);
                    break;
            }
        }
    }

    private void getCropFilePath(String path, String dateline) {
        ivShizhi.setImageURI(Uri.parse(path));
    }

}
