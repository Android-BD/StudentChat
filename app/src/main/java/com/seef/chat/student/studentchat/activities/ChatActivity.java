package com.seef.chat.student.studentchat.activities;

import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.seef.chat.student.studentchat.R;
import com.seef.chat.student.studentchat.Utils.Helper;
import com.seef.chat.student.studentchat.adapters.ChatAdapter;
import com.seef.chat.student.studentchat.models.Chat;

import java.text.SimpleDateFormat;
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

    private DatabaseReference dbRef;
    private FirebaseRecyclerAdapter adapter;

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
        adapter = new ChatAdapter(R.layout.row_chat, dbRef);
        configRecyclerView();
    }

    private void configRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        recyclerChat.setLayoutManager(linearLayoutManager);
        recyclerChat.setAdapter(adapter);
    }

    @OnClick(R.id.btnSend)
    void sendMessage() {
        dbRef.push().setValue(getChat());
        cleanInputText();
    }

    private Chat getChat() {
        Chat chat = new Chat();
        chat.setIdUser(Helper.ID_USER);
        chat.setUsername(Helper.USERNAME);
        chat.setPhoto(Helper.PHOTO_USER);
        chat.setMessage(txtMessage.getText().toString());
        chat.setHour(getHour());
        chat.setDate(getDate());
        return chat;
    }

    private void cleanInputText() {
        txtMessage.setText("");
    }

}
