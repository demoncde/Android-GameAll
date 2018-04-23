package com.tttrtcgame.bean;

import android.support.annotation.NonNull;

/**
 * Created by root on 17-2-21.
 */

public class EnterUserInfo implements Comparable<EnterUserInfo> {

    private long id;
    private String device;
    private long mSessionID;
    private boolean agreespeak;
    private int speak_status;
    private int mRole;
    private boolean isVideoOpen;
    private boolean mIsVideoDecoded;
    public float mYLocation;
    public float mXLocation;

    public EnterUserInfo(long uid, int mRole) {
        this.id = uid;
        this.mRole = mRole;
    }

    public EnterUserInfo(long uid, int mRole, float mYLocation, float mXLocation) {
        this.id = uid;
        this.mRole = mRole;
        this.mYLocation = mYLocation;
        this.mXLocation = mXLocation;
    }

    public long getSessionID() {
        return mSessionID;
    }

    public void setSessionID(long mSessionID) {
        this.mSessionID = mSessionID;
    }

    public boolean isAgreespeak() {
        return agreespeak;
    }

    public void setAgreespeak(boolean agreespeak) {
        this.agreespeak = agreespeak;
    }

    public int getSpeak_status() {
        return speak_status;
    }

    public void setSpeak_status(int speak_status) {
        this.speak_status = speak_status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public int getRole() {
        return mRole;
    }

    public boolean isVideoOpen() {
        return isVideoOpen;
    }

    public void setVideoOpen(boolean videoOpen) {
        isVideoOpen = videoOpen;
    }

    public boolean isVideoDecoded() {
        return mIsVideoDecoded;
    }

    public void setVideoDecoded(boolean mIsVideoDecoded) {
        this.mIsVideoDecoded = mIsVideoDecoded;
    }

    @Override
    public int compareTo(@NonNull EnterUserInfo o) {
        if (mYLocation > o.mYLocation) {
            return 1;
        } else if (mYLocation == o.mYLocation) {
            if (mXLocation > o.mXLocation) {
                return 1;
            } else {
                return -1;
            }
        } else {
            return -1;
        }

//        if (mCompareValue > o.mCompareValue) {
//            return 1;
//        } else {
//            return -1;
//        }
    }
}
