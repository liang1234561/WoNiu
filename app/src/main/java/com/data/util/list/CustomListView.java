package com.data.util.list;

import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.juns.wechat.R;

public class CustomListView extends ListView implements OnScrollListener {  
	  
    private static final int DONE = 0;  
    private static final int PULL_TO_REFRESH = 1;  
    private static final int RELEASE_TO_REFRESH = 2;  
    private static final int REFRESHING = 3;  
    private static final float RATIO = 3;
  
    private int state;
  
    private int firstVisibleIndex;
    private View headView;  
    private ImageView headArrow;  
    private ProgressBar progressBar;  
    private TextView headTitle;  
    private TextView headLastUpdate;  
    private int headContentWidth;  
    private int headContentHeight;  
    private Animation animation;  
    private Animation reverseAnimation;  
    private OnRefreshListner refreshListner;
  
    private boolean isRefreshable;  
    private boolean isRecored = false;
    private float startY;  
    private boolean isBack = false;//
      
    public CustomListView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        init(context);  
    }  
  
    private void init(Context context) {  
        setCacheColorHint(0x00000000);
  
        headView = View.inflate(context, R.layout.head, null);
        headArrow = (ImageView) headView.findViewById(R.id.head_arrow);  
        progressBar = (ProgressBar) headView.findViewById(R.id.progressbar);  
        headTitle = (TextView) headView.findViewById(R.id.head_title);  
        headLastUpdate = (TextView) headView.findViewById(R.id.head_last_update);  
        headArrow.setMinimumWidth(50);  
        headArrow.setMinimumHeight(70);  
  
        MeasureView(headView);  
          
        headContentWidth = headView.getMeasuredWidth();  
        headContentHeight = headView.getMeasuredHeight();  
  
        headView.setPadding(0, -1*headContentHeight, 0, 0);  
        addHeaderView(headView);
  
        setOnScrollListener(this);  
  
        animation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF,  
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);  
        animation.setDuration(250);  
        animation.setFillAfter(true);//
        animation.setInterpolator(new LinearInterpolator());//
  
        reverseAnimation = new RotateAnimation(0, -180,  
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,  
                0.5f);  
        reverseAnimation.setDuration(200);  
        reverseAnimation.setFillAfter(true);//
        reverseAnimation.setInterpolator(new LinearInterpolator());//
        state = DONE;
        isRefreshable = false;
    }  
      
  
    /** 
     * ����headView�� ��� 
     *  
     * @param child 
     */  
    private void MeasureView(View child) {  
        ViewGroup.LayoutParams lp = child.getLayoutParams();  
  
        if (null == lp) {  
            lp = new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,  
                    LayoutParams.WRAP_CONTENT);  
        }  
  
        int measureChildWidth = ViewGroup.getChildMeasureSpec(0, 0, lp.width);  
        int measureChildHeight;  
  
        if (lp.height > 0) {  
            measureChildHeight = MeasureSpec.makeMeasureSpec(lp.height,  
                    MeasureSpec.EXACTLY);  
        } else {  
            measureChildHeight = MeasureSpec.makeMeasureSpec(0,  
                    MeasureSpec.UNSPECIFIED);  
        }  
  
        child.measure(measureChildWidth, measureChildHeight);  
  
    }  
  
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
  
        switch (event.getAction()) {  
        case MotionEvent.ACTION_DOWN:  
            if (firstVisibleIndex == 0 && !isRecored) {  
                startY = event.getY();  
                isRecored = true;  
            }  
  
            break;  
  
        case MotionEvent.ACTION_MOVE:  
            float tempY = event.getY();  
            if (firstVisibleIndex == 0 && !isRecored) {  
                startY = tempY;  
                isRecored = true;  
            }  
  
            if (state != REFRESHING) {  
                if (state == PULL_TO_REFRESH) {  
                    // �������� ������ˢ�µ�״̬ ���� �ɿ�ˢ�µ�״̬  
                    if ((tempY - startY) / RATIO >= headContentHeight  
                            && (tempY - startY) > 0) {  
                        state = RELEASE_TO_REFRESH;  
  
                        changeHeadViewOfState();  
                    }  
                    // �������� ������ˢ�µ�״̬ ���� ˢ����ɵ�״̬  
                    else if ((tempY - startY) <= 0) {  
                        state = DONE;  
  
                        changeHeadViewOfState();  
                    }  
  
                } else if (state == RELEASE_TO_REFRESH) {  
                    // �������� ��û����ȫ��HEADVIEW ���ص������Կ���һ���֣�  
                    // ���ɿ�ˢ�µ�״̬ ���� ����ˢ�µ�״̬  
                    if ((tempY - startY) / RATIO < headContentHeight  
                            && (tempY - startY) > 0) {  
                        state = PULL_TO_REFRESH;  
  
                        changeHeadViewOfState();  
                        isBack = true;  
                    }  
                    // �������� һ�����Ƶ��������� ���ɿ�ˢ�µ�״̬ ���� ˢ����ɵ�״̬ ����ݲ�ˢ�µģ�  
                    else if ((tempY - startY) <= 0) {  
                        state = DONE;  
  
                        changeHeadViewOfState();  
                    }  
  
                } else if (state == DONE) {  
                    // ˢ����ɵ�״̬ ���� ����ˢ�µ�״̬  
                    if ((tempY - startY) > 0) {  
                        state = PULL_TO_REFRESH;  
  
                        changeHeadViewOfState();  
                    }  
                }  
  
                if (state == PULL_TO_REFRESH) {  
                    headView.setPadding(  
                            0,  
                            (int) ((tempY - startY) / RATIO - headContentHeight),  
                            0, 0);  
                }  
  
                if (state == RELEASE_TO_REFRESH) {  
                    headView.setPadding(  
                            0,  
                            (int) ((tempY - startY) / RATIO - headContentHeight),  
                            0, 0);  
                }  
  
            }  
  
            break;  
  
        case MotionEvent.ACTION_UP:  
            if (state != REFRESHING) {  
                if (state == PULL_TO_REFRESH) {  
                    // ����  
                    state = DONE;  
  
                    changeHeadViewOfState();  
                }  
  
                else if (state == RELEASE_TO_REFRESH) {  
                    // ����  
                    state = REFRESHING;  
                    changeHeadViewOfState();  
  
                    // ִ�����ˢ�·���  
                    onRefresh();  
                }  
            }  
  
            isRecored = false;  
            isBack = false;  
  
            break;  
        }  
  
        return super.onTouchEvent(event);  
    }  
  
    /** 
     * ִ������ˢ�� 
     */  
    private void onRefresh() {  
        if (refreshListner != null) {  
            refreshListner.onRefresh();  
        }  
    }  
  
    /** 
     * HeadView��״̬�仯Ч�� 
     */  
    private void changeHeadViewOfState() {  
        //   
        switch (state) {  
  
        case PULL_TO_REFRESH:  
              
            headArrow.setVisibility(View.VISIBLE);  
            progressBar.setVisibility(View.GONE);  
            headTitle.setVisibility(View.VISIBLE);  
            headLastUpdate.setVisibility(View.VISIBLE);  
              
            headArrow.clearAnimation();  
            headTitle.setText("上拉刷新");
            //�� �ɿ�ˢ��  ��  ����ˢ��  
            if(isBack){  
                  
                headArrow.startAnimation(animation);  
                isBack = false;  
            }  
  
            break;  
  
        case RELEASE_TO_REFRESH:  
              
            headArrow.setVisibility(View.VISIBLE);  
            progressBar.setVisibility(View.GONE);  
            headTitle.setVisibility(View.VISIBLE);  
            headLastUpdate.setVisibility(View.VISIBLE);  
            headArrow.clearAnimation();  
            headArrow.startAnimation(reverseAnimation);  
            headTitle.setText("下拉加载");
  
            break;  
  
        case REFRESHING:  
  
            headArrow.setVisibility(View.GONE);  
            progressBar.setVisibility(View.VISIBLE);  
            headTitle.setVisibility(View.VISIBLE);  
            headLastUpdate.setVisibility(View.VISIBLE);  
              
            headArrow.clearAnimation();  
            headTitle.setText("加载中...");
              
            headView.setPadding(0, 0, 0, 0);  
              
            break;  
  
        case DONE:  
              
            headArrow.setVisibility(View.VISIBLE);  
            progressBar.setVisibility(View.GONE);  
            headTitle.setVisibility(View.VISIBLE);  
            headLastUpdate.setVisibility(View.VISIBLE);  
              
            headArrow.clearAnimation();  
            headTitle.setText("加载完成");
              
            headView.setPadding(0, -1 * headContentHeight, 0, 0);  
  
            break;  
        }  
  
    }  
  /*
   * ====================================================================
   */
    private int lastPos;//���һ���ɼ��item��λ��  
    private int count;//item����,ע�ⲻ�ǵ�ǰ�ɼ��item����  
    private boolean hasFoot = false;//�Ƿ�����Foot  
    private int scrollState;
    @Override  
    public void onScroll(AbsListView view, int firstVisibleItem,  
            int visibleItemCount, int totalItemCount) {  
        firstVisibleIndex = firstVisibleItem;  
        lastPos = getLastVisiblePosition();  
        count = totalItemCount;  
          
        //��Ϊ�ս����ʱ��,lastPos=-1,count=0,���ʱ��������ִ��onAddFoot����  
        if(lastPos==count-1 && !hasFoot && lastPos != -1){  
            hasFoot = true;  
            if(!isFootLoading){
            	onScrollStateChanged(view, SCROLL_STATE_IDLE);
            }  
//            Log.d("addFoot================","ִ�����Foot....");  
        }  
//        Log.d("count================", count+"");  
//        Log.d("lastPos================", lastPos+"");  
    }  
    @Override  
    public void onScrollStateChanged(AbsListView view, int scrollState) { 
    	this.scrollState = scrollState;
        if(isFootLoading)  
            return;  
          //��listview���������һ����ֹͣ����ʱ���ؽ�ע
        if(hasFoot && scrollState==SCROLL_STATE_IDLE){  
            isFootLoading = true;  
            onAddFoot();
            onStartFootLoading();  
        }  
    }  
  
    /** 
     * ��������ˢ�¼��� 
     *  
     * @param listener 
     */  
    public void setOnRefreshListner(OnRefreshListner listener) {  
        // ��������ˢ�¿���  
        isRefreshable = true;  
        refreshListner = listener;  
  
    }  
      
      
    //ִ�еײ�����  
    public void onStartFootLoading(){  
        if(footLoadingListener!=null && isFootLoading)  
            footLoadingListener.onFootLoading();  
    }  
      
    public void setOnAddFootListener(OnAddFootListener addFootListener){  
        onAddFootListener = addFootListener;  
    }  
      
    public void onAddFoot(){
        if(onAddFootListener!=null && hasFoot)  
            onAddFootListener.addFoot();  
    }  
    public OnAddFootListener onAddFootListener;
    public OnFootLoadingListener footLoadingListener;
    private boolean isFootLoading = false;
    public void setOnFootLoadingListener(OnFootLoadingListener footLoading){  
        footLoadingListener = footLoading;
    }
  
    /** 
     * ����ˢ�¼����� 
     * @author lxj 
     * 
     */  
    public interface OnRefreshListner {  
        /** 
         * ����ˢ�µ�ʱ��,������ִ�л�ȡ��ݵĹ�� 
         */  
        void onRefresh();  
    }  
      
    /** 
     * ����ˢ�¼����� 
     * @author lxj 
     * 
     */  
    public interface OnFootLoadingListener{  
        /** 
         * ������ִ�к�̨��ȡ��ݵĹ�� 
         */  
        void onFootLoading();  
    }  
      
    /** 
     * ���Foot�ļ����� 
     * @author lxj 
     * 
     */  
    public interface OnAddFootListener{  
        /** 
         * �������û�addFootView�Ĳ��� 
         */  
        void addFoot();  
    }  
      
    /** 
     * �ײ���ݼ������,�û���Ҫ����һ��removeFootView�Ĳ��� 
     */  
    public void onFootLoadingComplete(){  
        hasFoot = false;  
        isFootLoading = false;   
    }  
  
    /** 
     * ����ˢ�����ʱ ��ִ�еĲ���,���״̬,����head 
     */  
    public void onRefreshComplete() {  
        state = DONE;  
        changeHeadViewOfState();  
  
        headLastUpdate.setText("下拉加载" + new Date().toLocaleString());
    }  
  
    @Override  
    public void setAdapter(ListAdapter adapter) {  
  
        headLastUpdate.setText("下拉加载1 " + new Date().toLocaleString());
          
        super.setAdapter(adapter);  
    }  
}  
