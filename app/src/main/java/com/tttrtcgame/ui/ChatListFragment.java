package com.tttrtcgame.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.tttrtcgame.LocalConfig;
import com.tttrtcgame.R;
import com.tttrtcgame.adapter.RecyclerViewAdapter;
import com.tttrtcgame.bean.MessageBean;
import com.tttrtcgame.dialog.ChatDialog;
import com.tttrtcgame.helper.SpaceItemDecoration;
import com.wushuangtech.wstechapi.TTTRtcEngineForGamming;

import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {

    private RecyclerView mChatView;
    private RecyclerViewAdapter mChatAdapter = new RecyclerViewAdapter();
    private TTTRtcEngineForGamming mTTTEngine;
    private Button mMessageButton;
    private RelativeLayout mExchangeTextVoice;
    private boolean mHideControler = false;

    public ChatListFragment() {
        // Required empty public constructor
        mTTTEngine = TTTRtcEngineForGamming.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_list, container, false);
        mChatView = v.findViewById(R.id.chat_list);
        // 设置布局管理器
        mChatView.setLayoutManager(new LinearLayoutManager(getContext()));
        // 设置adapter
        mChatView.setAdapter(mChatAdapter);
        mChatAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String audioPath) {
                mTTTEngine.playChatAudio(audioPath);
            }
        });
        // 设置Item添加和移除的动画
        mChatView.setItemAnimator(new DefaultItemAnimator());
        mChatView.addItemDecoration(new SpaceItemDecoration(20));

        mExchangeTextVoice = v.findViewById(R.id.exchange_text_voice);
        mExchangeTextVoice.setOnClickListener(view -> {

            ChatDialog chatDialog = new ChatDialog(getContext(), ChatDialog.HORIZONTAL);
            chatDialog.setOnSendMessageListener(message -> {
                mTTTEngine.sendChatMessage(0, 1, "0", message);
            });
            chatDialog.show();
        });

        mMessageButton = v.findViewById(R.id.chat_voice);
        mMessageButton.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    //按住事件发生后执行代码的区域
                    mTTTEngine.startRecordChatAudio();
                    mMessageButton.setText("松开结束");
                    break;

                case MotionEvent.ACTION_UP:
                    //松开事件发生后执行代码的区域
                    mMessageButton.setText("按住说话");
                    String seqID = UUID.randomUUID().toString() + System.currentTimeMillis();
                    mTTTEngine.stopRecordAndSendChatAudio(0, seqID);
                    break;

                default:

                    break;
            }
            return false;
        });

        if (mHideControler)
            v.findViewById(R.id.send_message).setVisibility(View.GONE);
        return v;
    }

    public void addMessage(MessageBean messageBean) {
        mChatAdapter.add(messageBean);
        mChatView.smoothScrollToPosition(mChatAdapter.getItemCount());
    }

    public void setLayoutType(int type) {
        mChatAdapter.setLayoutType(type);
    }

    public void hideSendControler() {
        mHideControler = true;
    }

}
