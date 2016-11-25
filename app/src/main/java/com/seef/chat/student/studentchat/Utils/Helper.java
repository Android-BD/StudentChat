package com.seef.chat.student.studentchat.Utils;

import com.google.firebase.database.DatabaseReference;
import com.seef.chat.student.studentchat.models.Chat;
import com.seef.chat.student.studentchat.models.User;

public class Helper {
    public static DatabaseReference REF_USER;
    public static String ID_USER;
    public static String PHOTO_USER;
    public static String USERNAME;
    public static User USER_PROFILE;
    public static Chat USER_CHAT;

    public static String PATH_STORAGE = "gs://studentchat-afa12.appspot.com";
}
