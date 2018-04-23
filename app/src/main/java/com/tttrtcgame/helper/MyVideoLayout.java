package com.tttrtcgame.helper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.tttrtcgame.utils.MyLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyVideoLayout extends RelativeLayout implements ViewTreeObserver.OnGlobalLayoutListener {

    public static final int MAX_VIDEO_NUM = 4;
    private FrameLayout mFirstVideoLayout;
    private FrameLayout mSecondVideoLayout;
    private FrameLayout mThirdVideoLayout;
    private FrameLayout mFourVideoLayout;
    private int mTotalWidth;
    public ConcurrentHashMap<Long, FrameLayout> mShowingDevices;
    public ConcurrentHashMap<Long, Boolean> mShowingUser;
    private ActivityCallBack mActivityCallBack;

    public MyVideoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(this);
        mShowingDevices = new ConcurrentHashMap<>();
        mShowingUser = new ConcurrentHashMap<>();
    }

    @Override
    public void onGlobalLayout() {
        if (mTotalWidth == 0) {
            mTotalWidth = getWidth();
            int mTotalHeight = getHeight();
            RelativeLayout.LayoutParams layoutParams = (LayoutParams) getLayoutParams();
            int mTempHeight = mTotalWidth * 4 / 3;
            int mTempWidth;

            int mVideoWidth;
            int mVideoHeight;
            if (mTempHeight <= mTotalHeight) {
                layoutParams.height = mTempHeight;
                mVideoWidth = mTotalWidth / 2;
                mVideoHeight = mTempHeight / 2;
            } else {
                mTempWidth = mTotalHeight * 3 / 4;
                int margin = mTotalWidth - mTempWidth;
                if (margin < 0) {
                    return ;
                }
                mVideoWidth = mTempWidth / 2;
                mVideoHeight = mTotalHeight / 2;
                layoutParams.leftMargin += margin / 2;
                layoutParams.rightMargin += margin / 2;
            }
            setLayoutParams(layoutParams);

            for (int i = 0; i < MAX_VIDEO_NUM; i++) {
                LayoutParams params = new LayoutParams(mVideoWidth, mVideoHeight);
                FrameLayout mVideoLayout = new FrameLayout(getContext());
                mVideoLayout.setId(i);
                addFirstVideoLayout(mVideoLayout, params, i);
            }

            if (mActivityCallBack != null) {
                mActivityCallBack.prepareOpenVideo();
            }
        }
    }

    public void setActivityCallBack(ActivityCallBack mActivityCallBack) {
        this.mActivityCallBack = mActivityCallBack;
    }

    private void addFirstVideoLayout(FrameLayout mVideoLayout, LayoutParams params, int index) {
        switch (index) {
            case 0:
                mFirstVideoLayout = mVideoLayout;
                break;
            case 1:
                mSecondVideoLayout = mVideoLayout;
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                break;
            case 2:
                mThirdVideoLayout = mVideoLayout;
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
            case 3:
                mFourVideoLayout = mVideoLayout;
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
        }
        mVideoLayout.setLayoutParams(params);
        addView(mVideoLayout);
    }

    public synchronized FrameLayout getVideoChildLayout(long uid) {
        MyLog.d("openUserVideo getRemoteViewParentLayout uid... " + uid
                + " | mShowingDevices size : " + mShowingDevices.size());
        for (Map.Entry<Long, FrameLayout> next : mShowingDevices.entrySet()) {
            MyLog.d("openUserVideo getRemoteViewParentLayout temp id... " + next.getKey()
                    + " | layout : " + next.getValue());
        }
        if (mShowingDevices.get(uid) != null) {
            return null;
        }

        if (mFirstVideoLayout != null && mFirstVideoLayout.getChildCount() == 0) {
            mShowingDevices.put(uid, mFirstVideoLayout);
            return mFirstVideoLayout;
        }

        if (mSecondVideoLayout != null && mSecondVideoLayout.getChildCount() == 0) {
            mShowingDevices.put(uid, mSecondVideoLayout);
            return mSecondVideoLayout;
        }

        if (mThirdVideoLayout != null && mThirdVideoLayout.getChildCount() == 0) {
            mShowingDevices.put(uid, mThirdVideoLayout);
            return mThirdVideoLayout;
        }

        if (mFourVideoLayout != null && mFourVideoLayout.getChildCount() == 0) {
            mShowingDevices.put(uid, mFourVideoLayout);
            return mFourVideoLayout;
        }
        return null;
    }

    public synchronized void removeVideoChild(long uid) {
        for (Map.Entry<Long, FrameLayout> next : mShowingDevices.entrySet()) {
            MyLog.d("openUserVideo removeVideoChild getRemoteViewParentLayout temp id... " + next.getKey()
                    + " | layout : " + next.getValue());
        }
        FrameLayout frameLayout = mShowingDevices.remove(uid);
        if (frameLayout != null) {
            frameLayout.removeAllViews();
        }
    }

    public void clear() {
        mFirstVideoLayout.removeAllViews();
        mSecondVideoLayout.removeAllViews();
        mThirdVideoLayout.removeAllViews();
        mFourVideoLayout.removeAllViews();
        removeAllViews();
        mShowingDevices.clear();
        mShowingDevices = null;
        mFirstVideoLayout = null;
        mSecondVideoLayout = null;
        mThirdVideoLayout = null;
        mFourVideoLayout = null;
        mActivityCallBack = null;
    }

    public interface ActivityCallBack {

        void prepareOpenVideo();
    }
}
