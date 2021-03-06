package com.data.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.data.data.GlobObject;
import com.data.data.bean.MsgBean;
import com.data.db.Friend;
import com.data.db.Message;
import com.data.ui.activity.group.CreateGroupChatActivity;
import com.data.ui.fragment.Fragment_Friends;
import com.data.ui.fragment.Fragment_Msg;
import com.data.ui.fragment.Fragment_Profile;
import com.data.util.MobileSystemUtil;
import com.juns.wechat.App;
import com.juns.wechat.Constants;
import com.juns.wechat.R;
import com.juns.wechat.common.Utils;
import com.juns.wechat.dialog.TitleMenu.ActionItem;
import com.juns.wechat.dialog.TitleMenu.TitlePopup;
import com.juns.wechat.dialog.WarnTipDialog;

import org.apache.http.message.BasicNameValuePair;
import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class MainActivity extends FragmentActivity implements OnClickListener {
    private TextView txt_title;
    private ImageView img_right;
    private WarnTipDialog Tipdialog;
    protected static final String TAG = "MainActivity";
    private TitlePopup titlePopup;
    private Fragment[] fragments;
    public Fragment_Msg homefragment;
    private Fragment_Friends contactlistfragment;
    private Fragment_Profile profilefragment;
    private ImageView[] imagebuttons;
    private TextView[] textviews;
    private String connectMsg = "";
    ;
    private int index;
    private int currentTabIndex;// 当前fragment的index

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.getInstance2().addActivity(this);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                getData();
                findViewById();
                initViews();
                initTabView();
                initPopWindow();
                setOnListener();
                get("http://chat.tytools.cn/common/checkVersion.json?type=1&version="+ MobileSystemUtil.getVersion(getApplicationContext()),false);
            }
        });
    }

    private void initTabView() {
        homefragment = new Fragment_Msg();
        contactlistfragment = new Fragment_Friends();
        profilefragment = new Fragment_Profile();
        fragments = new Fragment[]{homefragment, contactlistfragment
                , profilefragment};
        imagebuttons = new ImageView[3];
        imagebuttons[0] = (ImageView) findViewById(R.id.ib_weixin);
        imagebuttons[1] = (ImageView) findViewById(R.id.ib_contact_list);
        imagebuttons[2] = (ImageView) findViewById(R.id.ib_profile);

        imagebuttons[0].setSelected(true);
        textviews = new TextView[3];
        textviews[0] = (TextView) findViewById(R.id.tv_weixin);
        textviews[1] = (TextView) findViewById(R.id.tv_contact_list);
        textviews[2] = (TextView) findViewById(R.id.tv_profile);
        textviews[0].setTextColor(0xFF45C01A);
        // 添加显示第一个fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homefragment)
                .add(R.id.fragment_container, contactlistfragment)
                .add(R.id.fragment_container, profilefragment)
                .hide(contactlistfragment).hide(profilefragment)
                .show(homefragment).commit();
    }

    public void onTabClicked(View view) {
        img_right.setVisibility(View.GONE);
        switch (view.getId()) {
            case R.id.re_weixin:
                index = 0;
                img_right.setVisibility(View.VISIBLE);
                if (homefragment != null) {
                    homefragment.refresh();
                }
                img_right.setImageResource(R.drawable.icon_add);
                txt_title.setText(R.string.app_name);
                break;
            case R.id.re_contact_list:
                index = 1;
                txt_title.setText(R.string.contacts);
                img_right.setVisibility(View.VISIBLE);
                img_right.setImageResource(R.drawable.icon_titleaddfriend);
                break;
            case R.id.re_profile:
                index = 2;
                txt_title.setText(R.string.me);
                break;
        }
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager()
                    .beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index]);
            }
            trx.show(fragments[index]).commit();
        }
        imagebuttons[currentTabIndex].setSelected(false);
        // 把当前tab设为选中状态
        imagebuttons[index].setSelected(true);
        textviews[currentTabIndex].setTextColor(0xFF999999);
        textviews[index].setTextColor(0xFF45C01A);
        currentTabIndex = index;
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void findViewById() {
        txt_title = (TextView) findViewById(R.id.txt_title);
        img_right = (ImageView) findViewById(R.id.img_right);
    }

    private void initViews() {
        // 设置消息页面为初始页面
        img_right.setVisibility(View.VISIBLE);
        img_right.setImageResource(R.drawable.icon_add);
    }

    private void setOnListener() {
        img_right.setOnClickListener(this);

    }

    private void initPopWindow() {
        // 实例化标题栏弹窗
        titlePopup = new TitlePopup(this, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        titlePopup.setItemOnClickListener(onitemClick);
        // 给标题栏弹窗添加子类
        titlePopup.addAction(new ActionItem(this, R.string.menu_groupchat,
                R.drawable.icon_menu_group));
    }
    private TitlePopup.OnItemOnClickListener onitemClick = new TitlePopup.OnItemOnClickListener() {

        @Override
        public void onItemClick(ActionItem item, int position) {
            // mLoadingDialog.show();
            switch (position) {
                case 0:// 发起群聊
                    Utils.start_Activity(MainActivity.this,
                            CreateGroupChatActivity.class);
                    break;
                default:
                    break;
            }
        }
    };

    private int keyBackClickCount = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switch (keyBackClickCount++) {
                case 0:
                    Toast.makeText(this, "再次按返回键退出", Toast.LENGTH_SHORT).show();
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            keyBackClickCount = 0;
                        }
                    }, 3000);
                    break;
                case 1:
