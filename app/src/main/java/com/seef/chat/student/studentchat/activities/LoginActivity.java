package com.seef.chat.student.studentchat.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.seef.chat.student.studentchat.R;
import com.seef.chat.student.studentchat.Utils.Helper;
import com.seef.chat.student.studentchat.models.User;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.btnLogin)
    Button btnLogin;

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @BindString(R.string.error_login_google)
    String errorLoginGoogle;

    private DatabaseReference dbRef;
    private GoogleSignInOptions gso;
    private GoogleApiClient gac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        configInit();
    }

    private void configInit() {
        if (validSharePreference()) {
            redirectChat();
        } else {
            configFirebaseAuth();
            configDataBaseFirebase();
        }
    }



    private void configDataBaseFirebase() {
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    private void redirectChat() {
        startActivity(new Intent(LoginActivity.this, ChatActivity.class));
        finish();
    }

    private void configFirebaseAuth() {
        if (gso == null) {
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestProfile()
                    .build();
        }
    }

    @OnClick(R.id.btnLogin)
    void loginGoogle() {
        if (gac == null)
            configGoogleApiClient();

        intentLoginGoogle();
    }

    private void intentLoginGoogle() {
        Intent signIntent = Auth.GoogleSignInApi.getSignInIntent(gac);
        startActivityForResult(signIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            GoogleSignInResult gsr = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (gsr.isSuccess()) {
                GoogleSignInAccount account = gsr.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                messageError(errorLoginGoogle);
            }
        }
    }

    private void messageError(String error) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, error, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private boolean validSharePreference() {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("UserProfile", 0);
        String idUser = sharedPreferences.getString("id", null);
        String username = sharedPreferences.getString("username", null);
        String photo = sharedPreferences.getString("photo", null);
        if (idUser != null) {
            Helper.ID_USER = idUser;
            Helper.PHOTO_USER = photo;
            Helper.USERNAME = username;
            return true;
        }
        return false;
    }

    private void createUser(GoogleSignInAccount account) {
        User user = new User();
        user.setId(account.getId());
        user.setLike("0");
        user.setPhoto(account.getPhotoUrl().toString());
        user.setUsername(account.getDisplayName());
        dbRef.child("users").push().setValue(user);
    }

    private void addIdUserSharePreference(GoogleSignInAccount account) {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("UserProfile", 0);
        sharedPreferences.edit().putString("id", account.getId()).commit();
        sharedPreferences.edit().putString("username", account.getDisplayName()).commit();
        sharedPreferences.edit().putString("photo", account.getPhotoUrl().toString()).commit();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        recorrerUsers(account);
    }

    private void validateSharePreferences() {
        if (validSharePreference())
            redirectChat();
        else
            messageError(errorLoginGoogle);
    }

    private void configGoogleApiClient() {
        gac = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void recorrerUsers(final GoogleSignInAccount account) {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.child("users").getChildren()) {
                    User user = child.getValue(User.class);
                    if (user.getId().equals(account.getId().toString())) {
                        addIdUserSharePreference(account);
                        validateSharePreferences();
                        return;
                    }
                }
                addIdUserSharePreference(account);
                createUser(account);
                validateSharePreferences();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
