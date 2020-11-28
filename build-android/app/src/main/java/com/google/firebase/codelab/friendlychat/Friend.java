package com.google.firebase.codelab.friendlychat;

import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

public class Friend {
    private String id0;
    private String name0;
    private String id1;
    private String name1;
    private String sessionID;

    public Friend( FirebaseUser user, String requestName ){
        this.id0 = user.getUid();
        this.name0 = user.getDisplayName();
        this.id1 = null;
        this.name1 = requestName;
        this.sessionID = null;
    }

    public void accept( FirebaseUser user ){
        if( this.id1.equals( null ) ) {
            this.id1 = user.getUid();
            this.sessionID = UUID.randomUUID().toString();
        }
    }

    public String getFriendID( String id ){
        if( this.id0.equals( id ) )
            return id1;
        else if( this.id1.equals( id ) )
            return id0;
        else
            return null;
    }

    public String getFriendName( String id ){
        if( this.id0.equals( id ) )
            return name1;
        else if( this.id1.equals( id ) )
            return name0;
        else
            return null;
    }

    public String getSessionID( String id ){
        if( this.id0.equals( id ) || this.id1.equals( id ) )
            return sessionID;
        else
            return null;
    }

    public boolean ack(){
        return ( !this.id0.equals( null ) && !this.id1.equals( null ) );
    }

    public boolean request( FirebaseUser user ){
        return ( this.id0.equals( null ) && this.name1.equals( user.getDisplayName() ) );
    }

}
