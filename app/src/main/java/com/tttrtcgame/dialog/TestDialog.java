package com.tttrtcgame.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.tttrtcgame.LocalConfig;
import com.tttrtcgame.R;


/**
 * Created by Administrator on 2017-10-11.
 */
public class TestDialog extends Dialog implements View.OnClickListener {

    private Context mContext;
    private EditText mPort;
    private EditText mIP;

    public TestDialog(@NonNull Context context) {
        super(context, R.style.NoBackGroundDialog);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_dialog_layout);

        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.width = dm.widthPixels;
            getWindow().setAttributes(lp);
        }

        mIP = (EditText) findViewById(R.id.ip);
        mPort = (EditText) findViewById(R.id.port);
        findViewById(R.id.cancel).setOnClickListener(this);
        setServerParams();
    }

    public void setServerParams(){
        if (!TextUtils.isEmpty(LocalConfig.mIP)) {
            mIP.setText(LocalConfig.mIP);
        }

        if (LocalConfig.mPort != -1) {
            mPort.setText(String.valueOf(LocalConfig.mPort));
        }
    }

    public void onOKButtonClick() {
        Editable mIPText = mIP.getText();
        if (!TextUtils.isEmpty(mIPText)) {
            LocalConfig.mIP = mIPText.toString();
        } else {
            LocalConfig.mIP = "";
        }

        Editable mPortText = mPort.getText();
        if (!TextUtils.isEmpty(mPortText)) {
            LocalConfig.mPort = Integer.valueOf(mPortText.toString());
        } else {
            LocalConfig.mPort = -1;
        }
        this.dismiss();
    }

    @Override
    public void onClick(View v) {
        onOKButtonClick();
    }
}
