package com.seef.chat.student.studentchat.activities;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.seef.chat.student.studentchat.R;
import com.seef.chat.student.studentchat.Utils.Helper;
import com.seef.chat.student.studentchat.adapters.ChatAdapter;
import com.seef.chat.student.studentchat.models.Chat;
import com.seef.chat.student.studentchat.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.btnSend)
    FloatingActionButton btnSend;

    @BindView(R.id.txtMessage)
    EditText txtMessage;

    @BindView(R.id.recyclerChat)
    RecyclerView recyclerChat;

    private ArrayList<Chat> listMessages;

    private DatabaseReference dbRef;
    private ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        configInit();
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
        dbRef = FirebaseDatabase.getInstance().getReference();
        adapter = new ChatAdapter(this, new ArrayList<Chat>());
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

}
