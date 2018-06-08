package com.tttrtcgame.helper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tttrtcgame.LocalConfig;
import com.tttrtcgame.LocalConstans;
import com.tttrtcgame.R;

public class MultiplayerVideoLayout extends RelativeLayout implements ViewTreeObserver.OnGlobalLayoutListener,
        View.OnTouchListener {

    public static final int FIRST_VIDEO_LAYOUT = 10;
    public static final int SECOND_VIDEO_LAYOUT = 11;
    public static final int THIRD_VIDEO_LAYOUT = 12;

    private boolean mIsCreated;
    private ActivityCallBack mActivityCallBack;
    private int mOrder;
    private ViewGroup mRootView;

    private ImageView mAudioView;
    private int lastX;
    private int lastY;

    public MultiplayerVideoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiplayerVideoLayout(Context context, int order, ViewGroup mRootView) {
        super(context);
        this.mOrder = order;
        this.mRootView = mRootView;
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(this);
        if (LocalConfig.mLoginRoomGameType != LocalConstans.ROOM_GAME_TYPE_CHESS) {
            setOnTouchListener(this);
        }

        if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_VIDEO) {
            setBackgroundColor(Color.WHITE);
        } else if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_AUDIO){
            setBackgroundResource(R.drawable.multiplayer_audio_icon);
        }
    }

    @Override
    public void onGlobalLayout() {
        if (!mIsCreated) {
            mIsCreated = true;
            RelativeLayout.LayoutParams layoutParams = (LayoutParams) getLayoutParams();
            if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_VIDEO) {
                int VIDEO_LAYOUT_WIDTH = (int) getResources().getDimension(R.dimen.videolayout_size_width);
                int VIDEO_LAYOUT_HEIGHT = (int) getResources().getDimension(R.dimen.videolayout_size_height);
                int VIDEO_LAYOUT_PADDING = (int) getResources().getDimension(R.dimen.videolayout_padding);
                layoutParams.width = VIDEO_LAYOUT_WIDTH;
                layoutParams.height = VIDEO_LAYOUT_HEIGHT;
                if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_CHESS) {
                    int VIDEO_CHESS_LAYOUT_40;
                    int VIDEO_CHESS_LAYOUT_90;
                    int VIDEO_CHESS_LAYOUT_180;
                    if (isTabletDevice(getContext())) {
                        VIDEO_CHESS_LAYOUT_40 = (int) getResources().getDimension(R.dimen.videolayout_chess_video_margin_40_pad);
                        VIDEO_CHESS_LAYOUT_90 = (int) getResources().getDimension(R.dimen.videolayout_chess_video_margin_90_pad);
                        VIDEO_CHESS_LAYOUT_180 = (int) getResources().getDimension(R.dimen.videolayout_chess_video_margin_180_pad);
                    } else {
                        VIDEO_CHESS_LAYOUT_40 = (int) getResources().getDimension(R.dimen.videolayout_chess_video_margin_40);
                        VIDEO_CHESS_LAYOUT_90 = (int) getResources().getDimension(R.dimen.videolayout_chess_video_margin_90);
                        VIDEO_CHESS_LAYOUT_180 = (int) getResources().getDimension(R.dimen.videolayout_chess_video_margin_180);
                    }
                    if (mOrder == FIRST_VIDEO_LAYOUT) {
                        layoutParams.leftMargin = VIDEO_CHESS_LAYOUT_40;
                        layoutParams.topMargin = VIDEO_CHESS_LAYOUT_90;
                    } else if (mOrder == SECOND_VIDEO_LAYOUT) {
                        layoutParams.addRule(ALIGN_PARENT_RIGHT);
                        layoutParams.rightMargin = VIDEO_CHESS_LAYOUT_40;
                        layoutParams.topMargin = VIDEO_CHESS_LAYOUT_90;
                    } else if (mOrder == THIRD_VIDEO_LAYOUT) {
                        layoutParams.leftMargin = VIDEO_CHESS_LAYOUT_40;
                        layoutParams.topMargin = VIDEO_CHESS_LAYOUT_90 + VIDEO_LAYOUT_HEIGHT + VIDEO_CHESS_LAYOUT_180;
                    }
                } else {
                    layoutParams.addRule(ALIGN_PARENT_RIGHT);
                    layoutParams.addRule(ALIGN_PARENT_BOTTOM);
                    int VIDEO_LAYOUT_MARGIN = (int) getResources().getDimension(R.dimen.videolayout_margin);
                    layoutParams.bottomMargin = VIDEO_LAYOUT_MARGIN;
                    if (mOrder == FIRST_VIDEO_LAYOUT) {
                        layoutParams.rightMargin = VIDEO_LAYOUT_MARGIN;
                    } else if (mOrder == SECOND_VIDEO_LAYOUT) {
                        layoutParams.rightMargin = VIDEO_LAYOUT_MARGIN
                                + VIDEO_LAYOUT_WIDTH + VIDEO_LAYOUT_MARGIN;
                    } else if (mOrder == THIRD_VIDEO_LAYOUT) {
                        layoutParams.rightMargin = VIDEO_LAYOUT_MARGIN
                                + VIDEO_LAYOUT_WIDTH + VIDEO_LAYOUT_MARGIN
                                + VIDEO_LAYOUT_WIDTH + VIDEO_LAYOUT_MARGIN;
                    }
                }
                setLayoutParams(layoutParams);
//                setPadding(VIDEO_LAYOUT_PADDING, VIDEO_LAYOUT_PADDING, VIDEO_LAYOUT_PADDING, VIDEO_LAYOUT_PADDING);
                if (mActivityCallBack != null) {
                    mActivityCallBack.prepareWorking(this);
                }
            } else if (LocalConfig.mLoginRoomType == LocalConstans.ROOM_TYPE_AUDIO) {
                int AUDIO_LAYOUT_PADDING = (int) getResources().getDimension(R.dimen.videolayout_audio_padding);
                layoutParams.width = LayoutParams.WRAP_CONTENT;
                layoutParams.height = LayoutParams.WRAP_CONTENT;
                if (LocalConfig.mLoginRoomGameType == LocalConstans.ROOM_GAME_TYPE_CHESS) {
                    int AUDIO_CHESS_MARGIN_TOP = (int) getResources().getDimension(R.dimen.videolayout_chess_video_margin_100);
                    int AUDIO_CHESS_LEFT_MARGIN = (int) getResources().getDimension(R.dimen.videolayout_chess_video_margin_130);
                    int AUDIO_CHESS_RIGHT_MARGIN = (int) getResources().getDimension(R.dimen.videolayout_chess_video_margin_220);
                    int AUDIO_CHESS_BOTTOM_MARGIN;
                    if (isTabletDevice(getContext())) {
                        AUDIO_CHESS_BOTTOM_MARGIN = (int) getResources().getDimension(R.dimen.videolayout_chess_video_margin_280_pad);
                    } else {
                        AUDIO_CHESS_BOTTOM_MARGIN = (int) getResources().getDimension(R.dimen.videolayout_chess_video_margin_280);
                    }
                    if (mOrder == FIRST_VIDEO_LAYOUT) {
                        layoutParams.topMargin = AUDIO_CHESS_MARGIN_TOP;
                        layoutParams.leftMargin = AUDIO_CHESS_RIGHT_MARGIN;
                    } else if (mOrder == SECOND_VIDEO_LAYOUT) {
                        layoutParams.addRule(ALIGN_PARENT_RIGHT);
                        layoutParams.topMargin = AUDIO_CHESS_MARGIN_TOP;
                        layoutParams.rightMargin = AUDIO_CHESS_RIGHT_MARGIN;
                    } else if (mOrder == THIRD_VIDEO_LAYOUT) {
                        layoutParams.addRule(ALIGN_PARENT_BOTTOM);
                        layoutParams.bottomMargin = AUDIO_CHESS_BOTTOM_MARGIN;
                        layoutParams.leftMargin = AUDIO_CHESS_LEFT_MARGIN;
                    }
                } else {
                    layoutParams.addRule(ALIGN_PARENT_RIGHT);
                    layoutParams.addRule(ALIGN_PARENT_BOTTOM);
                    int AUDIO_LAYOUT_MARGIN = (int) getResources().getDimension(R.dimen.videolayout_audio_margin);
                    int AUDIO_LAYOUT_LEFT_MARGIN = (int) getResources().getDimension(R.dimen.videolayout_audio_left_margin);
                    int AUDIO_LAYOUT_SIZE = (int) getResources().getDimension(R.dimen.videolayout_audio_size);
                    layoutParams.bottomMargin = AUDIO_LAYOUT_MARGIN;
                    if (mOrder == FIRST_VIDEO_LAYOUT) {
                        layoutParams.rightMargin = AUDIO_LAYOUT_MARGIN;
                    } else if (mOrder == SECOND_VIDEO_LAYOUT) {
                        layoutParams.rightMargin = AUDIO_LAYOUT_MARGIN
                                + AUDIO_LAYOUT_SIZE + AUDIO_LAYOUT_LEFT_MARGIN;
                    } else if (mOrder == THIRD_VIDEO_LAYOUT) {
                        layoutParams.rightMargin = AUDIO_LAYOUT_MARGIN
                                + AUDIO_LAYOUT_SIZE + AUDIO_LAYOUT_LEFT_MARGIN
                                + AUDIO_LAYOUT_SIZE + AUDIO_LAYOUT_LEFT_MARGIN;
                    }
                }
                setLayoutParams(layoutParams);
                setPadding(AUDIO_LAYOUT_PADDING, AUDIO_LAYOUT_PADDING, AUDIO_LAYOUT_PADDING, AUDIO_LAYOUT_PADDING);

                RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                addAuidoUI(iconParams);
            }
        }
    }

    public int getOrder() {
        return mOrder;
    }

    public SurfaceView getSurfaceView(){
        return (SurfaceView) getChildAt(0);
    }

    private void addAuidoUI(LayoutParams iconParams) {
        //添加头像图标
        ImageView mIconView = new ImageView(getContext());
        int order = mOrder;
        if (order == MultiplayerVideoLayout.FIRST_VIDEO_LAYOUT) {
            mIconView.setImageResource(R.drawable.multiplayer_icon_first);
        } else if (order == MultiplayerVideoLayout.SECOND_VIDEO_LAYOUT) {
            mIconView.setImageResource(R.drawable.multiplayer_icon_second);
        } else if (order == MultiplayerVideoLayout.THIRD_VIDEO_LAYOUT) {
            mIconView.setImageResource(R.drawable.multiplayer_icon_third);
        }
        iconParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        addAudioView(mIconView, iconParams);

        LayoutParams audioParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        //添加音量图标
        ImageView mAudioView = new ImageView(getContext());
        mAudioView.setImageResource(R.drawable.multiplayer_audio_small);
        audioParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mAudioView.setVisibility(View.INVISIBLE);
        addAudioView(mAudioView, audioParams);
    }

    public void setActivityCallBack(ActivityCallBack mActivityCallBack) {
        this.mActivityCallBack = mActivityCallBack;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            lastX = (int) event.getRawX();
            lastY = (int) event.getRawY();
        } else if (action == MotionEvent.ACTION_MOVE) {
            updateParameters(this, event);
            lastX = (int) event.getRawX();
            lastY = (int) event.getRawY();
        } else if (action == MotionEvent.ACTION_UP) {
            view.performClick();
        }
        return true;
    }


    private void updateParameters(View view, MotionEvent event) {
        RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) view.getLayoutParams();
        Rect r = new Rect();
        mRootView.getDrawingRect(r);

        rl.bottomMargin -= (event.getRawY() - lastY);
        rl.rightMargin -= (event.getRawX() - lastX);
        if (rl.bottomMargin < 0) {
            rl.bottomMargin = 0;
        }
        if (rl.rightMargin < 0) {
            rl.rightMargin = 0;
        }
        if ((r.right - r.left - view.getWidth()) < rl.rightMargin) {
            rl.rightMargin = r.right - r.left - view.getWidth();
        }

        if ((r.bottom - r.top) - (rl.bottomMargin + view.getHeight()) <= 5) {
            rl.bottomMargin = r.bottom - r.top - view.getHeight() - 5;
        }
        setLayoutParams(rl);
//        ((ViewGroup) view.getParent()).updateViewLayout(view, rl);
        // make sure draging view is first front of all
//        view.bringToFront();
    }

    public void addAudioView(ImageView mAudioView, RelativeLayout.LayoutParams mParams) {
        this.mAudioView = mAudioView;
        addView(mAudioView, mParams);
    }

    /**
     * Checks if the device is a tablet or a phone
     *
     * @param activityContext
     *            The Activity Context.
     * @return Returns true if the device is a Tablet
     */
    public static boolean isTabletDevice(Context activityContext) {
        int smallestScreenWidthDp = activityContext.getResources().getConfiguration().smallestScreenWidthDp;
        if (smallestScreenWidthDp >= 600) {
            return true;
        } else {
            return false;
        }
    }

    public ImageView getAudioView() {
        return mAudioView;
    }

    public interface ActivityCallBack {

        void prepareWorking(MultiplayerVideoLayout mFrameLayout);
    }
}
