package com.tttrtcgame.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.jaeger.library.StatusBarUtil;
import com.tttrtcgame.LocalConfig;
import com.tttrtcgame.MainApplication;
import com.tttrtcgame.R;
import com.tttrtcgame.bean.JniObjs;
import com.tttrtcgame.callback.MyTTTRtcEngineEventHandler;
import com.tttrtcgame.dialog.ExitRoomDialog;
import com.wushuangtech.library.Constants;
import com.wushuangtech.wstechapi.TTTRtcEngineForGamming;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wangzhiguo on 17/10/12.
 */

public abstract class BaseActivity extends FragmentActivity {

    protected TTTRtcEngineForGamming mTTTEngine;
    protected Context mContext;
    protected ArrayList<View> mExcludeView = new ArrayList<>();;
    private Timer mTimer;
    protected View mTitleBarLy;
    protected TextView mAudienceNums;
    protected int mAudienceNum;
    private ExitRoomDialog mExitRoomDialog;
    private BroadcastReceiver mLocalBroadcast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (this.getClass().getSimpleName().equals(SplashActivity.class.getSimpleName())) {
            StatusBarUtil.setTranslucent(this);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //获取上下文
        mContext = this;
        //获取SDK实例对象
        mTTTEngine = TTTRtcEngineForGamming.getInstance();
        mLocalBroadcast = new MyLocalBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyTTTRtcEngineEventHandler.TAG);
        registerReceiver(mLocalBroadcast, filter);
        ((MainApplication) getApplicationContext()).mMyTTTRtcEngineEventHandler.setIsSaveCallBack(false);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        if (!this.getClass().getSimpleName().equals(SplashActivity.class.getSimpleName())) {
            mExitRoomDialog = new ExitRoomDialog(mContext, R.style.NoBackGroundDialog);
            mExitRoomDialog.setCanceledOnTouchOutside(false);
            mExitRoomDialog.mConfirmBT.setOnClickListener(v -> {
                exitRoom();
                mExitRoomDialog.dismiss();
            });

            mExitRoomDialog.mDenyBT.setOnClickListener(v -> mExitRoomDialog.dismiss());

            mTitleBarLy = findViewById(R.id.titlebar_layout);
            mExcludeView.add(mTitleBarLy);
            mAudienceNums = findViewById(R.id.titlebar_audiences);
            if (LocalConfig.mLoginRole == Constants.CLIENT_ROLE_AUDIENCE) {
                addAudienceNums();
            } else {
                String string = getResources().getString(R.string.titlebar_audience_num);
                String result = String.format(string, 0);
                mAudienceNums.setText(result);
            }

            findViewById(R.id.titlebar_exit).setOnClickListener(v -> {
                mExitRoomDialog.show();
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (mExitRoomDialog != null) {
            mExitRoomDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLocalBroadcast);
    }

    /*@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return judgeClick(mExcludeView, ev) || super.dispatchTouchEvent(ev);
    }*/

    protected abstract void receiveCallBack(JniObjs mJniObjs);

    protected void exitRoom() {
        mTTTEngine.leaveChannel();
        finish();
    }

    protected boolean judgeClick(ArrayList<View> mExcludeView, MotionEvent ev) {
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN
                && !this.getClass().getSimpleName().equals(SplashActivity.class.getSimpleName())) {
            boolean isExecute = true;
            for (int i = 0; i < mExcludeView.size(); i++) {
                View view = mExcludeView.get(i);
                if (view != null) {
                    boolean b = inRangeOfView(view, ev);
                    if (b) {
                        isExecute = false;
                    }
                }
            }

            if (isExecute) {
                if (mTitleBarLy.getVisibility() == View.VISIBLE) {
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }
                    mTitleBarLy.setVisibility(View.GONE);
                } else {
                    mTitleBarLy.setVisibility(View.VISIBLE);
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(() -> mTitleBarLy.setVisibility(View.GONE));
                        }
                    }, 5000);
                }
                return true;
            }
        }
        return false;
    }

    private boolean inRangeOfView(View view, MotionEvent ev) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        return !(ev.getX() < x || ev.getX() > (x + view.getWidth()) || ev.getY() < y || ev.getY() > (y + view.getHeight()));
    }

    protected void setAudienceNums() {
        String string = getResources().getString(R.string.titlebar_audience_num);
        String result = String.format(string, mAudienceNum);
        mAudienceNums.setText(result);
    }

    protected synchronized void addAudienceNums() {
        mAudienceNum++;
        setAudienceNums();
    }

    protected synchronized void reduceAudienceNums() {
        mAudienceNum--;
        setAudienceNums();
    }

    private class MyLocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MyTTTRtcEngineEventHandler.TAG.equals(action)) {
                JniObjs mJniObjs = intent.getParcelableExtra(
                        MyTTTRtcEngineEventHandler.MSG_TAG);
                receiveCallBack(mJniObjs);
            }
        }
    }
}
