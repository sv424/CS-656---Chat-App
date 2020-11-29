package com.google.firebase.codelab.friendlychat;

import android.webkit.JavascriptInterface;
import com.google.firebase.database.annotations.NotNull;

public final class JSInterface {

    @NotNull
    private final MainActivity callActivity;

    public JSInterface( MainActivity callActivity ){
        this.callActivity = callActivity;
    }

    @JavascriptInterface
    public void onPeerConnected() {
        callActivity.onPeerConnected();
    }

}