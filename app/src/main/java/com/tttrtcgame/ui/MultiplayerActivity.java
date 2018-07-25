package com.tttrtcgame.ui;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tttrtcgame.LocalConfig;
import com.tttrtcgame.LocalConstans;
import com.tttrtcgame.R;
import com.tttrtcgame.bean.EnterUserInfo;
import com.tttrtcgame.bean.JniObjs;
import com.tttrtcgame.bean.MessageBean;
import com.tttrtcgame.helper.MultiplayerVideoLayout;
import com.tttrtcgame.utils.MyLog;
import com.wushuangtech.jni.RoomJni;
import com.wushuangtech.library.Constants;
import com.wushuangtech.wstechapi.model.VideoCanvas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_AUDIO_PLAY_COMPLATION;
import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_AUDIO_RECOGNIZED;
import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_AUDIO_VOLUME_INDICATION;
import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_CHAT_MESSAGE_RECIVED;
import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_CHAT_MESSAGE_SENT;
import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_ERROR;
import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_FIRST_VIDEO_FRAME;
import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_USER_JOIN;
import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_USER_OFFLINE;

/**
 * Created by wangzhiguo on 17/12/21.
 */

public class MultiplayerActivity extends BaseActivity {

    public static final int MSG_TYPE_ERROR_ENTER_ROOM = 0;
    private static final int CONTROL_VOICE_IMAGE_VISIBILE = 14;
    public static final int DISCONNECT = 100;
    private static final int MAX_VIDEO_NUM = 3;
    private static final int VOLUME_MAX_NUM = 9;
    private ViewGroup mActivityBG;
    private ViewGroup mActivityVideoBG;

    private Handler mHandler = new LocalHandler();
    private AlertDialog mErrorExitDialog;

    private boolean mIsExitRoom;
    private List<EnterUserInfo> mPersons;
    private Vector<Long> mPlayers;
    public ConcurrentHashMap<EnterUserInfo, MultiplayerVideoLayout> mShowingDevices;
    private boolean mIsPhoneComing;
    private ChatListFragment mChatListFragment;

    private boolean mIsFirstUsing;
    private boolean mIsSecondUsing;
    private boolean mIsThirdUsing;

