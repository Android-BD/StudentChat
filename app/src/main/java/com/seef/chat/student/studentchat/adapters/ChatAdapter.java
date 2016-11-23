package com.seef.chat.student.studentchat.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.seef.chat.student.studentchat.R;
import com.seef.chat.student.studentchat.Utils.Helper;
import com.seef.chat.student.studentchat.models.Chat;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatAdapter extends FirebaseRecyclerAdapter<Chat, ChatAdapter.ChatViewHolder> {

    public ChatAdapter(int modelLayout, DatabaseReference ref) {
        super(Chat.class, modelLayout, ChatAdapter.ChatViewHolder.class, ref);
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    protected void populateViewHolder(ChatViewHolder viewHolder, Chat chat, int position) {
        String username = chat.getUsername();
        String message = chat.getMessage();
        String idUser = chat.getIdUser();
        String hour = chat.getHour();

        if (idUser.trim().equals(Helper.ID_USER.trim())) {
            viewHolder.txtUserSend.setText(username);
            viewHolder.txtMessageSend.setText(message);
            viewHolder.txtHoraSend.setText(hour);
            viewHolder.linearReceived.setVisibility(View.GONE);
        } else {
            viewHolder.txtUserReceived.setText(username);
            viewHolder.txtMessageReceived.setText(message);
            viewHolder.txtHoraReceived.setText(hour);
            viewHolder.linearSend.setVisibility(View.GONE);
        }
    }


    class ChatViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.txtUserSend)
        TextView txtUserSend;
        @BindView(R.id.txtMessageSend)
        TextView txtMessageSend;
        @BindView(R.id.txtUserReceived)
        TextView txtUserReceived;
        @BindView(R.id.txtMessageReceived)
        TextView txtMessageReceived;
        @BindView(R.id.txtHoraSend)
        TextView txtHoraSend;
        @BindView(R.id.txtHoraReceived)
        TextView txtHoraReceived;

        @BindView(R.id.linearSend)
        LinearLayout linearSend;
        @BindView(R.id.linearReceived)
        LinearLayout linearReceived;

        public ChatViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
