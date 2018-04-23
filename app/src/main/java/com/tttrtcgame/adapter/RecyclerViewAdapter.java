package com.tttrtcgame.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tttrtcgame.LocalConfig;
import com.tttrtcgame.R;
import com.tttrtcgame.bean.MessageBean;
import com.tttrtcgame.dialog.SpeekPopupWindow;

import java.util.ArrayList;

import static com.tttrtcgame.bean.MessageBean.MESSAGE_TYPE_TEXT;

/**
 * Created by Administrator on 2017-12-25.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int[] layouts = {R.layout.chat_item_text_self, R.layout.chat_item_text_other, R.layout.chat_item_voice_self, R.layout.chat_item_voice_other};
    private int[] social_layouts = {R.layout.chat_social_text_self, R.layout.chat_social_text_other, R.layout.chat_social_voice_self, R.layout.chat_social_voice_other, R.layout.chat_social_recognize_self, R.layout.chat_social_recognize_other};

    private ArrayList<MessageBean> mChatDatas = new ArrayList();
    private int mLayoutType = 0;
    private OnItemClickListener l;
    private OnItemLongClickListener longClickListener;
    private int mCurrentType = 0;
    private int mCurrentPosition = 0;
    private View mCurrentItem;
    private GestureDetector mGesture = null;
    private Context mContext = null;

    public interface OnItemClickListener {
        void onItemClick(String audioPath);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(String audioPath);
    }

    public RecyclerViewAdapter() {
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.l = l;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener l) {
        this.longClickListener = l;
    }

    public void setLayoutType(int type) {
        this.mLayoutType = type;
    }

    public void add(MessageBean messageBean) {
        mChatDatas.add(messageBean);
        notifyDataSetChanged();
//        notifyItemInserted(mChatDatas.size() - 1);
    }

    public void addRecognizedMessage(MessageBean messageBean) {
        messageBean.recognize = true;
        mChatDatas.add(mCurrentPosition + 1, messageBean);
        notifyDataSetChanged();
//        notifyItemInserted(mCurrentPosition);
    }

    public void updateItem(MessageBean messageBean) {
        mChatDatas.set(mCurrentPosition + 1, messageBean);
        notifyDataSetChanged();
//        notifyItemChanged(mCurrentPosition + 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        mGesture = new GestureDetector(mContext, new GestureListener());
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutType == 0 ? layouts[viewType] :  social_layouts[viewType],parent,false);
        RecyclerView.ViewHolder holder;
        if (viewType == 0 || viewType == 1)
            holder = new TextHolder(view);
        else if (viewType == 2 || viewType == 3){
            holder = new VoiceHolder(view);
            RelativeLayout voiceLayout = view.findViewById(R.id.voice);
            voiceLayout.setOnTouchListener((v, event) -> {
                mCurrentType = viewType + 2;
                mCurrentPosition = (int)holder.itemView.getTag();
                mCurrentItem = v;
                return mGesture.onTouchEvent(event);
            });
        } else {
            holder = new RecognizedHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TextHolder) {
            ((TextHolder)holder).userID.setText(String.valueOf(mChatDatas.get(position).userId));
            ((TextHolder)holder).message.setText(mChatDatas.get(position).message);
        } else if (holder instanceof VoiceHolder) {
            ((VoiceHolder)holder).userID.setText(String.valueOf(mChatDatas.get(position).userId));
            ((VoiceHolder)holder).time.setText(String.valueOf(mChatDatas.get(position).time) + "''");
            holder.itemView.setTag(position);
        } else {
            ((RecognizedHolder)holder).message.setText(mChatDatas.get(position).message);
        }
    }

    @Override
    public int getItemViewType(int position) {
        MessageBean messageBean = mChatDatas.get(position);
        if (messageBean.userId == 0) {
            return mCurrentType;
        } else if (messageBean.userId == LocalConfig.mLoginUserID)
            return messageBean.messageType == MESSAGE_TYPE_TEXT ? 0 : 2;
        else
            return messageBean.messageType == MESSAGE_TYPE_TEXT ? 1 : 3;
    }

    @Override
    public int getItemCount() {
        return mChatDatas.size();
    }

    static class TextHolder extends RecyclerView.ViewHolder{

        private TextView userID;
        private TextView message;

        public TextHolder(View view){
            super(view);
            userID = view.findViewById(R.id.user_id);
            message = view.findViewById(R.id.message);
        }
    }

    static class VoiceHolder extends RecyclerView.ViewHolder{

        private TextView userID;
        private TextView time;

        public VoiceHolder(View view){
            super(view);
            userID = view.findViewById(R.id.message);
            time = view.findViewById(R.id.time);
        }
    }

    static class RecognizedHolder extends RecyclerView.ViewHolder{

        private TextView message;

        public RecognizedHolder(View view){
            super(view);
            message = view.findViewById(R.id.message);
        }
    }


    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mLayoutType == 0) return;
            SpeekPopupWindow speekPopupWindow = SpeekPopupWindow.getInstance(mContext);
            speekPopupWindow.setOnSwitchTextClickLintener(() -> {
                speekPopupWindow.dismiss();
                if (longClickListener != null) {

                    addRecognizedMessage(new MessageBean("识别中..."));
                    longClickListener.onItemLongClick(mChatDatas.get(mCurrentPosition).message);
                }
            });
            speekPopupWindow.show(mCurrentItem);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (l != null)
                l.onItemClick(mChatDatas.get(mCurrentPosition).message);
            return super.onSingleTapUp(e);
        }

    }
}