    private TextView mShowChat;
    private AudioManager audioManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiplayer_activity);
        initView();
        initData();
        mTTTEngine.enableAudioVolumeIndication(300, 3);
        audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        RoomJni.getInstance().ClearGlobalStatus();
    }

    private void initView() {
        mActivityBG = findViewById(R.id.multiplayer_bg);
        mActivityVideoBG = findViewById(R.id.multiplayer_videoly);
        if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_CHAT) {

            mShowChat = findViewById(R.id.titlebar_showchat);

            FragmentManager fm = getSupportFragmentManager();
            if (mChatListFragment == null) {
                mChatListFragment = new ChatListFragment();
                if (LocalConfig.mLoginRole == Constants.CLIENT_ROLE_AUDIENCE)
                    mChatListFragment.hideSendControler();
                fm.beginTransaction().add(R.id.id_fragment_container, mChatListFragment).commit();
            }

            mShowChat.setVisibility(View.VISIBLE);
            mShowChat.setOnClickListener(view -> {
                if (mChatListFragment.isHidden()) {
                    fm.beginTransaction().show(mChatListFragment).commit();
                    mShowChat.setText("隐藏聊天");
                } else {
                    fm.beginTransaction().hide(mChatListFragment).commit();
                    mShowChat.setText("显示聊天");
                }
            });

        }
    }

    private void initData() {
        mPersons = new ArrayList<>();
        mPlayers = new Vector<>();
        mShowingDevices = new ConcurrentHashMap<>();
        if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_MULTIPLAYER) {
            mActivityBG.setBackgroundResource(R.drawable.multiplayer_bg);
        } else if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_COMPTITIVE) {
            mActivityBG.setBackgroundResource(R.drawable.comptitive_bg);
        } else if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_ACTION) {
            mActivityBG.setBackgroundResource(R.drawable.action_bg);
        } else if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_CHESS) {
            mActivityBG.setBackgroundResource(R.drawable.chess_bg);
        }
        mActivityBG.setOnTouchListener((view, motionEvent) -> {
            judgeClick(mExcludeView, motionEvent);
            return false;
        });
        EnterUserInfo mLocalUserInfo = new EnterUserInfo(LocalConfig.mLoginUserID, LocalConfig.mLoginRole);
        mPersons.add(mLocalUserInfo);
        if (LocalConfig.mLoginRole != Constants.CLIENT_ROLE_AUDIENCE && LocalConfig.mLoginRoomType != LocalConstans.ROOM_TYPE_CHAT) {
            mPlayers.add(mLocalUserInfo.getId());
            adJustRemoteViewDisplay(true, mLocalUserInfo);
        }
    }

    private class LocalHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_TYPE_ERROR_ENTER_ROOM:
                    String message = "";
                    int errorType = (int) msg.obj;
                    if (errorType == Constants.ERROR_KICK_BY_HOST) {
                        mIsExitRoom = true;
                        Toast.makeText(mContext, "被主播踢出", Toast.LENGTH_SHORT).show();
                        message = "被主播踢出";
                    } else if (errorType == Constants.ERROR_KICK_BY_PUSHRTMPFAILED) {
                        mIsExitRoom = true;
                        Toast.makeText(mContext, "rtmp推流失败", Toast.LENGTH_SHORT).show();
                        message = "rtmp推流失败";
                    } else if (errorType == Constants.ERROR_KICK_BY_SERVEROVERLOAD) {
                        mIsExitRoom = true;
                        Toast.makeText(mContext, "服务器过载", Toast.LENGTH_SHORT).show();
                        message = "服务器过载";
                    } else if (errorType == Constants.ERROR_KICK_BY_MASTER_EXIT) {
                        mIsExitRoom = true;
                        Toast.makeText(mContext, "主播已退出", Toast.LENGTH_SHORT).show();
                        message = "主播已退出";
                    } else if (errorType == Constants.ERROR_KICK_BY_RELOGIN) {
                        mIsExitRoom = true;
                        Toast.makeText(mContext, "重复登录", Toast.LENGTH_SHORT).show();
                        message = "重复登录";
                    } else if (errorType == Constants.ERROR_KICK_BY_NEWCHAIRENTER) {
                        mIsExitRoom = true;
                        Toast.makeText(mContext, "其他人以主播身份进入", Toast.LENGTH_SHORT).show();
                        message = "其他人以主播身份进入";
                    } else if (errorType == Constants.ERROR_KICK_BY_NOAUDIODATA) {
                        mIsExitRoom = true;
                        Toast.makeText(mContext, "长时间没有上行音频数据", Toast.LENGTH_SHORT).show();
                        message = "长时间没有上行音频数据";
                    } else if (errorType == Constants.ERROR_KICK_BY_NOVIDEODATA) {
                        mIsExitRoom = true;
                        Toast.makeText(mContext, "长时间没有上行视频数据", Toast.LENGTH_SHORT).show();
                        message = "长时间没有上行视频数据";
                    } else if (errorType == DISCONNECT) {
                        if (!mIsExitRoom) {
                            Toast.makeText(mContext, "网络连接断开，请检查网络", Toast.LENGTH_SHORT).show();
                            message = "网络连接断开，请检查网络";
                        }
                    }
                    if (mErrorExitDialog == null) {
                        mErrorExitDialog = new AlertDialog.Builder(mContext).setTitle("退出房间提示")//设置对话框标题
                                .setMessage("用户 " + LocalConfig.mLoginUserID + " 退出原因: " + message)//设置显示的内容
                                .setCancelable(false)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                                        exitRoom();
                                    }
                                }).show();//在按键响应事件中显示此对话框
                    }
                    break;
                case CONTROL_VOICE_IMAGE_VISIBILE:
                    ImageView objs = (ImageView) msg.obj;
                    objs.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    }

    private void addListData(EnterUserInfo info) {
        boolean bupdate = false;
        for (int i = 0; i < mPersons.size(); i++) {
            EnterUserInfo info1 = mPersons.get(i);
            if (info1.getId() == info.getId()) {
                mPersons.set(i, info);
                bupdate = true;
                break;
            }
        }
        if (!bupdate) {
            mPersons.add(info);
        }
    }

    private EnterUserInfo removeListData(long uid) {
        int index = -1;
        for (int i = 0; i < mPersons.size(); i++) {
            EnterUserInfo info1 = mPersons.get(i);
            if (info1.getId() == uid) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            return mPersons.remove(index);
        }
        return null;
    }

    private void openUserVideo() {
        for (int i = 0; i < mPersons.size(); i++) {
            EnterUserInfo enterUserInfo = mPersons.get(i);
            MyLog.d("openUserVideo User ID : " + enterUserInfo.getId()
                    + " | video open : " + enterUserInfo.isVideoOpen() + " | mPersons size : " + mPersons.size());
            if (enterUserInfo.getRole() == Constants.CLIENT_ROLE_BROADCASTER &&
                    !enterUserInfo.isVideoOpen()) {
                MyLog.d("openUserVideo User ID : " + enterUserInfo.getId() + " | mShowingDevices size : " + mShowingDevices.size());
                openUserVideo(enterUserInfo);
                break;
            }
        }
    }

    private void openUserVideo(EnterUserInfo enterUserInfo) {
        long userID = enterUserInfo.getId();
        int mOrder = -1;
        if (!mIsFirstUsing) {
            mOrder = MultiplayerVideoLayout.FIRST_VIDEO_LAYOUT;
            mIsFirstUsing = true;
        } else if (!mIsSecondUsing) {
            mOrder = MultiplayerVideoLayout.SECOND_VIDEO_LAYOUT;
            mIsSecondUsing = true;
        } else if (!mIsThirdUsing) {
            mOrder = MultiplayerVideoLayout.THIRD_VIDEO_LAYOUT;
            mIsThirdUsing = true;
        }

        if (mOrder != -1) {
            MultiplayerVideoLayout mLayout = new MultiplayerVideoLayout(mContext, mOrder, mActivityVideoBG);
            mLayout.setActivityCallBack(mFrameLayout -> {
                if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_VIDEO) {
                    SurfaceView mSurfaceView = mTTTEngine.CreateRendererView(mContext);
                    int result;
                    if (userID == LocalConfig.mLoginUserID) {
                        result = mTTTEngine.setupLocalVideo(new VideoCanvas(LocalConfig.mLoginUserID, Constants.
                                RENDER_MODE_HIDDEN, mSurfaceView), getRequestedOrientation());
                    } else {
                        result = mTTTEngine.setupRemoteVideo(new VideoCanvas(userID, Constants.
                                RENDER_MODE_HIDDEN, mSurfaceView));
                    }
                    MyLog.d("openUserVideo result : " + result);
                    mSurfaceView.setZOrderOnTop(true);
                    RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(-1, -1);
                    mFrameLayout.addView(mSurfaceView, params1);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    //添加音量图标
                    ImageView mAudioView = new ImageView(mContext);
                    mAudioView.setImageResource(R.drawable.yuyinxiao);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    mFrameLayout.addAudioView(mAudioView, params);
                }
            });
            mTTTEngine.muteRemoteAudioStream(enterUserInfo.getId(), false);
            enterUserInfo.setVideoOpen(true);

            mActivityVideoBG.addView(mLayout);
            mShowingDevices.put(enterUserInfo, mLayout);
            mExcludeView.add(mLayout);
        }
    }

    /**
     * Author: wangzg <br/>
     * Time: 2017-6-6 16:44:36<br/>
     * Description: 调整远端小窗口的显示与隐藏
     *
     * @param isVisibile the is visibile
     * @param info       the info
     */
    private synchronized void adJustRemoteViewDisplay(boolean isVisibile, EnterUserInfo info) {
        long userID = info.getId();
        MyLog.d("zhxopenUserVideo adJustRemoteViewDisplay " +
                "User ID : " + info.getId() + " | mShowingDevices size : " + mShowingDevices.size()
                + " | isVisibile : " + isVisibile
                + " | mPersons size : " + mPersons.size());
        if (isVisibile) {
            for (int i = 0; i < mPersons.size(); i++) {
                EnterUserInfo enterUserInfo = mPersons.get(i);
                if (enterUserInfo.getRole() == Constants.CLIENT_ROLE_BROADCASTER &&
                        !enterUserInfo.isVideoOpen()) {
                    openUserVideo(enterUserInfo);
                    break;
                }
            }
        } else {
            for (Map.Entry<EnterUserInfo, MultiplayerVideoLayout> next : mShowingDevices.entrySet()) {
                EnterUserInfo removeUser = next.getKey();
                if (removeUser.getId() == userID) {
                    MultiplayerVideoLayout value = next.getValue();
                    int order = value.getOrder();
                    if (order == MultiplayerVideoLayout.FIRST_VIDEO_LAYOUT) {
                        mIsFirstUsing = false;
                    } else if (order == MultiplayerVideoLayout.SECOND_VIDEO_LAYOUT) {
                        mIsSecondUsing = false;
                    } else if (order == MultiplayerVideoLayout.THIRD_VIDEO_LAYOUT) {
                        mIsThirdUsing = false;
                    }
                    mActivityVideoBG.removeView(value);
                    mExcludeView.remove(value);
                    mShowingDevices.remove(removeUser);
                    break;
                }
            }
            openUserVideo();
        }
    }

    @Override
    protected void receiveCallBack(JniObjs mJniObjs) {
        switch (mJniObjs.mJniType) {
            case CALL_BACK_ON_ERROR:
                Message.obtain(mHandler, MSG_TYPE_ERROR_ENTER_ROOM, mJniObjs.mErrorType).sendToTarget();
                break;
            case CALL_BACK_ON_USER_JOIN:
                if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_CHAT) return;

                long uid = mJniObjs.mUid;
                int identity = mJniObjs.mIdentity;
                MyLog.d("openUserVideotest CALL_BACK_ON_USER_JOIN user id : " + uid);
                EnterUserInfo userInfo = new EnterUserInfo(uid, identity);
                addListData(userInfo);
                if (identity == Constants.CLIENT_ROLE_AUDIENCE) {
                    addAudienceNums();
                } else {
                    if (mPlayers.size() >= MAX_VIDEO_NUM) {
                        MyLog.d("openUserVideo 超出玩家数量，静音 : " + uid);
                        mTTTEngine.muteRemoteAudioStream(uid, true);
                    } else {
                        adJustRemoteViewDisplay(true, userInfo);
                    }
                    mPlayers.add(userInfo.getId());
                }
                break;
            case CALL_BACK_ON_USER_OFFLINE:
                if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_CHAT) return;

                long offLineUserID = mJniObjs.mUid;
                MyLog.d("openUserVideotest CALL_BACK_ON_USER_OFFLINE user id : " + offLineUserID);
                EnterUserInfo enterUserInfo = removeListData(offLineUserID);
                if (enterUserInfo != null) {
                    if (enterUserInfo.getRole() == Constants.CLIENT_ROLE_AUDIENCE) {
                        reduceAudienceNums();
                    } else {
                        mPlayers.remove(enterUserInfo.getId());
                        adJustRemoteViewDisplay(false, enterUserInfo);
                    }
                } else {
                    MyLog.d("CALL_BACK_ON_USER_OFFLINE 没有从用户列表中找到该用户 id : " + offLineUserID);
                }
                break;
            case LocalConstans.CALL_BACK_ON_PHONE_LISTENER_COME:
                mTTTEngine.muteLocalAudioStream(true);
                mTTTEngine.muteLocalVideoStream(true);
                mTTTEngine.muteAllRemoteAudioStreams(true);
                mIsPhoneComing = true;
                break;
            case LocalConstans.CALL_BACK_ON_PHONE_LISTENER_IDLE:
                if (mIsPhoneComing) {
                    mTTTEngine.muteLocalAudioStream(false);
                    mTTTEngine.muteLocalVideoStream(false);
                    mTTTEngine.muteAllRemoteAudioStreams(false);
                    if (mTTTEngine.isSpeakerphoneEnabled()) {
                        mTTTEngine.setEnableSpeakerphone(true);
                    }
                    mIsPhoneComing = false;
                }
                break;
            case CALL_BACK_ON_AUDIO_VOLUME_INDICATION:
                long volumeUserID = mJniObjs.mUid;
                int volumeLevel = mJniObjs.mAudioLevel;
                for (Map.Entry<EnterUserInfo, MultiplayerVideoLayout> next : mShowingDevices.entrySet()) {
                    Long key = next.getKey().getId();
                    if (volumeUserID == key) {
                        MultiplayerVideoLayout layout = next.getValue();
                        if (layout != null) {
                            ImageView audioView = layout.getAudioView();
                            if (audioView != null) {
                                if (volumeLevel > 0 && volumeLevel <= 3) {
                                    if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_AUDIO) {
                                        audioView.setImageResource(R.drawable.multiplayer_audio_small);
                                        adJustImageSpeak(audioView);
                                    } else {
                                        audioView.setImageResource(R.drawable.yuyinxiao);
                                    }
                                } else if (volumeLevel > 3 && volumeLevel <= 6) {
                                    if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_AUDIO) {
                                        audioView.setImageResource(R.drawable.multiplayer_audio_middle);
                                        adJustImageSpeak(audioView);
                                    } else {
                                        audioView.setImageResource(R.drawable.yuyinzhong);
                                    }
                                } else if (volumeLevel > 6 && volumeLevel <= VOLUME_MAX_NUM) {
                                    if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_AUDIO) {
                                        adJustImageSpeak(audioView);
                                        audioView.setImageResource(R.drawable.multiplayer_audio_big);
                                    } else {
                                        audioView.setImageResource(R.drawable.yuyinda);
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case CALL_BACK_ON_FIRST_VIDEO_FRAME:
                long videoFrameUid = mJniObjs.mUid;
                MyLog.w("CALL_BACK_ON_FIRST_VIDEO_FRAME : " + videoFrameUid);
                break;
            case CALL_BACK_ON_CHAT_MESSAGE_SENT:
                int reciveType = mJniObjs.type;
                String reciveData = mJniObjs.strData;
                int time = mJniObjs.audioTime;
                mChatListFragment.addMessage(new MessageBean((int)LocalConfig.mLoginUserID, reciveType, reciveData, time));
                break;
            case CALL_BACK_ON_CHAT_MESSAGE_RECIVED:
                long nSrcUserID = mJniObjs.nSrcUserID;
                int type = mJniObjs.type;
                String strData = mJniObjs.strData;
                int audioTime = mJniObjs.audioTime;

                    mChatListFragment.addMessage(new MessageBean((int) nSrcUserID, type, strData, audioTime));
                break;
            case CALL_BACK_ON_AUDIO_PLAY_COMPLATION:
                break;
            case CALL_BACK_ON_AUDIO_RECOGNIZED:
                break;
        }
    }

    private void adJustImageSpeak(ImageView audioView) {
        mHandler.removeMessages(CONTROL_VOICE_IMAGE_VISIBILE, audioView);
        audioView.setVisibility(View.VISIBLE);
        Message obtain = Message.obtain();
        obtain.what = CONTROL_VOICE_IMAGE_VISIBILE;
        obtain.obj = audioView;
        mHandler.sendMessageDelayed(obtain, 1500);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_CHAT) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_RAISE,
                            AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_LOWER,
                            AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
