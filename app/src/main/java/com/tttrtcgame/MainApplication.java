package com.tttrtcgame;

import android.app.Application;
import android.util.DisplayMetrics;
import android.util.Log;

import com.tttrtcgame.callback.MyTTTRtcEngineEventHandler;
import com.wushuangtech.utils.CrashHandler;
import com.wushuangtech.wstechapi.TTTRtcEngineForGamming;

public class MainApplication extends Application {

    public MyTTTRtcEngineEventHandler mMyTTTRtcEngineEventHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        CrashHandler mCrashHandler = new CrashHandler(getApplicationContext());
        mCrashHandler.init();


        //1.初始化SDK
        //2.创建SDK的实例对象 "a967ac491e3acf92eed5e1b5ba641ab7" test900572e02867fab8131651339518
        TTTRtcEngineForGamming mTTTEngine = TTTRtcEngineForGamming.create(getApplicationContext(), "a967ac491e3acf92eed5e1b5ba641ab7", null);
        if (mTTTEngine == null) {
            System.exit(0);
        }
//        //2.设置SDK的回调接收类
        mMyTTTRtcEngineEventHandler = new MyTTTRtcEngineEventHandler(getApplicationContext());
        mTTTEngine.setTTTRtcEngineForGammingEventHandler(mMyTTTRtcEngineEventHandler);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        Log.d("wzg", "device infos , density : " + dm.density
                + " | width : " + dm.widthPixels + " | height : " + dm.heightPixels);
    }

}
