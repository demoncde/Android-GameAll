package com.tttrtcgame.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tttrtcgame.LocalConfig;
import com.tttrtcgame.LocalConstans;
import com.tttrtcgame.R;
import com.tttrtcgame.adapter.RecyclerViewAdapter;
import com.tttrtcgame.bean.EnterUserInfo;
import com.tttrtcgame.bean.JniObjs;
import com.tttrtcgame.bean.MessageBean;
import com.tttrtcgame.bean.VideoViewObj;
import com.tttrtcgame.callback.PhoneListener;
import com.tttrtcgame.dialog.ChatDialog;
import com.tttrtcgame.helper.MyVideoLayout;
import com.tttrtcgame.helper.SpaceItemDecoration;
import com.tttrtcgame.utils.MyLog;
import com.wushuangtech.library.Constants;
import com.wushuangtech.wstechapi.TTTRtcEngineForGamming;
import com.wushuangtech.wstechapi.model.VideoCanvas;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_AUDIO_PLAY_COMPLATION;
import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_AUDIO_RECOGNIZED;
import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_AUDIO_VOLUME_INDICATION;
import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_CHAT_MESSAGE_RECIVED;
import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_CHAT_MESSAGE_SENT;
import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_ERROR;
import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_FIRST_VIDEO_DECODER;
import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_USER_JOIN;
import static com.tttrtcgame.LocalConstans.CALL_BACK_ON_USER_OFFLINE;


public class MainActivity extends BaseActivity {

    private static final int AUTHOR_MAX_NUM = 12;
    private static final int VOLUME_MAX_NUM = 9;

    public static final int MSG_TYPE_ERROR_ENTER_ROOM = 0;
    public static final int DISCONNECT = 100;
    private static final int CONTROL_VOICE_SPEAK = 13;
    private static final int CONTROL_VOICE_IMAGE_VISIBILE = 14;

    private TTTRtcEngineForGamming mTTTEngine;

    private ArrayList<VideoViewObj> mLocalSeiList;
    private List<EnterUserInfo> listData;
    private Vector<Long> mPlayers;

    private Handler mHandler;

    protected TextView mSpeakingTV;
    protected MyVideoLayout mVideoParentLy;

    private VideoViewObj mLocalUserViewObj;
    private boolean mIsExitRoom;
    private boolean mIsSpeaking;
    private boolean mIsPhoneComing;
    private boolean mIsOwnerOpend;
    private AlertDialog mErrorExitDialog;
    private RecyclerView mMainChatView;
    private RecyclerViewAdapter mAdapter;
    private Button mMainPressSpeak;

