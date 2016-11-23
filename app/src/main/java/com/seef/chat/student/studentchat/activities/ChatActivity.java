package com.seef.chat.student.studentchat.activities;

import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.seef.chat.student.studentchat.R;
import com.seef.chat.student.studentchat.Utils.Helper;
import com.seef.chat.student.studentchat.adapters.ChatAdapter;
import com.seef.chat.student.studentchat.models.Chat;

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

    private void configInit() {
        configDataBaseFirebase();
    }

    private void configDataBaseFirebase() {
        /*optimizar codigo*/
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        adapter = new ChatAdapter(R.layout.row_chat, dbRef, this);
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
        Chat chat = new Chat();
        chat.setIdUser(Helper.ID_USER);
        chat.setUsername(Helper.USERNAME);
        chat.setPhoto(Helper.PHOTO_USER);
        chat.setMessage(txtMessage.getText().toString());
        dbRef.push().setValue(chat);
        cleanInputText();
    }

    private void cleanInputText() {
        txtMessage.setText("");
    }

}
