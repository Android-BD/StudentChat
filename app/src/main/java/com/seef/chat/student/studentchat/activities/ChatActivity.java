package com.seef.chat.student.studentchat.activities;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mlsdev.rximagepicker.RxImagePicker;
import com.mlsdev.rximagepicker.Sources;
import com.seef.chat.student.studentchat.R;
import com.seef.chat.student.studentchat.Utils.Helper;
import com.seef.chat.student.studentchat.adapters.ChatAdapter;
import com.seef.chat.student.studentchat.adapters.OnClickListener;
import com.seef.chat.student.studentchat.models.Chat;
import com.seef.chat.student.studentchat.models.User;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.functions.Action1;

public class ChatActivity extends AppCompatActivity implements OnClickListener {

    @BindView(R.id.btnSend)
    FloatingActionButton btnSend;

    @BindView(R.id.txtMessage)
    EditText txtMessage;

    @BindView(R.id.recyclerChat)
    RecyclerView recyclerChat;

    @BindView(R.id.imageOptions)
    ImageView imgOptions;

    private ArrayList<Chat> listMessages;

    private DatabaseReference dbRef;
    private StorageReference storageReference;
    private ChatAdapter adapter;
    private Dialog dialog;
    private Dialog dialogSendImage;

    private CircleImageView imgAvatar;
    private TextView txtUsername, txtEstado, txtCountLike;
    private CheckBox checkLike;
    private ImageView imgClose;
    private String urlImage = "";


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
        configStorageFirebase();
    }

    private void configStorageFirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl(Helper.PATH_STORAGE);
    }

    private void configDataBaseFirebase() {
        dbRef = FirebaseDatabase.getInstance().getReference();
        adapter = new ChatAdapter(this, new ArrayList<Chat>(), this);
        configRecyclerView();
    }

    private void configRecyclerView() {
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(adapter);
        recyclerChat.setHasFixedSize(true);
        recyclerChat.setItemViewCacheSize(10);
        recyclerChat.setDrawingCacheEnabled(true);
        recyclerChat.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        listener();

    }

    private void configPositionRecyclerView() {
        recyclerChat.scrollToPosition(adapter.getItemCount() - 1);
    }

    @OnClick(R.id.btnSend)
    void sendMessage() {
        if (!txtMessage.getText().toString().trim().equals(""))
            sendMessageFirebase(getChat());
    }

    private void sendMessageFirebase(Chat chat) {
        dbRef.child("messages").push().setValue(chat);
        urlImage = "";
        listener();
    }

    private void getUpdate() {
        adapter.add(listMessages);
        cleanInputText();
        configPositionRecyclerView();
    }

    private void listener() {
        DatabaseReference databaseReference = dbRef.child("messages").getRef();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listMessages = new ArrayList<Chat>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
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
        chat.setPhoto(urlImage);
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
        dialog = new Dialog(this);
        dialog.requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.design_profile);
        configComponent(dialog);
    }

    private void configComponent(final Dialog dialog) {
        imgAvatar = (CircleImageView) dialog.findViewById(R.id.imgAvatar);
        txtUsername = (TextView) dialog.findViewById(R.id.txtUsername);
        txtEstado = (TextView) dialog.findViewById(R.id.txtEstado);
        checkLike = (CheckBox) dialog.findViewById(R.id.checkLike);
        txtCountLike = (TextView) dialog.findViewById(R.id.txtCountLikes);
        imgClose = (ImageView) dialog.findViewById(R.id.imgClose);

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

    @OnClick(R.id.imageOptions)
    void openGalery() {
        RxImagePicker.with(this).requestImage(Sources.GALLERY).subscribe(new Action1<Uri>() {
            @Override
            public void call(Uri uri) {
                showImageSend(uri);
            }
        });
    }

    private void showImageSend(Uri uri) {
        dialogSendImage = new Dialog(this);

        dialogSendImage.requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        dialogSendImage.setContentView(R.layout.design_send);

        ImageView btnSendImage = (ImageView) dialogSendImage.findViewById(R.id.btnSendImage);
        ImageView btnCancelImage = (ImageView) dialogSendImage.findViewById(R.id.btnCancelImage);
        ImageView imageSend = (ImageView) dialogSendImage.findViewById(R.id.imgSend);
        Picasso.with(this).load(uri).into(imageSend);
        configEventComponentSendImage(btnSendImage, btnCancelImage, imageSend);
        dialogSendImage.show();
    }

    private void configEventComponentSendImage(ImageView btnSendImage, ImageView btnCancelImage, final ImageView imageSend) {
        btnCancelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSendImage.dismiss();
            }
        });

        btnSendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage(imageSend);
                dialogSendImage.dismiss();
            }
        });
    }

    private void uploadImage(final ImageView imageSend) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Subiendo Imagen...");

        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                imageSend.setDrawingCacheEnabled(true);
                imageSend.buildDrawingCache();
                Bitmap bitmap = imageSend.getDrawingCache();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                String timeStamp = "image" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".png";
                urlImage = timeStamp;
                StorageReference uploadImageRef = storageReference.child("images/" + timeStamp);

                UploadTask uploadTask = uploadImageRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        sendMessageFirebase(getChat());
                        progressDialog.dismiss();
                    }
                });
                return null;
            }
        };
        progressDialog.show();
        task.execute();
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
