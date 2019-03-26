package com.echomu.ovulationmaster.ovulation.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.echomu.ovulationmaster.MainActivity;
import com.echomu.ovulationmaster.R;
import com.example.echomu.ovulationmaster.ovulation.ImageUtil;
import com.example.echomu.ovulationmaster.ovulation.PublicMethod;
import com.example.echomu.ovulationmaster.ovulation.view.camera.CameraPreview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 拍照界面
 */
public class OvulationCameraActivity extends Activity implements View.OnClickListener {

    private CameraPreview cameraPreview;
    private View containerView;
    private View cropSquareView;
    private ImageView ivBaseLine;

    private String cropPath = "";
    /**
     * 原图的拍摄时间戳
     **/
    private String dateline;

    public static void startActivityForResult(Activity activity) {
        Intent intent = new Intent(activity, OvulationCameraActivity.class);
        activity.startActivityForResult(intent, MainActivity.REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ovulation_camera);

        cameraPreview = findViewById(R.id.camera_surface);
        containerView = findViewById(R.id.camera_crop_container);
        cropSquareView = findViewById(R.id.fl_corp_square);
        ivBaseLine = findViewById(R.id.iv_base_line);

        cameraPreview.setOnClickListener(this);
        findViewById(R.id.camera_cancel).setOnClickListener(this);
        findViewById(R.id.camera_take).setOnClickListener(this);
        findViewById(R.id.camera_album).setOnClickListener(this);

        //获取屏幕最小边，设置为cameraPreview较窄的一边
        float screenMinSize = Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
        //设置黄色基准线距离屏幕左边间距
        FrameLayout.LayoutParams baseLineParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        baseLineParams.topMargin = PublicMethod.dip2px(this, 200);
        baseLineParams.leftMargin = (int) (screenMinSize * 3 / 5 + PublicMethod.dip2px(this, 2));
        ivBaseLine.setLayoutParams(baseLineParams);

        cameraPreview.focus();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera_surface:
                cameraPreview.focus();
                break;
            case R.id.camera_cancel:
                finish();
                break;
            case R.id.camera_take:
                takePhoto();
                break;
            case R.id.camera_album:
//                SelectPhotoActivity.toStartActivityForResult(this, PostRecordActivity.REQUESTCODE_ADDIMAGE, 0, 1, true, false);
                break;
        }
    }

    private void takePhoto() {
        cameraPreview.setEnabled(false);
        cameraPreview.takePhoto(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                camera.stopPreview();
                //子线程处理图片，防止ANR
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            File originalFile = getOriginalFile();
                            FileOutputStream originalFileOutputStream = new FileOutputStream(originalFile);
                            originalFileOutputStream.write(data);
                            originalFileOutputStream.close();

                            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getPath());

                            //计算裁剪位置
                            float left = (float) cropSquareView.getLeft() / (float) cameraPreview.getWidth();
                            float top = ((float) containerView.getTop() - (float) cameraPreview.getTop()) / (float) cameraPreview.getHeight();
                            float right = (float) cropSquareView.getRight() / (float) cameraPreview.getWidth();
                            float bottom = (float) containerView.getBottom() / (float) cameraPreview.getHeight();

                            //裁剪及保存到文件
                            Bitmap cropBitmap = Bitmap.createBitmap(bitmap,
                                    (int) (left * (float) bitmap.getWidth()),
                                    (int) (top * (float) bitmap.getHeight()),
                                    (int) ((right - left) * (float) bitmap.getWidth()),
                                    (int) ((bottom - top) * (float) bitmap.getHeight()));
                            saveCropFile(cropBitmap);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    goBack();
                                }
                            });
                            return;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cameraPreview.setEnabled(true);
                            }
                        });
                    }
                }).start();

            }
        });
    }

    /**
     * @return 拍摄图片原始文件
     */
    private File getOriginalFile() {
        dateline = System.currentTimeMillis()+"";
        String originalPath = ImageUtil.DIR + "ovulation_original_" + dateline + ".jpeg";
        return new File(ImageUtil.DIR, "ovulation_original_" + dateline + ".jpeg");
    }

    private void saveCropFile(Bitmap croppedImage) {
        cropPath = ImageUtil.DIR + "ovulation_crop" + dateline + ".jpeg";
        Boolean b = ImageUtil.saveBitmapFile(this, croppedImage, "ovulation_crop" + dateline);
        if (!b) {
//            ToastUtil.showToast(this, "图片操作错误");
        }
    }

    /**
     * 点击对勾，使用拍照结果，返回对应图片路径
     */
    private void goBack() {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.EXTRA_CROP_PATH, cropPath);
        intent.putExtra(MainActivity.EXTRA_DATELINE, dateline);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (cameraPreview != null) {
            cameraPreview.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cameraPreview != null) {
            cameraPreview.onStop();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
//                case PostRecordActivity.REQUESTCODE_ADDIMAGE:
//                    //获取图库添加文件路径
//                    if (data != null && data.hasExtra("addlist")) {
//                        List<Attachment> addlist = data.getParcelableArrayListExtra("addlist");
//                        if (addlist != null && addlist.size() != 0) {
//                            OvulationCropActivity.toStartActivity(this, addlist.get(0).getMediaPath());
//                        }
//                    }
//                    break;
//                case BaseOvulationActivity.REQUEST_CODE:
//                    //获取试纸编辑的文件路径
//                    cropPath = data.getStringExtra(BaseOvulationActivity.EXTRA_CROP_PATH);
//                    goBack();
//                    break;
            }
        }
    }

}
