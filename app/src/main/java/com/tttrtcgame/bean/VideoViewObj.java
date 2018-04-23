package com.tttrtcgame.bean;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by wangzhiguo on 17/9/30.
 */

public class VideoViewObj {
    public int mIndex;
    public long mBindUid;
    public boolean mIsUsing;
    public boolean mVideoOpend;
    public View mRootBG;
    public ImageView mSpeakImage;
    public TextView mRemoteUserIndex;
    public ImageView mRemoteUserIcon;
    public TextView mRemoteUserID;
    public View mLocalNameFlag;

    public void clear() {
        mRootBG = null;
        mSpeakImage = null;
        mRemoteUserIndex = null;
        mRemoteUserIcon = null;
        mRemoteUserID = null;
    }
}
