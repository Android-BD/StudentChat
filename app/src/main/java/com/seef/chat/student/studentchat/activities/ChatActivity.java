package com.seef.chat.student.studentchat.activities;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.seef.chat.student.studentchat.R;
import com.seef.chat.student.studentchat.Utils.Helper;
import com.seef.chat.student.studentchat.adapters.ChatAdapter;
import com.seef.chat.student.studentchat.adapters.OnClickListener;
import com.seef.chat.student.studentchat.models.Chat;
import com.seef.chat.student.studentchat.models.User;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.internal.Utils;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements OnClickListener {

    @BindView(R.id.btnSend)
    FloatingActionButton btnSend;

    @BindView(R.id.txtMessage)
    EditText txtMessage;

    @BindView(R.id.recyclerChat)
    RecyclerView recyclerChat;

    private ArrayList<Chat> listMessages;

    private DatabaseReference dbRef;
    private ChatAdapter adapter;
    private Dialog dialog;

    private CircleImageView imgAvatar;
    private TextView txtUsername, txtEstado, txtCountLike;
    private CheckBox checkLike;
    private ImageView imgClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        configInit();
        //loadProfile();
    }

    private String getHour() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date());
    }

    private String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

    private void configInit() {
        configDataBaseFirebase();
    }

    private void configDataBaseFirebase() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        dbRef = FirebaseDatabase.getInstance().getReference();
        adapter = new ChatAdapter(this, new ArrayList<Chat>(), this);
        configRecyclerView();
    }

    private void configRecyclerView() {
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(adapter);
        listener();

    }

    private void configPositionRecyclerView() {
        recyclerChat.scrollToPosition(adapter.getItemCount()-1);
    }

    @OnClick(R.id.btnSend)
    void sendMessage() {
        if (!txtMessage.getText().toString().trim().equals(""))
            sendMessageFirebase(getChat());
    }

    private void sendMessageFirebase(Chat chat) {
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("messages").push().setValue(chat);
        listener();
    }

    private void getUpdate() {
        adapter.add(listMessages);
        cleanInputText();
        configPositionRecyclerView();
    }

    private void listener() {

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listMessages = new ArrayList<Chat>();
                for (DataSnapshot child: dataSnapshot.child("messages").getChildren()) {
                    Chat chat = child.getValue(Chat.class);
                    listMessages.add(chat);
                }
                getUpdate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private Chat getChat() {
        Chat chat = new Chat();
        chat.setMessage(txtMessage.getText().toString());
        chat.setHour(getHour());
        chat.setDate(getDate());
        chat.setUser(getUser());
        return chat;
    }

    private User getUser() {
        User user = new User();
        user.setId(Helper.ID_USER);
        user.setUsername(Helper.USERNAME);
        return user;
    }

    private void cleanInputText() {
        txtMessage.setText("");
    }

    private void loadProfile() {
        dialog =  new Dialog(this);
        dialog.requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.design_profile);
        configComponent(dialog);
    }

    private void configComponent(final Dialog dialog) {
        imgAvatar = (CircleImageView)dialog.findViewById(R.id.imgAvatar);
        txtUsername = (TextView)dialog.findViewById(R.id.txtUsername);
        txtEstado = (TextView)dialog.findViewById(R.id.txtEstado);
        checkLike = (CheckBox)dialog.findViewById(R.id.checkLike);
        txtCountLike = (TextView)dialog.findViewById(R.id.txtCountLikes);
        imgClose = (ImageView)dialog.findViewById(R.id.imgClose);

        checkLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLike.setChecked(checkLike.isChecked() ? true : false);
                updateLikesUser(Helper.USER_PROFILE);

            }
        });

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void updateLikesUser(User user) {

        Integer likes = Integer.valueOf(user.getLike()) + 1;
        Map<String, Object> updates = new HashMap<>();
        updates.put("likes", likes);
        Helper.REF_USER.updateChildren(updates);
    }

    private void loadInfoProfile(User user) {
        Helper.USER_PROFILE = user;
        if (!user.getPhoto().trim().equals(""))
            Picasso.with(this).load(Uri.parse(user.getPhoto())).into(imgAvatar);
        else
            Picasso.with(this).load(R.drawable.photo_profile).into(imgAvatar);

        txtEstado.setText("Online");

        txtCountLike.setText(user.getLike());

        txtUsername.setText(user.getUsername());
    }

    private void recorrerUsers(final String idUser) {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.child("users").getChildren()) {
                    Helper.REF_USER = child.getRef();
                    User user = child.getValue(User.class);
                    if (user.getId().equals(idUser)) {
                        loadInfoProfile(user);
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(Chat chat) {
        loadProfile();
        recorrerUsers(chat.getUser().getId());
        dialog.show();
    }

}
