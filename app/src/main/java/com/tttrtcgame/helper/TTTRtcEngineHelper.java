package com.tttrtcgame.helper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.tttrtcgame.LocalConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangzhiguo on 17/11/24.
 */

public class TTTRtcEngineHelper {

    private Activity mActivity;
    private ProgressDialog mDialog;

    public TTTRtcEngineHelper(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public void splashShowWaittingDialog() {
        if (mDialog == null) {
            mDialog = ProgressDialog.show(mActivity, "", "正在进入房间...");
        } else {
            mDialog.show();
        }
    }

    public void splashDismissWaittingDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    public boolean splashCheckSetting(String mRoomID) {
        if (TextUtils.isEmpty(mRoomID)) {
            Toast.makeText(mActivity, "房间ID为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        Pattern p = Pattern.compile("[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]");
        String roomID = mRoomID.trim();
        Matcher matcher = p.matcher(roomID);
        if (matcher.matches()) {
            Toast.makeText(mActivity, "房间ID输入不合法", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            LocalConfig.mLoginRoomID = Long.valueOf(roomID);
        } catch (Exception e) {
            Toast.makeText(mActivity, "房间ID输入不合法", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean inRangeOfView(View view, MotionEvent ev) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        return !(ev.getX() < x || ev.getX() > (x + view.getWidth()) || ev.getY() < y || ev.getY() > (y + view.getHeight()));
    }
}
