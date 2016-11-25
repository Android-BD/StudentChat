package com.seef.chat.student.studentchat.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.seef.chat.student.studentchat.R;
import com.seef.chat.student.studentchat.Utils.Helper;
import com.seef.chat.student.studentchat.models.Chat;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private List<Chat> chatMessages;
    private OnClickListener onClickListener;

    public ChatAdapter(Context context, List<Chat> chatMessages, OnClickListener onClickListener) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.onClickListener = onClickListener;

    }

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        Chat chat = this.chatMessages.get(position);

        String username = chat.getUser().getUsername();
        String message = chat.getMessage();
        String idUser = chat.getUser().getId();
        String hour = chat.getHour();
        String photo = chat.getPhoto();

        if (idUser.trim().equals(Helper.ID_USER.trim())) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            lp.gravity = Gravity.RIGHT;
            lp.setMargins(90, 0, 20, 0);
            holder.linearSend.setLayoutParams(lp);
            holder.linearContent.setBackground(holder.chatRigth);
            holder.linearContent.setPadding(20, 10, 20, 10);
            holder.txtUserSend.setGravity(Gravity.RIGHT);
            holder.txtUserSend.setTextColor(holder.colorUserSend);
        } else {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            lp.gravity = Gravity.LEFT;
            lp.setMargins(20, 0, 90, 0);
            holder.linearSend.setLayoutParams(lp);
            holder.linearContent.setBackground(holder.chatLeft);
            holder.linearContent.setPadding(20, 10, 20, 10);
            holder.txtUserSend.setGravity(Gravity.LEFT);
            holder.txtUserSend.setTextColor(holder.colorUserReceived);
        }

        holder.txtUserSend.setText(username);
        holder.txtHoraSend.setText(hour);
        holder.txtMessageSend.setText(message);
        //if (!message.trim().equals(""))
            //holder.txtMessageSend.setText(message);
        /*else {
            holder.linearContentMessage.removeView(holder.txtMessageSend);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(400, 400);
            holder.imgMessage.setLayoutParams(lp);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReferenceFromUrl(Helper.PATH_STORAGE);
            storageReference.child("/images/" + photo).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(context)
                            .load(uri)
                            .fit()
                            .centerCrop()
                            .into(holder.imgMessage);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

            holder.linearContentMessage.setGravity(Gravity.CENTER);
        }*/
        holder.setOnClickListener(chat, onClickListener);

    }

    public void add(ArrayList<Chat> listChat) {
        this.chatMessages = listChat;
        this.notifyDataSetChanged();
    }

    public void add(Chat chat) {
        this.chatMessages.add(chat);
        this.notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return this.chatMessages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtUserSend)
        TextView txtUserSend;
        @BindView(R.id.txtMessageSend)
        TextView txtMessageSend;
        @BindView(R.id.txtHoraSend)
        TextView txtHoraSend;

        @BindView(R.id.linearSend)
        LinearLayout linearSend;
        @BindView(R.id.linearContent)
        LinearLayout linearContent;
        @BindView(R.id.contentMessage)
        LinearLayout linearContentMessage;

        @BindDrawable(R.drawable.chat_left)
        Drawable chatLeft;
        @BindDrawable(R.drawable.chat_right)
        Drawable chatRigth;

        @BindDrawable(R.drawable.logo_student_chat)
        Drawable logoStudentChat;

        @BindColor(R.color.userSend)
        ColorStateList colorUserSend;
        @BindColor(R.color.userReceived)
        ColorStateList colorUserReceived;

        /*@BindView(R.id.imgMessage)
        ImageView imgMessage;*/

        @OnClick(R.id.txtUserSend)
        void infoProfileSend() {

            Toast.makeText(itemView.getContext(), "Send", Toast.LENGTH_SHORT).show();
        }

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setOnClickListener(final Chat chat, final OnClickListener onClickListener) {

            txtUserSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickListener.onClick(chat);
                }
            });
        }
    }

}
