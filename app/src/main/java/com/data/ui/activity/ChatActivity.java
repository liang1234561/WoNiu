package com.data.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.data.data.GlobObject;
import com.data.data.NewMessage;
import com.data.data.UpdataGroupUser;
import com.data.data.bean.MsgBean;
import com.data.db.Friend;
import com.data.db.Message;
import com.data.db.User;
import com.data.operation.BeanMessageHandler;
import com.data.pbprotocol.ChatProtocol;
import com.data.ui.activity.group.AddGroupChatActivity;
import com.data.ui.activity.group.ExitGroupAcyivity;
import com.data.ui.activity.group.RemoveGroupChatActivity;
import com.data.ui.adapter.MessageAdapter;
import com.data.ui.view.AudioRecorderButton;
import com.data.ui.view.TT;
import com.data.util.net.PbAsyncTcpResponse;
import com.data.util.net.RequestParamTools;
import com.data.util.net.RsProtocolContext;
import com.data.util.net.bean.ProtocolContext;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.LocationMessageBody;
import com.easemob.chat.VideoMessageBody;
import com.easemob.util.VoiceRecorder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.juns.wechat.App;
import com.juns.wechat.Constants;
import com.juns.wechat.R;
import com.juns.wechat.chat.AlertDialog;
import com.juns.wechat.chat.adpter.VoicePlayClickListener;
import com.juns.wechat.chat.utils.CommonUtils;
import com.juns.wechat.chat.widght.PasteEditText;
import com.juns.wechat.common.Utils;
import com.juns.wechat.dialog.TitleMenu.ActionItem;
import com.juns.wechat.dialog.TitleMenu.TitlePopup;

import org.apache.http.message.BasicNameValuePair;
import org.litepal.crud.DataSupport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

//聊天页面
public class ChatActivity extends Activity implements OnClickListener {

    private static final int REQUEST_CODE_EMPTY_HISTORY = 2;
    public static final int REQUEST_CODE_CONTEXT_MENU = 3;
    public static final int REQUEST_CODE_TEXT = 5;
    public static final int REQUEST_CODE_VOICE = 6;
    public static final int REQUEST_CODE_PICTURE = 7;
    public static final int REQUEST_CODE_LOCATION = 8;
    public static final int REQUEST_CODE_FILE = 10;
    public static final int REQUEST_CODE_COPY_AND_PASTE = 11;
    public static final int REQUEST_CODE_VIDEO = 14;
    public static final int REQUEST_CODE_CAMERA = 18;
    public static final int REQUEST_CODE_LOCAL = 19;
    public static final int REQUEST_CODE_GROUP_DETAIL = 21;
    public static final int REQUEST_CODE_SELECT_FILE = 24;


    public static final int RESULT_CODE_EXIT_GROUP = 7;

    public static final int CHATTYPE_SINGLE = 1;
    public static final int CHATTYPE_GROUP = 2;

    public static final String COPY_IMAGE = "EASEMOBIMG";
    private View recordingContainer;
    private ImageView micImage;
    private TextView recordingHint;
    private ListView listView;
    private List<Message> messagelist;
    private PasteEditText mEditTextContent;
    private View buttonSetModeKeyboard;
    private View buttonSetModeVoice;
    private View buttonSend;
    private AudioRecorderButton buttonPressToSpeak;
    // private ViewPager expressionViewpager;
    private LinearLayout emojiIconContainer;
    private LinearLayout btnContainer;
    // private ImageView locationImgview;
    private View more;
    private ClipboardManager clipboard;
    private ViewPager expressionViewpager;
    private InputMethodManager manager;
    private int chatType;
    private EMConversation conversation;
    public static ChatActivity activityInstance = null;
    // 给谁发送消息
    private String Name;
    private String toChatUsername;
    private VoiceRecorder voiceRecorder;
    private MessageAdapter adapter;
    private File cameraFile;
    static int resendPos;

