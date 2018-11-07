package com.data.ui.activity.set;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.data.data.UpdataUser;
import com.data.db.User;
import com.data.operation.BeanMessageHandler;
import com.data.pbprotocol.ChatProtocol;
import com.data.ui.activity.BaseActivity;
import com.data.ui.activity.ChatActivity;
import com.data.ui.view.BitMapComp;
import com.data.ui.view.TT;
import com.data.util.MobileSystemUtil;
import com.data.util.image.SmartImageView;
import com.data.util.net.PbAsyncTcpResponse;
import com.data.util.net.RequestParamTools;
import com.data.util.net.RsProtocolContext;
import com.data.util.net.bean.ProtocolContext;
import com.google.protobuf.InvalidProtocolBufferException;
import com.juns.wechat.App;
import com.juns.wechat.R;
import com.juns.wechat.common.Utils;

import org.litepal.crud.DataSupport;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.greenrobot.event.EventBus;

public class UpdataImageActivity extends BaseActivity implements View.OnClickListener{
    private Dialog setHeadDialog;
    private View mDialogView;
    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int RESIZE_REQUEST_CODE = 2;

    private final static int CORPORATE_REQUESTCODE = 3; // 返回的结果码

    private static final String IMAGE_FILE_NAME = "header.jpg";
    private File fileHead;

