package com.tttrtcgame.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tttrtcgame.LocalConfig;
import com.tttrtcgame.LocalConstans;
import com.tttrtcgame.R;
import com.tttrtcgame.bean.JniObjs;
import com.tttrtcgame.databinding.SplashActivityBinding;
import com.tttrtcgame.dialog.TestDialog;
import com.tttrtcgame.helper.TTTRtcEngineHelper;
import com.tttrtcgame.utils.MyLog;
import com.tttrtcgame.utils.SharedPreferencesUtil;
import com.wushuangtech.jni.RoomJni;
import com.wushuangtech.library.Constants;
import com.wushuangtech.wstechapi.TTTRtcEngine;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class SplashActivity extends BaseActivity {

    private SplashActivityBinding mBinder;
    private Context mContext;
    private boolean mIsLoging;
    private TestDialog testDialog;
    private TTTRtcEngineHelper mTTTRtcEngineHelper;
    private View mLastUserRoleView;
    private View mLastRoomTypeView;
    private View mLastRoomGameTypeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinder = DataBindingUtil.setContentView(this, R.layout.splash_activity);
        mContext = this;
        mTTTRtcEngineHelper = new TTTRtcEngineHelper(this);

        // 权限申请
        AndPermission.with(this)
                .permission(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .onGranted(permissions -> {

                })
                .start();

        // 读取保存的数据
        long roomID = SharedPreferencesUtil.getLong(this, "RoomID", 0);
        // 设置保存的数据
        if (roomID != 0) {
            String s = String.valueOf(roomID);
            mBinder.splashRoomIdEt.setText(s);
            mBinder.splashRoomIdEt.setSelection(s.length());
        }

        mLastUserRoleView = mBinder.splashRolePlayerTv;
        mLastRoomTypeView = mBinder.splashRoomTypeVideoRb;
        mLastRoomGameTypeView = mBinder.splashGameTypeOneTv;
        mTTTEngine.setVideoProfile(Constants.VIDEO_PROFILE_120P, true);

        TextView mVersion = findViewById(R.id.version);
        String string = getResources().getString(R.string.version_info);
        String result = String.format(string, TTTRtcEngine.getInstance().getVersion());
        mVersion.setText(result);

        // TODO 设置日志收集,发布时删除以下代码
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        String abs = externalStorageDirectory.toString() + "/TTTGameLog.txt";
        File file = new File(abs);
        if (file.exists()) {
            file.delete();
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mTTTEngine.setLogFile(abs);

        MyLog.d("SplashActivity onCreate...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyLog.d("SplashActivity onDestroy...");
        TTTRtcEngine.destroy();
        System.exit(0);
    }

    protected void initEngine() {
        if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_VIDEO) {
            mTTTEngine.enableVideo();
        } else if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_AUDIO) {
            mTTTEngine.disableVideo();
        }
    }

    public void onClickUserRoleButton(View v) {
        mLastUserRoleView.setBackgroundResource(R.drawable.splash_com_btn_uncheck);
        switch (v.getId()) {
            case R.id.splash_role_player_tv:
                LocalConfig.mLoginRole = Constants.CLIENT_ROLE_BROADCASTER;
                break;
            case R.id.splash_role_audience_tv:
                LocalConfig.mLoginRole = Constants.CLIENT_ROLE_AUDIENCE;
                break;
        }
        mLastUserRoleView = v;
        mLastUserRoleView.setBackgroundResource(R.drawable.splash_com_btn_checked);
    }

    public void onClickRoomTypeButton(View v) {
        mLastRoomTypeView.setBackgroundResource(R.drawable.splash_com_btn_uncheck);
        switch (v.getId()) {
            case R.id.splash_room_type_video_rb:
                LocalConfig.mLoginRoomType = LocalConstans.ROOM_TYPE_VIDEO;
                break;
            case R.id.splash_room_type_audio_rb:
                LocalConfig.mLoginRoomType = LocalConstans.ROOM_TYPE_AUDIO;
                break;
            case R.id.splash_room_type_chat_rb:
                LocalConfig.mLoginRoomType = LocalConstans.ROOM_TYPE_CHAT;
                break;
        }
        mLastRoomTypeView = v;
        mLastRoomTypeView.setBackgroundResource(R.drawable.splash_com_btn_checked);
    }

    public void onClickRoomGameTypeButton(View v) {
        mLastRoomGameTypeView.setBackgroundResource(R.drawable.splash_com_btn_uncheck);
        switch (v.getId()) {
            case R.id.splash_game_type_one_tv:
                LocalConfig.mLoginRoomGameType = LocalConstans.ROOM_GAME_TYPE_MULTIPLAYER;
                break;
            case R.id.splash_game_type_four_tv:
                LocalConfig.mLoginRoomGameType = LocalConstans.ROOM_GAME_TYPE_CHESS;
                break;
            case R.id.splash_game_type_five_tv:
                LocalConfig.mLoginRoomGameType = LocalConstans.ROOM_GAME_TYPE_SOCIAL;
                break;
            case R.id.splash_game_type_two_tv:
                LocalConfig.mLoginRoomGameType = LocalConstans.ROOM_GAME_TYPE_COMPTITIVE;
                break;
            case R.id.splash_game_type_three_tv:
                LocalConfig.mLoginRoomGameType = LocalConstans.ROOM_GAME_TYPE_ACTION;
                break;
        }
        mLastRoomGameTypeView = v;
        mLastRoomGameTypeView.setBackgroundResource(R.drawable.splash_com_btn_checked);
    }

    public void onClickEnterButton(View v) {

        boolean checkResult = mTTTRtcEngineHelper.splashCheckSetting(mBinder.splashRoomIdEt.getText().toString());
        if (!checkResult) {
            return;
        }

        // TODO 发布时删除以下代码
        if (!TextUtils.isEmpty(LocalConfig.mIP)) {
            MyLog.d("setServerAddress ip : " + LocalConfig.mIP + " | port : " + LocalConfig.mPort);
            RoomJni.getInstance().setServerAddress(LocalConfig.mIP, LocalConfig.mPort);
        }
        initEngine();
        boolean enableChat = false;
        if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_CHAT) {
            enableChat = true;
            RoomJni.getInstance().setServerAddress("39.107.64.215", 5000);
        }

        Random mRandom = new Random();
        LocalConfig.mLoginUserID = mRandom.nextInt(899) + 100;
        // 保存配置
        SharedPreferencesUtil.saveLong(this, "RoomID", LocalConfig.mLoginRoomID);
        if (mIsLoging) {
            return;
        }

        mTTTEngine.setClientRole(LocalConfig.mLoginRole, "");
        mIsLoging = true;
        mTTTRtcEngineHelper.splashShowWaittingDialog();
        boolean finalEnableChat = enableChat;
        new Thread(() -> {
            // 设置频道类型
            int results = mTTTEngine.setChannelProfile(Constants.CHANNEL_PROFILE_GAME_FREE_MODE);
//            MyLog.d("setChannelProfile result : " + results);
            // 进入频道
            int result = mTTTEngine.joinChannel("", String.valueOf(LocalConfig.mLoginRoomID), LocalConfig.mLoginUserID, finalEnableChat, false);
            MyLog.d("joinChannel result : " + result
                    + " | roomID : " + String.valueOf(LocalConfig.mLoginRoomID) + " | user id : " + String.valueOf(LocalConfig.mLoginUserID));
        }).start();

    }

    @Override
    protected void receiveCallBack(JniObjs mJniObjs) {
        switch (mJniObjs.mJniType) {
            case LocalConstans.CALL_BACK_ON_ENTER_ROOM:
                mTTTRtcEngineHelper.splashDismissWaittingDialog();
                mIsLoging = false;
                if (LocalConfig.mLoginRole == Constants.CLIENT_ROLE_AUDIENCE) {
                    mTTTEngine.muteLocalAudioStream(true);
                    mTTTEngine.muteLocalVideoStream(true);
                } else {
                    mTTTEngine.muteLocalAudioStream(false);
                    mTTTEngine.muteLocalVideoStream(false);
                    if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_AUDIO
                            || LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_CHAT) {
                        mTTTEngine.muteLocalVideoStream(true);
                    }

                    if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_SOCIAL
                            || LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_CHAT) {
                        // 由于是按键发言，所以在进房间前，本地用户需要静音
                        mTTTEngine.muteLocalAudioStream(true);
                    } else {
                        mTTTEngine.muteLocalAudioStream(false);
                    }
                }

                if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_VIDEO) {
                    if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_SOCIAL) {
                        startActivity(new Intent(mContext, MainActivity.class));
                    } else if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_MULTIPLAYER) {
                        startActivity(new Intent(mContext, MultiplayerActivity.class));
                    } else if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_COMPTITIVE) {
                        startActivity(new Intent(mContext, MultiplayerActivity.class));
                    } else if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_ACTION) {
                        startActivity(new Intent(mContext, MultiplayerActivity.class));
                    } else if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_CHESS) {
                        startActivity(new Intent(mContext, MultiplayerActivity.class));
                    }
                } else if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_AUDIO) {
                    if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_SOCIAL) {
                        startActivity(new Intent(mContext, MainActivity.class));
                    } else if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_MULTIPLAYER) {
                        startActivity(new Intent(mContext, MultiplayerActivity.class));
                    } else if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_COMPTITIVE) {
                        startActivity(new Intent(mContext, MultiplayerActivity.class));
                    } else if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_ACTION) {
                        startActivity(new Intent(mContext, MultiplayerActivity.class));
                    } else if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_CHESS) {
                        startActivity(new Intent(mContext, MultiplayerActivity.class));
                    }
                } else if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_CHAT) {
                    if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_SOCIAL) {
                        startActivity(new Intent(mContext, MainActivity.class));
                    } else {
                        startActivity(new Intent(mContext, MultiplayerActivity.class));
                    }
                }
                break;
            case LocalConstans.CALL_BACK_ON_ERROR:
                mIsLoging = false;
                mTTTRtcEngineHelper.splashDismissWaittingDialog();
                final int errorType = mJniObjs.mErrorType;
                runOnUiThread(() -> {
                    if (errorType == Constants.ERROR_ENTER_ROOM_TIMEOUT) {
                        Toast.makeText(mContext, "超时，10秒未收到服务器返回结果", Toast.LENGTH_SHORT).show();
                    } else if (errorType == Constants.ERROR_ENTER_ROOM_UNKNOW) {
                        Toast.makeText(mContext, "无法连接服务器", Toast.LENGTH_SHORT).show();
                    } else if (errorType == Constants.ERROR_ENTER_ROOM_VERIFY_FAILED) {
                        Toast.makeText(mContext, "验证码错误", Toast.LENGTH_SHORT).show();
                    } else if (errorType == Constants.ERROR_ENTER_ROOM_BAD_VERSION) {
                        Toast.makeText(mContext, "版本错误", Toast.LENGTH_SHORT).show();
                    } else if (errorType == Constants.ERROR_ENTER_ROOM_UNKNOW) {
                        Toast.makeText(mContext, "该房间不存在", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    /**
     * Author: wangzg <br/>
     * Time: 2017-11-24 10:17:28<br/>
     * Description: 测试用，不用关注该函数
     */
    public void onTestButtonClick(View v) {
        if (testDialog == null) {
            testDialog = new TestDialog(this);
            testDialog.setCanceledOnTouchOutside(false);
        } else {
            testDialog.setServerParams();
        }
        testDialog.show();
    }
}
