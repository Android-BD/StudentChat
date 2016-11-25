package com.seef.chat.student.studentchat.activities;

import android.app.Dialog;
import android.media.audiofx.LoudnessEnhancer;
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

import com.google.firebase.database.ChildEventListener;
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
import java.util.List;
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
                getUpdateValues();
                existUserLike();
            }
        });

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void likeCreateUser(User user) {
        User userLike = new User();
        userLike.setId(Helper.ID_USER);
        userLike.setUsername(Helper.USERNAME);
        Integer likes = Integer.valueOf(user.getLike()) + 1;
        Map<String, Object> updates = new HashMap<>();
        updates.put("like", likes.toString());
        Helper.REF_USER.child("users").push().setValue(userLike);
        Helper.REF_USER.updateChildren(updates);
        txtCountLike.setText(likes.toString());

    }

    private void loadInfoProfile(User user) {
        Helper.USER_PROFILE = user;
        if (!user.getPhoto().trim().equals(""))
            Picasso.with(this).load(Uri.parse(user.getPhoto())).into(imgAvatar);
        else
            Picasso.with(this).load(R.drawable.photo_profile).into(imgAvatar);

        txtEstado.setText("Estado de usuario proximamente...");

        txtCountLike.setText(user.getLike());

        txtUsername.setText(user.getUsername());
    }

    private void getUser(Chat chat) {
        DatabaseReference db = dbRef.child("users").getRef();
        db.orderByChild("id").equalTo(chat.getUser().getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot child = dataSnapshot.getChildren().iterator().next();
                User user = child.getValue(User.class);
                Helper.REF_USER = child.getRef();
                loadInfoProfile(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUpdateValues() {
        DatabaseReference dbreference = dbRef.child("users").getRef();
        dbreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().orderByChild("id").equalTo(Helper.USER_CHAT.getUser().getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        loadInfoProfile(dataSnapshot.getChildren().iterator().next().getValue(User.class));
                        /*Helper.USER_PROFILE = ;*/
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void existUserLike() {
        DatabaseReference db = dbRef.child("users").getRef();
        db.orderByChild("id").equalTo(Helper.USER_CHAT.getUser().getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getChildren().iterator().next().child("users").getRef().orderByChild("id").equalTo(Helper.ID_USER).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getChildrenCount() > 0) {
                            dislikeDeleteUser(Helper.USER_PROFILE);
                        } else {
                            likeCreateUser(Helper.USER_PROFILE);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void dislikeDeleteUser(User user) {
        Integer likes = Integer.valueOf(user.getLike()) - 1;
        Map<String, Object> updates = new HashMap<>();
        updates.put("like", likes.toString());
        Helper.REF_USER.updateChildren(updates);
        txtCountLike.setText(likes.toString());

        Helper.REF_USER.child("users").getRef().orderByChild("id").equalTo(Helper.ID_USER).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot child = dataSnapshot.getChildren().iterator().next();
                child.getRef().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void activeInhabilityLike(boolean checked) {
        checkLike.setChecked(checked);
    }

    private void verificarLikeUser(Chat chat) {
        DatabaseReference db = dbRef.child("users").getRef();
        db.orderByChild("id").equalTo(chat.getUser().getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getChildren().iterator().next().child("users").getRef().orderByChild("id").equalTo(Helper.ID_USER).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getChildrenCount() > 0) {
                            activeInhabilityLike(true);
                        } else
                            activeInhabilityLike(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(Chat chat) {
        Helper.USER_CHAT = chat;
        loadProfile();
        verificarLikeUser(chat);
        getUser(chat);
        verificarMismoUsuario(chat);
        dialog.show();

    }

    private void verificarMismoUsuario(Chat chat) {
        if (chat.getUser().getId().equals(Helper.ID_USER)) {
            checkLike.setEnabled(false);
            activeInhabilityLike(false);
        } else {
            checkLike.setEnabled(true);
        }
    }

}
