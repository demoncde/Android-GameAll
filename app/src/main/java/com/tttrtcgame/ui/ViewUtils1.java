package com.tttrtcgame.ui;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by wangzhiguo on 17/12/21.
 */

public class ViewUtils {

    private boolean inRangeOfView(View view, MotionEvent ev){
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        return !(ev.getX() < x || ev.getX() > (x + view.getWidth()) || ev.getY() < y || ev.getY() > (y + view.getHeight()));
    }
}
