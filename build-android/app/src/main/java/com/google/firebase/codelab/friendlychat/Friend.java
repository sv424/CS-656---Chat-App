package com.google.firebase.codelab.friendlychat;

import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

public class Friend {
    private String id;
    private String name;
    private String photoURL;
    private String sessionID;

    public Friend( FirebaseUser user ){
        this.id = user.getUid();
        this.name = user.getDisplayName();
        if (user.getPhotoUrl() != null) { this.photoURL = user.getPhotoUrl().toString(); }
        this.sessionID = null;
    }

    public String getFriendID(){
        return this.id;
    }

    public String getFriendName(){
        return this.name;
    }

    public String getPhotoURL(){
        return this.photoURL;
    }

    public String getSessionID(){
        return this.sessionID;
    }

    public void setSessionID( String s ){
        this.sessionID = s;
    }

}