//                    EMChatManager.getInstance().logout();// 退出环信聊天
//                    App.getInstance2().exit();
                    finish();
                    overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                    break;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_right:
                if (index == 0) {
                    titlePopup.show(findViewById(R.id.layout_bar));
                } else {
                    Utils.start_Activity(MainActivity.this, SearchActivity.class,
                            new BasicNameValuePair(Constants.NAME, "添加朋友"));
                }
                break;
            default:
                break;
        }
    }

    private void initVersion() {
        // TODO 检查版本更新
        String versionInfo = Utils.getValue(this, Constants.VersionInfo);
        if (!TextUtils.isEmpty(versionInfo)) {
            Tipdialog = new WarnTipDialog(this,
                    "发现新版本：  1、更新啊阿三达到阿德阿   2、斯顿阿斯顿撒旦？");
            Tipdialog.setBtnOkLinstener(onclick);
            Tipdialog.show();
        }
    }

    private DialogInterface.OnClickListener onclick = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Utils.showLongToast(MainActivity.this, "正在下载...");// TODO
            Tipdialog.dismiss();
        }
    };

    private void getData(){
        if(GlobObject.friendMap.isEmpty()){
            List<Message> messageList= DataSupport.where("chat_id > 0 and chat_id <10000 group by chat_id").find(Message.class);
            for (int i = 0; i < messageList.size(); i++) {
//                Message message = DataSupport.where("chat_id = ?", messageList.get(i).getChat_id()+"").findLast(Message.class);
                Message message = messageList.get(i);
                if(message != null){
                    MsgBean a = new MsgBean();
                    a.setChat_id(message.getChat_id());
                    Friend friend = DataSupport.where("friendId = ?", message.getChat_id() + "").findLast(Friend.class);
                    if (friend == null) {
                        a.setFriend(new Friend(message.getChat_id(),"系统管理员"));
                    }else{
                        a.setFriend(friend);
                    }
                    a.setMessage(message);
                    a.setIsnew(false);
                    GlobObject.msgMap.put(message.getChat_id(),a);
                }
            }


            GlobObject.friendList = DataSupport.findAll(Friend.class);
            for (int i = 0; i < GlobObject.friendList.size(); i++) {
                Log.e(TAG, "getData: "+ GlobObject.friendList.get(i).toString());
                Long  id = GlobObject.friendList.get(i).getFriendId();
                GlobObject.friendMap.put(id,GlobObject.friendList.get(i));
                Message messages = null;
                if(id>=10000){
                    messages = DataSupport.where("(chat_id = ? or sender_id = ?) and chat_id > 0", id+"", id+"").findLast(Message.class);
                }else if(id<0){
                    messages = DataSupport.where("chat_id = ?", id+"").findLast(Message.class);
                }
                if(messages != null){
                    MsgBean a = new MsgBean();
                    a.setChat_id(id);
                    a.setFriend(GlobObject.friendList.get(i));
                    a.setMessage(messages);
                    a.setIsnew(false);
                    GlobObject.msgMap.put(id,a);
                }
            }
        }
    }


    public void showVersionNoUpdate(String str,boolean click) {
        JSONObject jsonObject = JSONObject.parseObject(str);
        String url = "未发现新版本，当前安装的已是最新版本";
        if(jsonObject.get("code").equals("200")){
            if (jsonObject.get("url")!=null){
                click = true;
                url = jsonObject.getString("url");
            }
        }
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.alertDialogStyle).create();
        alertDialog.setCanceledOnTouchOutside(false);
        if(click){
            alertDialog.show();
        }
        Window window = alertDialog.getWindow();
        window.setContentView(R.layout.upgrade_app);
        alertDialog.setCanceledOnTouchOutside(true);
        ((TextView) window.findViewById(R.id.upgrade_app_text)).setText("提示");
        ((TextView) window.findViewById(R.id.upgrade_app_text_info)).setText(url);
        ((Button) window.findViewById(R.id.upgrade_app_commit)).setText("确定");
        ((Button) window.findViewById(R.id.upgrade_app_commit)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    public void get(final String url,boolean click) {
        Log.e(TAG, "getData: "+ url);
        final StringBuilder sb = new StringBuilder();
        FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                BufferedReader br = null;
                InputStreamReader isr = null;
                URLConnection conn;
                try {
                    URL geturl = new URL(url);
                    conn = geturl.openConnection();//创建连接
                    conn.connect();//get连接
                    isr = new InputStreamReader(conn.getInputStream());//输入流
                    br = new BufferedReader(isr);
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);//获取输入流数据
                    }
                    System.out.println(sb.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (br != null) {
                        try {
                            if (br != null) {
                                br.close();
                            }
                            if (isr != null) {
                                isr.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return sb.toString();
            }
        });
        new Thread(task).start();
        String s = null;
        try {
            s = task.get();
            showVersionNoUpdate(s,click);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}