    private TextView txt_title;
    private ImageView img_right;
    private RelativeLayout edittext_layout;
    private ProgressBar loadmorePB;
    private boolean isloading;
    private final int pagesize = 20;
    private boolean haveMoreData = true;
    private Button btnMore;
    public String playMsgId;
    private AnimationDrawable animationDrawable;
    private Handler micImageHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
        }
    };
    private View animaView;
    private long chatId;
    private long messagesMaxId = 0;
    private Friend friend;
    private TitlePopup titlePopup;
    private boolean isManager;
    private long id = 0;

    // private EMGroup group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        EventBus.getDefault().register(this);
        initView();
        setUpView();
        setListener();
        initPopWindow();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Utils.finish(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * initView
     */
    protected void initView() {
        messagelist = new ArrayList<>();
        chatId = getIntent().getLongExtra(Constants.User_ID, 0);
        List<Message> messages = null;
        Log.e("abc","chatId:"+chatId);
        if(chatId < 0){
            messages = DataSupport.order("msg_date desc").where("chat_id = ?", ""+chatId)
                    .offset(messagelist.size()).limit(10)
                    .find(Message.class);
        }else if(chatId>10000){
            messages = DataSupport.order("msg_date desc").where("(chat_id = ? or sender_id = ?) and chat_id > 0", ""+chatId,""+chatId)
                    .offset(messagelist.size()).limit(10)
                    .find(Message.class);
        }else{
            messages = DataSupport.order("msg_date desc").where("chat_id = ?", ""+chatId)
                    .offset(messagelist.size()).limit(10)
                    .find(Message.class);
        }



        List<Friend> friends = DataSupport.where("friendId = ?", chatId + "").find(Friend.class);

        txt_title = (TextView) findViewById(R.id.txt_title);
        if (friends.size() > 0) {
            friend = friends.get(0);
            id = friend.getFriendId();
            Log.e("abc",friend.toString());
            User user = DataSupport.findFirst(User.class);
            if(friend.getManger_ids()!= null &&friend.getManger_ids().contains(String.valueOf(user.getUser_id()))){
                isManager = true;
            }else{
                isManager = false;
            }
            txt_title.setText(friends.get(0).getName());
        }else{
            friend = new Friend(chatId,"系统管理员");
            id = friend.getFriendId();
            txt_title.setText(friend.getName());
        }
        Collections.reverse(messages);
        if (messages.size() > 0) {
            messagesMaxId = messages.get(messages.size()-1).getMsg_date();
        }
        messagelist.addAll(messages);


        recordingContainer = findViewById(R.id.view_talk);
        img_right = (ImageView) findViewById(R.id.img_right);
        micImage = (ImageView) findViewById(R.id.mic_image);
        animationDrawable = (AnimationDrawable) micImage.getBackground();
        animationDrawable.setOneShot(false);
        recordingHint = (TextView) findViewById(R.id.recording_hint);
        listView = (ListView) findViewById(R.id.list);
        mEditTextContent = (PasteEditText) findViewById(R.id.et_sendmessage);
        buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
        edittext_layout = (RelativeLayout) findViewById(R.id.edittext_layout);
        buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
        buttonSend = findViewById(R.id.btn_send);
        buttonPressToSpeak = (AudioRecorderButton) findViewById(R.id.btn_press_to_speak);
        expressionViewpager = (ViewPager) findViewById(R.id.vPager);
        emojiIconContainer = (LinearLayout) findViewById(R.id.ll_face_container);
        btnContainer = (LinearLayout) findViewById(R.id.ll_btn_container);
        // locationImgview = (ImageView) findViewById(R.id.btn_location);
        loadmorePB = (ProgressBar) findViewById(R.id.pb_load_more);
        btnMore = (Button) findViewById(R.id.btn_more);
        more = findViewById(R.id.more);
        edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);

        edittext_layout.requestFocus();
        voiceRecorder = new VoiceRecorder(micImageHandler);
        buttonPressToSpeak.setOnAudioFinishRecorderListener(new AudioRecorderButton.AudioFinishRecorderListener() {

            @Override
            public void onFinish(float seconds, String filePath) {
                File file = new File(filePath);
                String fileName = file.getName();
                String prefix = fileName.substring(fileName.lastIndexOf(".") + 1);
                getFileUploadRequest(prefix, File2byte(file),4,seconds);
            }
        });
        mEditTextContent.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    edittext_layout
                            .setBackgroundResource(R.drawable.input_bar_bg_active);
                } else {
                    edittext_layout
                            .setBackgroundResource(R.drawable.input_bar_bg_normal);
                }

            }
        });
        mEditTextContent.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                edittext_layout
                        .setBackgroundResource(R.drawable.input_bar_bg_active);
                more.setVisibility(View.GONE);
                emojiIconContainer.setVisibility(View.GONE);
                btnContainer.setVisibility(View.GONE);
            }
        });
        // 监听文字框
        mEditTextContent.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!TextUtils.isEmpty(s)) {
                    btnMore.setVisibility(View.GONE);
                    buttonSend.setVisibility(View.VISIBLE);
                } else {
                    btnMore.setVisibility(View.VISIBLE);
                    buttonSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    private void setUpView() {
        activityInstance = this;
        // position = getIntent().getIntExtra("position", -1);
        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "demo");
        // 判断单聊还是群聊
        chatType = getIntent().getIntExtra(Constants.TYPE, CHATTYPE_SINGLE);
        Name = getIntent().getStringExtra(Constants.NAME);
        img_right.setVisibility(View.VISIBLE);
        toChatUsername = getIntent().getStringExtra(Constants.User_ID);

        adapter = new MessageAdapter(this, toChatUsername, messagelist,friend);
        // 显示消息
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new ListScrollListener());
        int count = listView.getCount();
        if (count > 0) {
            listView.setSelection(count);
        }

        listView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                more.setVisibility(View.GONE);
                emojiIconContainer.setVisibility(View.GONE);
                btnContainer.setVisibility(View.GONE);
                return false;
            }
        });


    }

    protected void setListener() {
        findViewById(R.id.img_back).setVisibility(View.VISIBLE);
        findViewById(R.id.img_back).setOnClickListener(this);
        findViewById(R.id.view_camera).setOnClickListener(this);
        findViewById(R.id.view_file).setOnClickListener(this);
        findViewById(R.id.view_photo).setOnClickListener(this);
        findViewById(R.id.img_back).setOnClickListener(this);
        img_right.setOnClickListener(this);
    }

    /**
     * onActivityResult
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        btnContainer.setVisibility(View.GONE);
        listView.setSelection(listView.getCount());
        if (resultCode == RESULT_CODE_EXIT_GROUP) {
            setResult(RESULT_OK);
            finish();
            return;
        }
        if (requestCode == REQUEST_CODE_CONTEXT_MENU) {
        }
        if (resultCode == RESULT_OK) { // 清空消息
            if (requestCode == REQUEST_CODE_EMPTY_HISTORY) {

            } else if (requestCode == REQUEST_CODE_CAMERA) { // 发送照片
                if (cameraFile != null && cameraFile.exists())
                    sendPicture(cameraFile.getAbsolutePath());
            } else if (requestCode == REQUEST_CODE_LOCAL) { // 发送本地图片
                if (data != null) {
                    Uri selectedImage = data.getData();
                    if (selectedImage != null) {
                        sendPicByUri(selectedImage);
                    }
                }
            } else if (requestCode == REQUEST_CODE_SELECT_FILE) { // 发送选择的文件
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        sendFile(uri);
                    }
                }

            } else if (requestCode == REQUEST_CODE_TEXT
                    || requestCode == REQUEST_CODE_VOICE
                    || requestCode == REQUEST_CODE_PICTURE
                    || requestCode == REQUEST_CODE_LOCATION
                    || requestCode == REQUEST_CODE_VIDEO
                    || requestCode == REQUEST_CODE_FILE) {
            } else if (requestCode == REQUEST_CODE_COPY_AND_PASTE) {
                // 粘贴
                if (!TextUtils.isEmpty(clipboard.getText())) {
                    String pasteText = clipboard.getText().toString();
                    if (pasteText.startsWith(COPY_IMAGE)) {
                        // 把图片前缀去掉，还原成正常的path
                        sendPicture(pasteText.replace(COPY_IMAGE, ""));
                    }

                }
            } else if (conversation.getMsgCount() > 0) {
                adapter.refresh();
                setResult(RESULT_OK);
            } else if (requestCode == REQUEST_CODE_GROUP_DETAIL) {
                adapter.refresh();
            }
        }
    }

    /**
     * 消息图标点击事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        hideKeyboard();
        switch (view.getId()) {
            case R.id.img_back:
                Utils.finish(ChatActivity.this);
                break;
            case R.id.img_right:
                if(chatId < 0){
//                    titlePopup.show(findViewById(R.id.top_bar));
                    Utils.start_Activity(ChatActivity.this,
                            GroupSettingActivity.class,new BasicNameValuePair("id",""+id));
                }
                break;
            case R.id.view_camera:
                selectPicFromCamera();// 点击照相图标
                break;
            case R.id.view_file:
                // 发送文件
                selectFileFromLocal();
                break;
            case R.id.view_photo:
                selectPicFromLocal(); // 点击图片图标
                break;
            case R.id.btn_send:
                // 点击发送按钮(发文字和表情)
                String s = mEditTextContent.getText().toString();
                sendText(s);
                break;
            default:
                break;
        }
    }

    private void initPopWindow() {
        // 实例化标题栏弹窗
        if(chatId < 0){

            titlePopup = new TitlePopup(this, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            titlePopup.setItemOnClickListener(onitemClick);
            // 给标题栏弹窗添加子类
            if(isManager){
                titlePopup.addAction(new ActionItem(this, "添加好友",
                        R.drawable.icon_menu_group));
                titlePopup.addAction(new ActionItem(this, "移出好友",
                        R.drawable.icon_menu_group));
            }else{
                titlePopup.addAction(new ActionItem(this, "退出此群",
                        R.drawable.icon_menu_group));
            }
        }else{
            img_right.setVisibility(View.GONE);
        }

    }
    private TitlePopup.OnItemOnClickListener onitemClick = new TitlePopup.OnItemOnClickListener() {

        @Override
        public void onItemClick(ActionItem item, int position) {
            // mLoadingDialog.show();
            switch (position) {
                case 0:// 发起群聊
                    if(isManager){
                        Utils.start_Activity(ChatActivity.this,
                                AddGroupChatActivity.class,new BasicNameValuePair("id",""+id));
                    }else{
                        Utils.start_Activity(ChatActivity.this,
                                ExitGroupAcyivity.class,new BasicNameValuePair("id",""+id));
                    }
                    break;
                case 1:// 发起群聊
                    Utils.start_Activity(ChatActivity.this,
                            RemoveGroupChatActivity.class,new BasicNameValuePair("id",""+id));
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 照相获取图片
     */
    public void selectPicFromCamera() {
        if (!CommonUtils.isExitsSdcard()) {
            String st = getResources().getString(
                    R.string.sd_card_does_not_exist);
//			Toast.makeText(getApplicationContext(), st, 0).show();
            return;
        }

        int currentApiVersion = Build.VERSION.SDK_INT;

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 拍照

        cameraFile = new File(Environment.getExternalStorageDirectory(), "Walk"
                + System.currentTimeMillis() + ".jpg");

        if (currentApiVersion < 24) {
            Uri uri = Uri.fromFile(cameraFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    uri);
        } else {
            Uri imageUri = FileProvider.getUriForFile(getApplicationContext(),
                    getApplicationContext().getPackageName() + ".fileprovider",
                    cameraFile);
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        }
        startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
    }

    /**
     * 选择文件
     */
    private void selectFileFromLocal() {
        Intent intent = null;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
    }

    /**
     * 从图库获取图片
     */
    public void selectPicFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_LOCAL);
    }

    /**
     * 发送文本消息
     *
     * @param content message content
     *                boolean resend
     */
    private void sendText(String content) {
        if (content.length() > 0) {
            getSendMessageRequest(chatId, content, 1);

            mEditTextContent.setText("");
            setResult(RESULT_OK);
        }
    }


    /**
     * 发送图片
     *
     * @param filePath
     */
    private void sendPicture(final String filePath) {
        Log.e("abc","图片发送地址:"+filePath);
        File file = new File(filePath);
        String fileName = file.getName();
        String prefix = fileName.substring(fileName.lastIndexOf(".") + 1);
        getFileUploadRequest(prefix, File2byte(file),2,0);
    }

    /**
     * 发送视频消息
     */
    private void sendVideo(final String filePath, final String thumbPath,
                           final int length) {
        final File videoFile = new File(filePath);
        if (!videoFile.exists()) {
            return;
        }
        try {
            EMMessage message = EMMessage
                    .createSendMessage(EMMessage.Type.VIDEO);
            // 如果是群聊，设置chattype,默认是单聊
            if (chatType == CHATTYPE_GROUP)
                message.setChatType(ChatType.GroupChat);
            String to = toChatUsername;
            message.setReceipt(to);
            VideoMessageBody body = new VideoMessageBody(videoFile, thumbPath,
                    length, videoFile.length());
            message.addBody(body);
            conversation.addMessage(message);
            listView.setAdapter(adapter);
            adapter.refresh();
            listView.setSelection(listView.getCount() - 1);
            setResult(RESULT_OK);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 根据图库图片uri发送图片
     *
     * @param selectedImage
     */
    private void sendPicByUri(Uri selectedImage) {
        // String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(selectedImage, null, null,
                null, null);
        String st8 = getResources().getString(R.string.cant_find_pictures);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex("_data");
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            cursor = null;

            if (picturePath == null || picturePath.equals("null")) {
                Toast toast = Toast.makeText(this, st8, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            sendPicture(picturePath);
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                Toast toast = Toast.makeText(this, st8, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;

            }
            sendPicture(file.getAbsolutePath());
        }

    }

    /**
     * 发送位置信息
     *
     * @param latitude
     * @param longitude
     * @param imagePath
     * @param locationAddress
     */
    private void sendLocationMsg(double latitude, double longitude,
                                 String imagePath, String locationAddress) {
        EMMessage message = EMMessage
                .createSendMessage(EMMessage.Type.LOCATION);
        // 如果是群聊，设置chattype,默认是单聊
        if (chatType == CHATTYPE_GROUP)
            message.setChatType(ChatType.GroupChat);
        LocationMessageBody locBody = new LocationMessageBody(locationAddress,
                latitude, longitude);
        message.addBody(locBody);
        message.setReceipt(toChatUsername);
        conversation.addMessage(message);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listView.setSelection(listView.getCount() - 1);
        setResult(RESULT_OK);

    }

    /**
     * 发送文件
     *
     * @param uri
     */
    private void sendFile(Uri uri) {
        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = getContentResolver().query(uri, projection, null,
                        null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        File file = new File(filePath);
        if (file == null || !file.exists()) {
            String st7 = getResources().getString(R.string.File_does_not_exist);
//			Toast.makeText(getApplicationContext(), st7, 0).show();
            return;
        }
        if (file.length() > 10 * 1024 * 1024) {
            String st6 = getResources().getString(
                    R.string.The_file_is_not_greater_than_10_m);
//			Toast.makeText(getApplicationContext(), st6, 0).show();
            return;
        }
        String fileName = file.getName();
        String prefix = fileName.substring(fileName.lastIndexOf(".") + 1);
        getFileUploadRequest(prefix, File2byte(file),3,0);
        listView.setAdapter(adapter);
        adapter.refresh();
        listView.setSelection(listView.getCount() - 1);
        setResult(RESULT_OK);
    }

    public byte[] File2byte(File file) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }


    /**
     * 显示语音图标按钮
     *
     * @param view
     */
    public void setModeVoice(View view) {
        hideKeyboard();
        edittext_layout.setVisibility(View.GONE);
        more.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        buttonSetModeKeyboard.setVisibility(View.VISIBLE);
        buttonSend.setVisibility(View.GONE);
        btnMore.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.VISIBLE);
        btnContainer.setVisibility(View.VISIBLE);
        emojiIconContainer.setVisibility(View.GONE);

    }

    /**
     * 显示键盘图标
     *
     * @param view
     */
    public void setModeKeyboard(View view) {
        edittext_layout.setVisibility(View.VISIBLE);
        more.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        buttonSetModeVoice.setVisibility(View.VISIBLE);
        // mEditTextContent.setVisibility(View.VISIBLE);
        mEditTextContent.requestFocus();
        // buttonSend.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.GONE);
        if (TextUtils.isEmpty(mEditTextContent.getText())) {
            btnMore.setVisibility(View.VISIBLE);
            buttonSend.setVisibility(View.GONE);
        } else {
            btnMore.setVisibility(View.GONE);
            buttonSend.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 点击清空聊天记录
     *
     * @param view
     */
    public void emptyHistory(View view) {
        String st5 = getResources().getString(
                R.string.Whether_to_empty_all_chats);
        startActivityForResult(
                new Intent(this, AlertDialog.class)
                        .putExtra("titleIsCancel", true).putExtra("msg", st5)
                        .putExtra("cancel", true), REQUEST_CODE_EMPTY_HISTORY);
    }

    /**
     * 点击进入群组详情
     *
     * @param view
     */
    public void toGroupDetails(View view) {
        // startActivityForResult(
        // (new Intent(this, GroupDeatilActivity.class).putExtra(
        // "groupId", toChatUsername)), REQUEST_CODE_GROUP_DETAIL);
    }

    /**
     * 显示或隐藏图标按钮页
     *
     * @param view
     */
    public void more(View view) {
        if (more.getVisibility() == View.GONE) {
            System.out.println("more gone");
            hideKeyboard();
            more.setVisibility(View.VISIBLE);
            btnContainer.setVisibility(View.VISIBLE);
            emojiIconContainer.setVisibility(View.GONE);
        } else {
            if (emojiIconContainer.getVisibility() == View.VISIBLE) {
                emojiIconContainer.setVisibility(View.GONE);
                btnContainer.setVisibility(View.VISIBLE);
            } else {
                more.setVisibility(View.GONE);
            }

        }

    }

    /**
     * 点击文字输入框
     *
     * @param v
     */
    public void editClick(View v) {
        listView.setSelection(listView.getCount() - 1);
        if (more.getVisibility() == View.VISIBLE) {
            more.setVisibility(View.GONE);
        }

    }


    private PowerManager.WakeLock wakeLock;


    public List<String> getExpressionRes(int getSum) {
        List<String> reslist = new ArrayList<String>();
        for (int x = 0; x <= getSum; x++) {
            String filename = "f_static_0" + x;

            reslist.add(filename);

        }
        return reslist;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        activityInstance = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (wakeLock.isHeld())
            wakeLock.release();
        if (VoicePlayClickListener.isPlaying
                && VoicePlayClickListener.currentPlayListener != null) {
            // 停止语音播放
            VoicePlayClickListener.currentPlayListener.stopPlayVoice();
        }

        try {
            // 停止录音
            if (voiceRecorder.isRecording()) {
                voiceRecorder.discardRecording();
                recordingContainer.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
        }
    }

    /**
     * 隐藏软键盘
     */
    private void hideKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    /**
     * 返回
     *
     * @param view
     */
    public void back(View view) {
        finish();
    }

    /**
     * 覆盖手机返回键
     */
    @Override
    public void onBackPressed() {
        if (more.getVisibility() == View.VISIBLE) {
            more.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * listview滑动监听listener
     */
    private class ListScrollListener implements OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case OnScrollListener.SCROLL_STATE_IDLE:
                    if (view.getFirstVisiblePosition() == 0 && !isloading
                            && haveMoreData) {
                        loadmorePB.setVisibility(View.VISIBLE);
                        // sdk初始化加载的聊天记录为20条，到顶时去db里获取更多
                        List<Message> messages;
                        try {
                            // 获取更多messges，调用此方法的时候从db获取的messages
                            // sdk会自动存入到此conversation中
                            if(chatId <0){
                                messages = DataSupport.order("msg_date desc").where("chat_id = ?", ""+chatId)
                                        .offset(messagelist.size()).limit(20)
                                        .find(Message.class);
                            }else if(chatId >10000){
                                messages = DataSupport.order("msg_date desc").where("(chat_id = ? or sender_id = ?) and chat_id > 0", ""+chatId,""+chatId)
                                        .offset(messagelist.size()).limit(20)
                                        .find(Message.class);
                            }else{
                                messages = DataSupport.order("msg_date desc").where("chat_id = ?", ""+chatId)
                                        .offset(messagelist.size()).limit(20)
                                        .find(Message.class);
                            }

                            Collections.reverse(messages);
                            messagelist.addAll(0,messages);
                        } catch (Exception e1) {
                            loadmorePB.setVisibility(View.GONE);
                            return;
                        }
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                        }
                        if (messages.size() != 0) {
                            // 刷新ui
                            adapter.notifyDataSetChanged();
                            listView.setSelection(messages.size() - 1);
                            if (messages.size() != pagesize)
                                haveMoreData = false;
                        } else {
                            haveMoreData = false;
                        }
                        loadmorePB.setVisibility(View.GONE);
                        isloading = false;

                    }
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        // 点击notification bar进入聊天页面，保证只有一个聊天页面
        String username = intent.getStringExtra("userId");
        if(username != null && toChatUsername != null){
            if (toChatUsername.equals(username))
                super.onNewIntent(intent);
            else {
                finish();
                startActivity(intent);
            }
        }else{
            super.onNewIntent(intent);
        }
    }


    public String getToChatUsername() {
        return toChatUsername;
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
                            ChatProtocol.FileUploadResponse fileUploadResponse = response.getFileUpload();
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("url",fileUploadResponse.getUrl());
                            jsonObj.put("other",other);
                            jsonObj.put("type",type);
                            getSendMessageRequest(chatId,jsonObj.toJSONString(),messageType);
                        } else {
                            TT.show(ChatActivity.this, "" + response.getErrorMessage());
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


    private void getSendMessageRequest(long chatid, String text, int type) {
        if (!App.isOnline()) {
            return;
        }
        RsProtocolContext protocolContext = RequestParamTools.getSendMessageRequest(chatid, text, type);
        if (protocolContext != null) {
            BeanMessageHandler.getAppClient().sendRequest(protocolContext, new PbAsyncTcpResponse() {
                @Override
                protected void onSuccess(ProtocolContext protocol) {
                    try {
                        ChatProtocol.Response response = ChatProtocol.Response.parseFrom(protocol.getBodyBuffer());
                        Log.e("abc", response.toString());
                        if (response.getErrorCode() == 0) {
                            ChatProtocol.SendMessageResponse sendMessageResponse = response.getSendMessage();
                            ChatProtocol.Message pbmessage = sendMessageResponse.getMessages();
                            Message message = new Message();
                            long chatid = pbmessage.getChatId();
                            message.setMessageId(pbmessage.getId());
                            message.setChat_id(pbmessage.getChatId());
                            message.setMsg_date(pbmessage.getMsgDate());
                            message.setRev_flag(pbmessage.getRevFlag());
                            message.setSender_id(pbmessage.getSenderId());
                            message.setText(pbmessage.getText());
                            message.setType(pbmessage.getType());
                            message.setTop_view(pbmessage.getTopView());
                            message.setDirect(2);
                            message.saveOrUpdate("messageId = ?",""+message.getMessageId());
                            MsgBean a = new MsgBean();
                            a.setMessage(message);
                            a.setIsnew(false);
                            a.setChat_id(chatid);
                            if(GlobObject.friendMap.get(chatid)!=null){
                                a.setFriend(GlobObject.friendMap.get(chatid));
                                GlobObject.msgMap.put(chatid,a);
                            }
                            EventBus.getDefault().post(new NewMessage());
                        } else {
                            TT.show(ChatActivity.this, "" + response.getErrorMessage());
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

    public void onEventMainThread(NewMessage newMessage) {
        //"chat_id = ? or sender_id = ?", ""+chatId,""+chatId
        List<Message> messages = null;
        if(chatId <0 ){
            messages = DataSupport.where("chat_id = ? and msg_date > ?",chatId + "", messagesMaxId + "").find(Message.class);
        }else if(chatId>10000){
            messages = DataSupport.where("(chat_id = ? or sender_id = ?) and msg_date > ? and chat_id > 0", chatId + "", chatId + "", messagesMaxId + "").find(Message.class);
        }else {
            messages = DataSupport.where("chat_id = ? and msg_date > ?",chatId + "", messagesMaxId + "").find(Message.class);
        }
        if (messages.size() > 0) {
            Collections.reverse(messages);
            for (int i = 0; i < messages.size(); i++) {
                if(messages.get(i).getMsg_date() > messagesMaxId){
                    messagesMaxId = messages.get(i).getMsg_date();
                    messagelist.add(messages.get(i));
                }
            }
        }
        adapter.refresh();
        listView.setSelection(listView.getCount() - 1);
    }

    public void onEventMainThread(UpdataGroupUser user){
        if(user != null){
            txt_title.setText(user.getName());
        }
    }

}
