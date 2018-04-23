package com.tttrtcgame.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.tttrtcgame.R;

/**
 * Created by Administrator on 2018-01-15.
 */

public class ChatDialog extends Dialog implements View.OnClickListener{

    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;

    private int mOrientation = VERTICAL;

    private EditText mMessage = null;
    private OnSendMessageListener mSendMessageListener = null;

    public ChatDialog(@NonNull Context context, int orientation) {
        super(context, R.style.ChatDialog);
        mOrientation = orientation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mOrientation == VERTICAL) {
            setContentView(R.layout.v_chat_dialog);
        } else {
            setContentView(R.layout.h_chat_dialog);
        }
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.BOTTOM);
        lp.width = -1;
        dialogWindow.setAttributes(lp);

        mMessage = findViewById(R.id.message);
        findViewById(R.id.send_message).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_message:
                if (mSendMessageListener != null) {
                    String message = mMessage.getText().toString();
                    if (TextUtils.isEmpty(message)) {
                        Toast.makeText(getContext(), "消息为NULL", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        mSendMessageListener.onSendMessage(message);
                        mMessage.setText("");
                    }
                }
                break;
        }
    }

    public void setOnSendMessageListener(OnSendMessageListener l) {
        this.mSendMessageListener = l;
    }

    public interface OnSendMessageListener {
        void onSendMessage(String message);
    }
}
