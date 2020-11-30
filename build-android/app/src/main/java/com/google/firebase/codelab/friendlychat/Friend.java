package com.google.firebase.codelab.friendlychat;

import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

public class Friend {
    private String id;
    private String name;
    private String photoURL;
    private String sessionID;

    public Friend( String friendId, String friendName, String sessionID ){
        this.id = friendId;
        this.name = friendName;
        this.sessionID = sessionID;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getFriendId()
    {
        return id;
    }

    public String getFriendName()
    {
        return name;
    }

    public String getSessionID()
    {
       return sessionID;
    }

}