    private AudioManager audioManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainly_activity);
        mTTTEngine = TTTRtcEngineForGamming.getInstance();
        initView();
        initData();
        EnterUserInfo mLocalUserInfo = new EnterUserInfo(LocalConfig.mLoginUserID, LocalConfig.mLoginRole);

        // 设置布局管理器
        mMainChatView.setLayoutManager(new LinearLayoutManager(this));
        // 设置adapter
        mMainChatView.setAdapter(mAdapter = new RecyclerViewAdapter());
        mAdapter.setOnItemClickListener(audioPath -> mTTTEngine.playChatAudio(audioPath));
        mAdapter.setOnItemLongClickListener(audioPath -> mTTTEngine.speechRecognition(audioPath));
        // 设置Item添加和移除的动画
        mMainChatView.setItemAnimator(new DefaultItemAnimator());
        mMainChatView.addItemDecoration(new SpaceItemDecoration(20));
        mAdapter.setLayoutType(1);
        if (LocalConfig.mLoginRole != Constants.CLIENT_ROLE_AUDIENCE) {
            mPlayers.add(mLocalUserInfo.getId());
            adJustRemoteViewDisplay(true, mLocalUserInfo);
            if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_CHAT) {
                mSpeakingTV.setVisibility(View.GONE);
            }
        } else {
            mSpeakingTV.setVisibility(View.GONE);
        }

        if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_CHAT) {
            findViewById(R.id.id_social_chat).setVisibility(View.VISIBLE);
            if (LocalConfig.mLoginRole != Constants.CLIENT_ROLE_AUDIENCE) {
                findViewById(R.id.chat_controler).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.chat_controler).setVisibility(View.GONE);
            }
        }

        TelephonyManager tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
        if (tm != null) {
            tm.listen(new PhoneListener(this), PhoneStateListener.LISTEN_CALL_STATE);
        }
        mTTTEngine.enableAudioVolumeIndication(300, 3);

        audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        MyLog.w("MainActivity onCreate... : " + LocalConfig.mLoginRoomType);
    }

    @Override
    protected void onDestroy() {
        for (int i = 0; i < mLocalSeiList.size(); i++) {
            VideoViewObj videoViewObj = mLocalSeiList.get(i);
            videoViewObj.clear();
        }
        mHandler.removeCallbacksAndMessages(null);
        mVideoParentLy.clear();
        mVideoParentLy = null;
        MyLog.d("MainActivity onDestroy... ");
        super.onDestroy();
    }

    private void initView() {
        mMainChatView = findViewById(R.id.social_chat_view);
        mSpeakingTV = findViewById(R.id.mainly_speaking);
        mSpeakingTV.setClickable(true);
        mVideoParentLy = findViewById(R.id.mainly_user_videoly);
        mVideoParentLy.setActivityCallBack(() -> {
            if (!mIsOwnerOpend) {
                openLocalVideo();
            }
            openRemoteUserVideo();
        });

        mMainPressSpeak = findViewById(R.id.main_press_speak);
        mMainPressSpeak.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    //按住事件发生后执行代码的区域
                    mTTTEngine.startRecordChatAudio();
                    mMainPressSpeak.setText("松开结束");
                    break;

                case MotionEvent.ACTION_UP:
                    //松开事件发生后执行代码的区域
                    mMainPressSpeak.setText("按住说话");
                    String seqID = UUID.randomUUID().toString() + System.currentTimeMillis();
                    mTTTEngine.stopRecordAndSendChatAudio(0, seqID);
                    break;


                default:

                    break;
            }
            return false;
        });

        findViewById(R.id.main_bg).setOnTouchListener((view, motionEvent) -> {
            judgeClick(mExcludeView, motionEvent);
            return false;
        });

        findViewById(R.id.change_voice).setOnClickListener(view -> {
            ChatDialog chatDialog = new ChatDialog(this, ChatDialog.VERTICAL);
            chatDialog.setOnSendMessageListener(message -> {
                mTTTEngine.sendChatMessage(0, 1, "0", message);
            });
            chatDialog.show();
        });

        mLocalSeiList = new ArrayList<>();
        for (int i = 1; i <= AUTHOR_MAX_NUM; i++) {
            VideoViewObj obj = new VideoViewObj();
            obj.mIndex = i;
            switch (i) {
                case 1:
                    obj.mRootBG = findViewById(R.id.mainly_user_left1);
                    break;
                case 2:
                    obj.mRootBG = findViewById(R.id.mainly_user_left2);
                    break;
                case 3:
                    obj.mRootBG = findViewById(R.id.mainly_user_left3);
                    break;
                case 4:
                    obj.mRootBG = findViewById(R.id.mainly_user_left4);
                    break;
                case 5:
                    obj.mRootBG = findViewById(R.id.mainly_user_left5);
                    break;
                case 6:
                    obj.mRootBG = findViewById(R.id.mainly_user_left6);
                    break;
                case 7:
                    obj.mRootBG = findViewById(R.id.mainly_user_right1);
                    break;
                case 8:
                    obj.mRootBG = findViewById(R.id.mainly_user_right2);
                    break;
                case 9:
                    obj.mRootBG = findViewById(R.id.mainly_user_right3);
                    break;
                case 10:
                    obj.mRootBG = findViewById(R.id.mainly_user_right4);
                    break;
                case 11:
                    obj.mRootBG = findViewById(R.id.mainly_user_right5);
                    break;
                case 12:
                    obj.mRootBG = findViewById(R.id.mainly_user_right6);
                    break;
            }

            obj.mSpeakImage = obj.mRootBG.findViewById(R.id.userly_audio_icon);
            obj.mRemoteUserIcon = obj.mRootBG.findViewById(R.id.userly_icon);
            obj.mRemoteUserIndex = obj.mRootBG.findViewById(R.id.userly_count);
            obj.mRemoteUserID = obj.mRootBG.findViewById(R.id.userly_user_id);
            obj.mLocalNameFlag = obj.mRootBG.findViewById(R.id.userly_local_user_name);
            mLocalSeiList.add(obj);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initData() {
        listData = new ArrayList<>();
        mPlayers = new Vector<>();
        mExcludeView.add(mSpeakingTV);
        if (mHandler == null) {
            mHandler = new LocalHandler();
        }

        mSpeakingTV.setOnClickListener(v -> {
            if (mIsSpeaking) {
                Message.obtain(mHandler, CONTROL_VOICE_SPEAK, false).sendToTarget();
            } else {
                Message.obtain(mHandler, CONTROL_VOICE_SPEAK, true).sendToTarget();
            }
        });
    }

    private void addListData(EnterUserInfo info) {
        boolean bupdate = false;
        for (int i = 0; i < listData.size(); i++) {
            EnterUserInfo info1 = listData.get(i);
            if (info1.getId() == info.getId()) {
                listData.set(i, info);
                bupdate = true;
                break;
            }
        }
        if (!bupdate) {
            listData.add(info);
        }
    }

    private EnterUserInfo removeListData(long uid) {
        int index = -1;
        for (int i = 0; i < listData.size(); i++) {
            EnterUserInfo info1 = listData.get(i);
            if (info1.getId() == uid) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            return listData.remove(index);
        }
        return null;
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
        MyLog.d("adJustRemoteViewDisplay isVisibile : " + isVisibile +
                " | User ID : " + info.getId());
        if (isVisibile) {
            boolean checkRes = checkVideoExist(info.getId());
            if (checkRes) {
                MyLog.d("该用户已经存在... ");
                return;
            }

            long id = info.getId();
            VideoViewObj obj = getRemoteViewParentLayout();
            if (obj != null) {
                obj.mBindUid = id;
                obj.mRemoteUserIndex.setText(String.valueOf(obj.mIndex));
                obj.mRemoteUserIndex.setVisibility(View.VISIBLE);
                obj.mRemoteUserID.setText(String.valueOf(id));
                setUserIcon(obj.mRemoteUserIcon, obj.mBindUid);
                if (id == LocalConfig.mLoginUserID) {
                    mLocalUserViewObj = obj;
                    obj.mLocalNameFlag.setVisibility(View.VISIBLE);
                    openLocalVideo();
                }
            } else {
                MyLog.d("getRemoteViewParentLayout 失败... ");
            }
        } else {
            VideoViewObj videoCusSei;
            for (int i = 0; i < mLocalSeiList.size(); i++) {
                videoCusSei = mLocalSeiList.get(i);
                if (videoCusSei.mBindUid == info.getId()) {
                    videoCusSei.mIsUsing = false;
                    videoCusSei.mVideoOpend = false;
                    videoCusSei.mBindUid = 0;
                    videoCusSei.mRemoteUserIndex.setText("");
                    videoCusSei.mRemoteUserID.setText("");
                    videoCusSei.mLocalNameFlag.setVisibility(View.GONE);
                    videoCusSei.mRemoteUserIcon.setImageResource(R.drawable.touxiangmoren);
                    videoCusSei.mSpeakImage.setVisibility(View.INVISIBLE);
                    videoCusSei.mRemoteUserIndex.setVisibility(View.INVISIBLE);
                    mVideoParentLy.removeVideoChild(info.getId());
                    MyLog.d("openUserVideo 有用户离开，开始打开下一路视频");
                    openUserVideo();
                    break;
                }
            }
        }
    }

    private synchronized void openLocalVideo() {
        if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_AUDIO
                || LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_CHAT
                || LocalConfig.mLoginRole == Constants.CLIENT_ROLE_AUDIENCE) {
            return;
        }
        FrameLayout videoChildLayout = mVideoParentLy.getVideoChildLayout(LocalConfig.mLoginUserID);
        if (videoChildLayout != null) {
            SurfaceView mSurfaceView = mTTTEngine.CreateRendererView(mContext);
            mTTTEngine.setupLocalVideo(new VideoCanvas(LocalConfig.mLoginUserID, Constants.
                    RENDER_MODE_HIDDEN, mSurfaceView), getRequestedOrientation());
            mSurfaceView.setZOrderOnTop(true);
            mSurfaceView.setZOrderMediaOverlay(true);
            videoChildLayout.addView(mSurfaceView);
            mIsOwnerOpend = true;
        }
    }

    private synchronized boolean openUserVideo() {
        if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_AUDIO
                || LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_CHAT) {
            return false;
        }

        if (mVideoParentLy.mShowingDevices.size() < MyVideoLayout.MAX_VIDEO_NUM) {
            for (int i = 0; i < mLocalSeiList.size(); i++) {
                VideoViewObj videoViewObj = mLocalSeiList.get(i);
                if (videoViewObj.mBindUid != LocalConfig.mLoginUserID && videoViewObj.mBindUid != 0) {
                    boolean mIsDecoder = false;
                    for (int j = 0; j < listData.size(); j++) {
                        EnterUserInfo enterUserInfo = listData.get(j);
                        if (enterUserInfo.getId() == videoViewObj.mBindUid) {
                            mIsDecoder = enterUserInfo.isVideoDecoded();
                            break;
                        }
                    }

                    if (!mIsDecoder) {
                        MyLog.d("openUserVideo 该用户的视频还没到来!" + videoViewObj.mBindUid);
                        continue;
                    }

                    if (videoViewObj.mIsUsing) {
                        if (!videoViewObj.mVideoOpend) {
                            FrameLayout videoChildLayout = mVideoParentLy.getVideoChildLayout(videoViewObj.mBindUid);
                            if (videoChildLayout != null) {
                                SurfaceView mSurfaceView = mTTTEngine.CreateRendererView(mContext);
                                int result = mTTTEngine.setupRemoteVideo(new VideoCanvas(videoViewObj.mBindUid, Constants.
                                        RENDER_MODE_HIDDEN, mSurfaceView));
                                videoChildLayout.addView(mSurfaceView);
                                videoViewObj.mVideoOpend = true;
                                MyLog.d("openUserVideo open remote user id : " + videoViewObj.mBindUid
                                        + " | result : " + result + " | layout index : " + videoChildLayout.getId());
                                return true;
                            } else {
                                MyLog.d("openUserVideo 该用户的视频已经被打开!" + videoViewObj.mBindUid);
                            }
                        } else {
                            MyLog.d("openUserVideo 该videoViewObj并没有人使用!");
                        }
                    } else {
                        MyLog.d("openUserVideo videoViewObj的uid为0!");
                    }
                } else {
                    MyLog.d("openUserVideo open remote user failed! id : " + videoViewObj.mBindUid);
                }
            }
        } else {
            MyLog.d("openUserVideo 当前视频已经显示够满4路，不能再打开视频了!");
        }
        return false;
    }

    private void openRemoteUserVideo() {
        boolean b = openUserVideo();
        if (b) {
            openRemoteUserVideo();
        }
    }

    /**
     * Author: wangzg <br/>
     * Time: 2017-6-6 16:45:00<br/>
     * Description: 创建一个新的远端小视频的布局窗口
     *
     * @return the list
     */
    private VideoViewObj getRemoteViewParentLayout() {
        for (int i = 0; i < mLocalSeiList.size(); i++) {
            VideoViewObj videoCusSei = mLocalSeiList.get(i);
            if (!videoCusSei.mIsUsing) {
                videoCusSei.mIsUsing = true;
                return videoCusSei;
            }
        }
        return null;
    }

    private void setUserIcon(ImageView mView, long userID) {
        char[] chars = String.valueOf(userID).toCharArray();
        String end = String.valueOf(chars[chars.length - 1]);
        switch (Integer.valueOf(end)) {
            case 0:
                mView.setImageResource(R.drawable.touxiang1);
                break;
            case 1:
                mView.setImageResource(R.drawable.touxiang2);
                break;
            case 2:
                mView.setImageResource(R.drawable.touxiang3);
                break;
            case 3:
                mView.setImageResource(R.drawable.touxiang4);
                break;
            case 4:
                mView.setImageResource(R.drawable.touxiang5);
                break;
            case 5:
                mView.setImageResource(R.drawable.touxiang6);
                break;
            case 6:
                mView.setImageResource(R.drawable.touxiang7);
                break;
            case 7:
                mView.setImageResource(R.drawable.touxiang8);
                break;
            case 8:
                mView.setImageResource(R.drawable.touxiang9);
                break;
            case 9:
                mView.setImageResource(R.drawable.touxiang10);
                break;
        }
    }

    private boolean checkVideoExist(long uid) {
        for (int i = 0; i < mLocalSeiList.size(); i++) {
            VideoViewObj videoCusSei = mLocalSeiList.get(i);
            if (videoCusSei.mIsUsing && videoCusSei.mBindUid == uid) {
                return true;
            }
        }
        return false;
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
                        mErrorExitDialog = new AlertDialog.Builder(MainActivity.this).setTitle("退出房间提示")//设置对话框标题
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
                case CONTROL_VOICE_SPEAK:
                    boolean isSpeak = (boolean) msg.obj;
                    mTTTEngine.muteLocalAudioStream(!isSpeak);
                    if (isSpeak) {
                        mSpeakingTV.setText("结束发言");
                        mSpeakingTV.setBackgroundResource(R.drawable.mainly_btn_speaking_bg_press);
                        mIsSpeaking = true;
                    } else {
                        if (mLocalUserViewObj != null && mLocalUserViewObj.mSpeakImage != null) {
                            mLocalUserViewObj.mSpeakImage.setVisibility(View.INVISIBLE);
                        }

                        mSpeakingTV.setText("开始发言");
                        mSpeakingTV.setBackgroundResource(R.drawable.mainly_btn_speaking_bg);
                        mIsSpeaking = false;
                    }
                    break;
                case CONTROL_VOICE_IMAGE_VISIBILE:
                    VideoViewObj objs = (VideoViewObj) msg.obj;
                    objs.mSpeakImage.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    }

    @Override
    protected void receiveCallBack(JniObjs mJniObjs) {
        switch (mJniObjs.mJniType) {
            case CALL_BACK_ON_ERROR:
                Message.obtain(mHandler, MSG_TYPE_ERROR_ENTER_ROOM, mJniObjs.mErrorType).sendToTarget();
                break;
            case CALL_BACK_ON_USER_JOIN:
                long uid = mJniObjs.mUid;
                int identity = mJniObjs.mIdentity;
                if (mPlayers.size() >= AUTHOR_MAX_NUM) {
                    mTTTEngine.muteRemoteAudioStream(uid, true);
                    mTTTEngine.muteRemoteVideoStream(uid, true);
                    return;
                }

                MyLog.d("openUserVideo CALL_BACK_ON_USER_JOIN user id : " + uid);
                EnterUserInfo userInfo = new EnterUserInfo(uid, identity);
                addListData(userInfo);
                if (identity == Constants.CLIENT_ROLE_AUDIENCE) {
                    addAudienceNums();
                } else {
                    mPlayers.add(userInfo.getId());
                    adJustRemoteViewDisplay(true, userInfo);
                }
                break;
            case CALL_BACK_ON_USER_OFFLINE:
                long offLineUserID = mJniObjs.mUid;
                MyLog.d("openUserVideo CALL_BACK_ON_USER_OFFLINE user id : " + offLineUserID);
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
            case CALL_BACK_ON_AUDIO_VOLUME_INDICATION:
                long volumeUserID = mJniObjs.mUid;
                int volumeLevel = mJniObjs.mAudioLevel;
                for (final VideoViewObj obj : mLocalSeiList) {
                    if (obj.mBindUid == volumeUserID && obj.mIsUsing && obj.mSpeakImage != null) {
                        boolean isEnd = false;
                        if (volumeUserID == LocalConfig.mLoginUserID && !mIsSpeaking) {
                            isEnd = true;
                        }

                        if (isEnd) {
                            break;
                        }

                        if (volumeLevel > 0 && volumeLevel <= 3) {
                            obj.mSpeakImage.setImageResource(R.drawable.xiao);
                            adJustImageSpeak(obj);
                        } else if (volumeLevel > 3 && volumeLevel <= 6) {
                            obj.mSpeakImage.setImageResource(R.drawable.zhong);
                            adJustImageSpeak(obj);
                        } else if (volumeLevel > 6 && volumeLevel <= VOLUME_MAX_NUM) {
                            obj.mSpeakImage.setImageResource(R.drawable.da);
                            adJustImageSpeak(obj);
                        }
                        break;
                    }
                }
                break;
            case LocalConstans.CALL_BACK_ON_PHONE_LISTENER_COME:
                if (mIsSpeaking) {
                    mTTTEngine.muteLocalAudioStream(true);
                    mTTTEngine.muteLocalVideoStream(true);
                }
                mTTTEngine.muteAllRemoteAudioStreams(true);
                mIsPhoneComing = true;
                break;
            case LocalConstans.CALL_BACK_ON_PHONE_LISTENER_IDLE:
                if (mIsPhoneComing) {
                    if (mIsSpeaking) {
                        mTTTEngine.muteLocalAudioStream(false);
                        mTTTEngine.muteLocalVideoStream(false);
                    }
                    mTTTEngine.muteAllRemoteAudioStreams(false);
                    if (mTTTEngine.isSpeakerphoneEnabled()) {
                        mTTTEngine.setEnableSpeakerphone(true);
                    }
                    mIsPhoneComing = false;
                }
                break;
            case CALL_BACK_ON_FIRST_VIDEO_DECODER:
                long decoderUserID = mJniObjs.mUid;
                MyLog.d("openUserVideo CALL_BACK_ON_FIRST_VIDEO_DECODER user id : " + decoderUserID);
                for (int i = 0; i < listData.size(); i++) {
                    EnterUserInfo info = listData.get(i);
                    if (info.getId() == decoderUserID) {
                        info.setVideoDecoded(true);
                        openUserVideo();
                        break;
                    }
                }
                break;
            case CALL_BACK_ON_CHAT_MESSAGE_SENT:
                int reciveType = mJniObjs.type;
                String reciveData = mJniObjs.strData;
                int time = mJniObjs.audioTime;
                mAdapter.add(new MessageBean((int)LocalConfig.mLoginUserID, reciveType, reciveData, time));
                mMainChatView.smoothScrollToPosition(mAdapter.getItemCount());
                break;
            case CALL_BACK_ON_CHAT_MESSAGE_RECIVED:
                long nSrcUserID = mJniObjs.nSrcUserID;
                int type = mJniObjs.type;
                String strData = mJniObjs.strData;
                int audioTime = mJniObjs.audioTime;
                mAdapter.add(new MessageBean((int) nSrcUserID, type, strData, audioTime));
                mMainChatView.smoothScrollToPosition(mAdapter.getItemCount());
                break;
            case CALL_BACK_ON_AUDIO_PLAY_COMPLATION:
                break;
            case CALL_BACK_ON_AUDIO_RECOGNIZED:
                String recognized = mJniObjs.strData;
                Log.d("zhx", "recognized: " + recognized);
                mAdapter.updateItem(new MessageBean(recognized));
                break;
        }
    }

    private void adJustImageSpeak(VideoViewObj obj) {
        mHandler.removeMessages(CONTROL_VOICE_IMAGE_VISIBILE, obj);
        obj.mSpeakImage.setVisibility(View.VISIBLE);
        Message obtain = Message.obtain();
        obtain.what = CONTROL_VOICE_IMAGE_VISIBILE;
        obtain.obj = obj;
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