    private TextView txt_title;
    private ImageView img_back;
    private RelativeLayout rlRelativeLayout;
    private RelativeLayout rlUserName;
    private TextView tvUserName;
    private TextView tvWxZhang;
    private RelativeLayout rlImage;
    private SmartImageView imImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.menu_setting_personal_data);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initControl() {
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_title.setText("设置");
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        rlImage = (RelativeLayout)findViewById(R.id.rl_image);
        rlUserName = (RelativeLayout)findViewById(R.id.rl_user_name);
        tvUserName = (TextView)findViewById(R.id.tv_user_name);
        tvWxZhang = (TextView)findViewById(R.id.tv_wx_zhanghao);
        imImage = (SmartImageView)findViewById(R.id.im_image);
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        User user = DataSupport.findFirst(User.class);
        if(user != null){
            if(user.getPhoto()!=null){
                imImage.setImageUrl(user.getPhoto());
            }
            if(user.getNickname()!=null){
                tvUserName.setText(user.getNickname());
            }
            if(user.getName()!=null){
                tvWxZhang.setText(user.getName());
            }
        }
    }

    @Override
    protected void setListener() {
        img_back.setOnClickListener(this);
        rlImage.setOnClickListener(this);
        rlUserName.setOnClickListener(this);
    }

    /**
     * 设置头部控件，并存入数据库
     *
     * @param bitmap
     */
    public void setDbHeadPortrait(Bitmap bitmap) {
        User user = DataSupport.findFirst(User.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        user.setPhotobyte(baos.toByteArray());
        user.save();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                Utils.finish(UpdataImageActivity.this);
                break;
            case R.id.rl_image:
                fileHead = new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME);
                showPop();
                break;
            case R.id.rl_user_name:
                startActivityForResult(new Intent(UpdataImageActivity.this, UpdataNameActivity.class),CORPORATE_REQUESTCODE);
                break;
            default:
                break;
        }
    }

    private void handleImageBeforeKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private boolean isSdcardExisting() {//判断SD卡是否存在
        final String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
        } else {
            switch (requestCode) {
                //相册选取图片返回
                case IMAGE_REQUEST_CODE:
                    //resizeImage(data.getData());
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitKat(data);
                    } else {
                        handleImageBeforeKitKat(data);
                    }
                    break;
                //相机拍照返回
                case CAMERA_REQUEST_CODE:
                    if (isSdcardExisting()) {
                        if (fileHead != null && fileHead.exists())
                            displayImage(fileHead.getAbsolutePath());
                        else
                            TT.show(this, "找不到图片");
                        //resizeImage(Uri.fromFile(getImageUri()));
                    } else {
                        TT.show(UpdataImageActivity.this, "程序无相关权限或无存储卡，请在设置中授权");
                    }
                    break;

                case RESIZE_REQUEST_CODE:
                    if (data != null) {
                        showResizeImage(data);
                    }
                    break;
            }
        }
        if (resultCode == CORPORATE_REQUESTCODE) {//判断公司名称修改
            if (requestCode == CORPORATE_REQUESTCODE) {
                String corporateName = data.getStringExtra("username");
                if (corporateName != null) {
                    tvUserName.setText(corporateName);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showResizeImage(Intent data) {//显示图片
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            imImage.setImageBitmap(photo);
            setDbHeadPortrait(photo);
            setHeadDialog.dismiss();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.provider.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
        displayImage(imagePath);
    }

    /**
     * 拍完照和从相册获取完图片都要执行的方法(根据图片路径显示图片)
     */
    private void displayImage(String imagePath) {
        if (!TextUtils.isEmpty(imagePath)) {
            sendPicture(imagePath);
            ImgUpdateDirection(imagePath, BitMapComp.comps(imagePath));    //显示图片,并且判断图片显示的方向,如果不正就放正
        } else
            Toast.makeText(this, "图片获取失败", Toast.LENGTH_LONG).show();
    }

    private void sendPicture(final String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();
        String prefix = fileName.substring(fileName.lastIndexOf(".") + 1);
        getFileUploadRequest(prefix, MobileSystemUtil.File2byte(file),2,0);
    }

    private void ImgUpdateDirection(String filepath, Bitmap orc_bitmap) {
        int digree = 0;//图片旋转的角度
        //根据图片的URI获取图片的绝对路径
        //String filepath = ImgUriDoString.getRealFilePath(getApplicationContext(), uri);
        //根据图片的filepath获取到一个ExifInterface的对象
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
            if (exif != null) {

                // 读取图片中相机方向信息
                int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                // 计算旋转角度
                switch (ori) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        digree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        digree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        digree = 270;
                        break;
                    default:
                        digree = 0;
                        break;
                }
            }
            //如果图片不为0
            if (digree != 0) {
                // 旋转图片
                Matrix m = new Matrix();
                m.postRotate(digree);
                orc_bitmap = Bitmap.createBitmap(orc_bitmap, 0, 0, orc_bitmap.getWidth(),
                        orc_bitmap.getHeight(), m, true);
            }
            if (orc_bitmap != null) {
                imImage.setImageBitmap(orc_bitmap);
                setDbHeadPortrait(orc_bitmap);
                setHeadDialog.dismiss();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (orc_bitmap != null) {
                orc_bitmap.recycle();
                orc_bitmap = null;
            }
            exif = null;
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String Path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return Path;
    }

    //显示加载图片
    public void showPop() {
        setHeadDialog = new AlertDialog.Builder(this, R.style.MyDialogStyle).create();
        setHeadDialog.show();
       /* mDialogView = View.inflate(getApplicationContext(),
                R.layout.head_portrait_dialog, null);*/
        mDialogView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.head_portrait_dialog, null);
        setHeadDialog.getWindow().setContentView(mDialogView);

        WindowManager windowManager = this.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = setHeadDialog.getWindow()
                .getAttributes();
        lp.width = (int) (display.getWidth()); // 设置宽度
        setHeadDialog.getWindow().setAttributes(lp);
        bindDialogEvent();
    }

    private void bindDialogEvent() {
        Button cameraButton = (Button) mDialogView
                .findViewById(R.id.iv_userinfo_takepic);
        Button photoButton = (Button) mDialogView
                .findViewById(R.id.iv_userinfo_choosepic);
        Button cancelButton = (Button) mDialogView
                .findViewById(R.id.iv_userinfo_cancle);

        cameraButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                int currentApiVersion = Build.VERSION.SDK_INT;

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 拍照
                if (currentApiVersion < 24) {
                    Uri uri = Uri.fromFile(fileHead);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            uri);
                      /*  ContentValues values = new ContentValues(1);
                        values.put(MediaStore.Images.Media.DATA, fileHead.getPath());
                        cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, getContentResolver().insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values));*/
                } else {
                    Uri imageUri = FileProvider.getUriForFile(getApplicationContext(),
                            getApplicationContext().getPackageName() + ".fileprovider",
                            fileHead);
                    cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                }
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                setHeadDialog.dismiss();
            }
        });
        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                galleryIntent.setType("image/*");// 图片
                startActivityForResult(galleryIntent, IMAGE_REQUEST_CODE);
                setHeadDialog.dismiss();
            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                setHeadDialog.dismiss();
            }
        });
    }

    private void getFileUploadRequest(final String type, byte[] content,final int messageType,final float other) {
        if (!App.isOnline()) {
            return;
        }
        RsProtocolContext protocolContext = RequestParamTools.getFileUploadRequest(type, content,messageType);
        if (protocolContext != null) {
            BeanMessageHandler.getAppClient().sendRequest(protocolContext, new PbAsyncTcpResponse() {
                @Override
                protected void onSuccess(ProtocolContext protocol) {
                    try {
                        ChatProtocol.Response response = ChatProtocol.Response.parseFrom(protocol.getBodyBuffer());
                        Log.e("abc", response.toString());
                        if (response.getErrorCode() == 0) {
                            User user = DataSupport.findFirst(User.class);
                            user.setPhoto(response.getFileUpload().getUrl());
                            user.save();
                            EventBus.getDefault().post(new UpdataUser());
                        } else {
                            TT.show(UpdataImageActivity.this, "" + response.getErrorMessage());
                        }
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                    super.onSuccess(protocol);
                }

                @Override
                protected void onFailed(int type) {

                }
            });
        } else {

        }
    }
}
