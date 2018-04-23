package com.tttrtcgame.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tttrtcgame.R;

/**
 * Created by Administrator on 2018-01-26.
 */

public class SpeekPopupWindow extends PopupWindow {

    private static OnSwitchTextClickLintener l;

    public interface OnSwitchTextClickLintener {
        void switchTextClick();
    }

    public static SpeekPopupWindow getInstance(Context context) {
        View v = LayoutInflater.from(context).inflate(R.layout.speek_popupwindow, null);
        TextView voice2text = v.findViewById(R.id.voice2text);
        voice2text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (l != null)
                    l.switchTextClick();
            }
        });
        return new SpeekPopupWindow(v);
    }

    public void setOnSwitchTextClickLintener(OnSwitchTextClickLintener l) {
        this.l = l;
    }

    private SpeekPopupWindow(View contentView) {
        super(contentView, 200, 120);
        setOutsideTouchable(true);
    }

    public void show(View v) {
        showAsDropDown(v, (v.getWidth() - 200) / 2, -1 * (v.getHeight() + 130));
    }
}
