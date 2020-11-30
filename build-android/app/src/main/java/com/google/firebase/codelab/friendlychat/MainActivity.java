/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.codelab.friendlychat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.database.ktx.database;
//import com.google.firebase.ktx.Firebase;

import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.util.Stack;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView messageImageView;
        TextView messengerTextView;
        CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }
    }

    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_IMAGE = 2;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleSignInClient mSignInClient;
    private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";
    private String sessionID = "0";

    public static final String FRIENDS_CHILD = "friends";
    public static final String ACTIONS_CHILD = "action";

    private LinearLayout mMessageInput;
    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private ImageView mAddMessageImageView;

    private RelativeLayout mCallRing;
    private TextView mIncomingCallTxt;
    private ImageView mCallAccept;
    private ImageView mCallReject;

    private RelativeLayout mCallButtons;
    private ImageView mCallVoice;
    private ImageView mCallVideo;

    private RelativeLayout mCallVoiceControl;
    private ImageView mToggleVoiceMic;
    private ImageView mToggleVoiceSpk;
    private ImageView mEndCallVoice;

    private LinearLayout mCallVideoControl;
    private ImageView mToggleVideoCam;
    private ImageView mToggleVideoMic;
    private ImageView mToggleVideoSpk;
    private ImageView mEndCallVideo;


    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>
            mFirebaseAdapter;

    private Friend friend;
    // TODO: Add code to get friend object from friend list.

    //PeerJS variables
    private static final String VIDEO_CALL = "VideoCall";
    private static final String VOICE_CALL = "VoiceCall";
    private static final String SEND_CALL = "SendCall";
    private static final String ACCEPT_CALL = "AcceptCall";
    private static final String END_CALL = "EndCall";
    private String[] voicePermissions = {Manifest.permission.RECORD_AUDIO};
    private String[] videoPermissions = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private int requestCode = 1;
    private WebView webView;
    private String callerID;
    private String callerUserID;
    private boolean isVideoCall = false;
    public boolean isPeerConnected = false;
    private boolean micOn = true;
    private boolean camOn = true;
    private boolean isMute = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Set default username is anonymous.
        mUsername = ANONYMOUS;


       // sessionUserName = session.getusername();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize ProgressBar and RecyclerView.
        mMessageInput = (LinearLayout) findViewById(R.id.messageInput);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        SnapshotParser<FriendlyMessage> parser = new SnapshotParser<FriendlyMessage>() {
            @Override
            public FriendlyMessage parseSnapshot(DataSnapshot dataSnapshot) {
                FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                if (friendlyMessage != null) {
                    friendlyMessage.setId(dataSnapshot.getKey());
                }
                return friendlyMessage;
            }
        };

        DatabaseReference messagesRef = mFirebaseDatabaseReference.child( getMessagesChild() );
        FirebaseRecyclerOptions<FriendlyMessage> options =
                new FirebaseRecyclerOptions.Builder<FriendlyMessage>()
                        .setQuery(messagesRef, parser)
                        .build();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>(options) {
            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(final MessageViewHolder viewHolder,
                                            int position,
                                            FriendlyMessage friendlyMessage) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (friendlyMessage.getText() != null) {
                    viewHolder.messageTextView.setText(friendlyMessage.getText());
                    viewHolder.messageTextView.setVisibility(TextView.VISIBLE);
                    viewHolder.messageImageView.setVisibility(ImageView.GONE);
                } else if (friendlyMessage.getImageUrl() != null) {
                    String imageUrl = friendlyMessage.getImageUrl();
                    if (imageUrl.startsWith("gs://")) {
                        StorageReference storageReference = FirebaseStorage.getInstance()
                                .getReferenceFromUrl(imageUrl);
                        storageReference.getDownloadUrl().addOnCompleteListener(
                                new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            String downloadUrl = task.getResult().toString();
                                            Glide.with(viewHolder.messageImageView.getContext())
                                                    .load(downloadUrl)
                                                    .into(viewHolder.messageImageView);
                                        } else {
                                            Log.w(TAG, "Getting download url was not successful.",
                                                    task.getException());
                                        }
                                    }
                                });
                    } else {
                        Glide.with(viewHolder.messageImageView.getContext())
                                .load(friendlyMessage.getImageUrl())
                                .into(viewHolder.messageImageView);
                    }
                    viewHolder.messageImageView.setVisibility(ImageView.VISIBLE);
                    viewHolder.messageTextView.setVisibility(TextView.GONE);
                }


                viewHolder.messengerTextView.setText(friendlyMessage.getName());
                if (friendlyMessage.getPhotoUrl() == null) {
                    viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,
                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(MainActivity.this)
                            .load(friendlyMessage.getPhotoUrl())
                            .into(viewHolder.messengerImageView);
                }

            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendlyMessage friendlyMessage = new
                        FriendlyMessage(mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl,
                        null /* no image */);
                mFirebaseDatabaseReference.child( getMessagesChild() )
                        .push().setValue(friendlyMessage);
                mMessageEditText.setText("");
            }
        });

        mAddMessageImageView = (ImageView) findViewById(R.id.addMessageImageView);
        mAddMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });



        //Call Ringing
        mCallRing = (RelativeLayout) findViewById(R.id.callRing);
        mIncomingCallTxt = (TextView) findViewById(R.id.incomingCallTxt);
        mCallAccept = (ImageView) findViewById(R.id.acceptBtn);
        mCallAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( !isPermissionGranted( videoPermissions ) && isVideoCall ){
                    askPermissions( videoPermissions );
                }
                else if( !isPermissionGranted( voicePermissions ) && !isVideoCall ){
                    askPermissions( voicePermissions );
                }
                mFirebaseDatabaseReference.child( getActionsChild() + "/action").setValue( ACCEPT_CALL );
            }
        });
        mCallReject = (ImageView) findViewById(R.id.rejectBtn);
        mCallReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFirebaseDatabaseReference.child( getActionsChild() + "/action").setValue( END_CALL );
            }
        });

        //Send Call Buttons
        mCallButtons = (RelativeLayout) findViewById(R.id.callButtons);
        mCallVoice = (ImageView) findViewById(R.id.callVoiceBtn);
        mCallVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCallRequest( VOICE_CALL );
            }
        });
        mCallVideo = (ImageView) findViewById(R.id.callVideoBtn);
        mCallVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCallRequest( VIDEO_CALL );
            }
        });

        //Voice Call Controls
        mCallVoiceControl = (RelativeLayout) findViewById(R.id.callVoiceControl);
        mToggleVoiceMic = (ImageView) findViewById(R.id.toggleVoiceMic);
        mToggleVoiceMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                micOn = !micOn;
                setMic( micOn );
                mToggleVoiceMic.setImageResource(
                        micOn ? R.drawable.ic_baseline_mic_24 : R.drawable.ic_baseline_mic_off_24 );
            }
        });
        mToggleVoiceSpk = (ImageView) findViewById(R.id.toggleVoiceSpk);
        mToggleVoiceSpk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMute = !isMute;
                setSpk( isMute );
                mToggleVoiceSpk.setImageResource(
                        isMute ? R.drawable.ic_baseline_volume_up_24 : R.drawable.ic_baseline_volume_off_24 );
            }
        });
        mEndCallVoice = (ImageView) findViewById(R.id.callVoiceEnd);
        mEndCallVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchFromVoiceControls();
                mFirebaseDatabaseReference.child( getActionsChild() + "/action").setValue( END_CALL );

            }
        });

        //Video Call Controls
        mCallVideoControl = (LinearLayout) findViewById(R.id.callVideoControl);
        mToggleVideoCam = (ImageView) findViewById(R.id.toggleVideoCamBtn);
        mToggleVideoCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camOn = !camOn;
                setCam( camOn );
                mToggleVideoCam.setImageResource(
                        camOn ? R.drawable.ic_baseline_videocam_24 : R.drawable.ic_baseline_videocam_off_24 );
            }
        });
        mToggleVideoMic = (ImageView) findViewById(R.id.toggleVideoMicBtn);
        mToggleVideoMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                micOn = !micOn;
                setMic( micOn );
                mToggleVideoMic.setImageResource(
                        micOn ? R.drawable.ic_baseline_mic_24 : R.drawable.ic_baseline_mic_off_24 );
            }
        });
        mToggleVideoSpk = (ImageView) findViewById(R.id.toggleVideoSpkBtn);
        mToggleVideoSpk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMute = !isMute;
                setSpk( isMute );
                mToggleVideoSpk.setImageResource(
                        isMute ? R.drawable.ic_baseline_volume_up_24 : R.drawable.ic_baseline_volume_off_24 );
            }
        });
        mEndCallVideo = (ImageView) findViewById(R.id.callVideoEnd);
        mEndCallVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchFromVideoControls();
                switchToMainActivity();
                mFirebaseDatabaseReference.child( getActionsChild() + "/action").setValue( END_CALL );

            }
        });

        setupWebView();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.
    }

    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }

    @Override
    public void onDestroy() {
        if( !webView.equals( null ) )
            webView.loadUrl("about:blank");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                mSignInClient.signOut();

                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    Log.d(TAG, "Uri: " + uri.toString());

                    FriendlyMessage tempMessage = new FriendlyMessage(null, mUsername, mPhotoUrl,
                            LOADING_IMAGE_URL);
                    mFirebaseDatabaseReference.child( getMessagesChild() ).push()
                            .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError,
                                                       DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        String key = databaseReference.getKey();
                                        StorageReference storageReference =
                                                FirebaseStorage.getInstance()
                                                        .getReference(mFirebaseUser.getUid())
                                                        .child(key)
                                                        .child(uri.getLastPathSegment());

                                        putImageInStorage(storageReference, uri, key);
                                    } else {
                                        Log.w(TAG, "Unable to write message to database.",
                                                databaseError.toException());
                                    }
                                }
                            });
                }
            }
        }
    }

    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key) {
        storageReference.putFile(uri).addOnCompleteListener(MainActivity.this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            task.getResult().getMetadata().getReference().getDownloadUrl()
                                    .addOnCompleteListener(MainActivity.this,
                                            new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    if (task.isSuccessful()) {
                                                        FriendlyMessage friendlyMessage =
                                                                new FriendlyMessage(null, mUsername, mPhotoUrl,
                                                                        task.getResult().toString());
                                                        mFirebaseDatabaseReference.child( getMessagesChild() ).child(key)
                                                                .setValue(friendlyMessage);
                                                    }
                                                }
                                            });
                        } else {
                            Log.w(TAG, "Image upload task was not successful.",
                                    task.getException());
                        }
                    }
                });
    }

    private void sendCallRequest( String type ){

        isVideoCall = type.equals( VIDEO_CALL );

        if( !isPermissionGranted( videoPermissions ) && isVideoCall ){
            askPermissions( videoPermissions );
        }
        else if( !isPermissionGranted( voicePermissions ) && !isVideoCall ){
            askPermissions( voicePermissions );
        }

        switchToSendingRing();

        mFirebaseDatabaseReference.child( getActionsChild() + "/incoming" ).setValue( mFirebaseUser.getDisplayName() );
        mFirebaseDatabaseReference.child( getActionsChild() + "/userID" ).setValue( mFirebaseUser.getUid() );
        mFirebaseDatabaseReference.child( getActionsChild() + "/callType" ).setValue( type );
        mFirebaseDatabaseReference.child( getActionsChild() + "/action" ).setValue( SEND_CALL );
        mFirebaseDatabaseReference.child( getActionsChild() + "/action" ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ( snapshot.getValue() != null && snapshot.getValue().toString().equals( ACCEPT_CALL ) ) {
                    listenForPickup();
                }
                else if ( snapshot.getValue() != null && snapshot.getValue().toString().equals( END_CALL ) ) {
                    switchFromSendingRing();
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }



    private void listenForPickup(){
        mFirebaseDatabaseReference.child( getActionsChild() ).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if( snapshot.child( "/action" ).getValue() != null &&
                        snapshot.child( "/userID" ).getValue() != null &&
                        snapshot.child( "/action" ).getValue().equals( ACCEPT_CALL ) ) {

                    callerUserID = snapshot.child( "/userID" ).getValue().toString();

                    switchFromSendingRing();

                    listenForHangup();
                    callJavascriptFunction("javascript:startCall(\"" + callerUserID + "\")" );

                    setMic(true);

                    if (isVideoCall) {
                        setCam(true);
                        switchFromMainActivity();
                        switchToVideoControls();
                    } else {
                        setCam(false);
                        switchToVoiceControls();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenForHangup(){
        mFirebaseDatabaseReference.child( getActionsChild() + "/action" ).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if( snapshot.getValue() != null && snapshot.getValue().equals( END_CALL ) )
                    endCall();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupWebView(){

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebChromeClient((WebChromeClient)(new WebChromeClient() {
            public void onPermissionRequest(@Nullable PermissionRequest request) {
                if (request != null) {
                    request.grant(request.getResources());
                }
            }
        }));
        webView.getSettings().setJavaScriptEnabled( true );
        webView.getSettings().setMediaPlaybackRequiresUserGesture( false );
        webView.addJavascriptInterface( new JSInterface(this), "Android");

        loadCall();

    }

    private void loadCall() {
        String filePath = "file:android_asset/call.html";
        webView.loadUrl(filePath);

        webView.setWebViewClient((WebViewClient)(new WebViewClient() {
            public void onPageFinished(@Nullable WebView view, @Nullable String url) {
                MainActivity.this.initializePeer();
            }
        }));
    }

    private void initializePeer() {
        callJavascriptFunction("javascript:init(\"" + mFirebaseUser.getUid() + "\")");
        mFirebaseDatabaseReference.child( getActionsChild() ).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if( snapshot.child( "/callType" ).getValue() != null &&
                        snapshot.child( "/incoming" ).getValue() != null &&
                        snapshot.child( "/userID" ).getValue() != null &&
                        ( snapshot.child( "/action" ).getValue().toString() ).equals( SEND_CALL )) {

                    isVideoCall = ( snapshot.child( "/callType" ).getValue().toString() ).equals( VIDEO_CALL );
                    callerID = snapshot.child( "/incoming" ).getValue().toString();
                    callerUserID = snapshot.child( "/userID" ).getValue().toString();
                    onCallRequest();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }

    private void onCallRequest(){
        switchToReceivingRing();
        mIncomingCallTxt.setText( (CharSequence) ( callerID + " is "+ ( isVideoCall ? "video" : "voice" ) + " calling...") );

        listenForHangup();

        mCallAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFirebaseDatabaseReference.child( getActionsChild() + "/action" ).setValue( ACCEPT_CALL );
                mFirebaseDatabaseReference.child( getActionsChild() + "/userID").setValue( mFirebaseUser.getUid() );
                mCallRing.setVisibility( View.GONE );

                setMic( true );
                switchFromReceivingRing();

                if( isVideoCall ) {
                    setCam( true );
                    switchFromMainActivity();
                    switchToVideoControls();
                }
                else {
                    setCam( false );
                    switchToVoiceControls();
                }

                listenForHangup();

            }
        });

        mCallReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFirebaseDatabaseReference.child( getActionsChild() + "/action" ).setValue( END_CALL );
                mCallRing.setVisibility( View.GONE );
                switchFromReceivingRing();
            }
        });
    }

    private void endCall(){

        if( isVideoCall ) switchFromVideoControls();
        else switchFromVoiceControls();
        switchToMainActivity();

        callerID = "";
        callerUserID="";

        webView.loadUrl("about:blank");
        loadCall();
        //callJavascriptFunction( "javascript:endCall()" );
    }

    private void switchToVideoControls(){
        webView.setVisibility( View.VISIBLE );
        mCallVideoControl.setVisibility( View.VISIBLE );
    }

    private void switchFromVideoControls(){
        webView.setVisibility( View.GONE );
        mCallVideoControl.setVisibility( View.GONE );
    }

    private void switchToVoiceControls(){

        mCallVoiceControl.setVisibility( View.VISIBLE );
    }

    private void switchFromVoiceControls(){

        mCallVoiceControl.setVisibility( View.GONE );
    }


    private void switchToMainActivity(){
        mMessageRecyclerView.setVisibility( View.VISIBLE );
        mMessageInput.setVisibility( View.VISIBLE );
        mCallButtons.setVisibility( View.VISIBLE );
    }

    private void switchFromMainActivity(){
        mMessageRecyclerView.setVisibility( View.GONE );
        mMessageInput.setVisibility( View.GONE );
        mCallButtons.setVisibility( View.GONE );
    }

    private void switchToSendingRing(){
        mCallAccept.setVisibility( View.GONE);
        mCallRing.setVisibility( View.VISIBLE );
        mCallButtons.setVisibility( View.GONE );
    }

    private void switchFromSendingRing(){
        mCallAccept.setVisibility( View.VISIBLE);
        mCallRing.setVisibility( View.GONE );
        mCallButtons.setVisibility( View.VISIBLE );
    }

    private void switchToReceivingRing(){
        mCallRing.setVisibility( View.VISIBLE );
        mCallButtons.setVisibility( View.GONE );
    }

    private void switchFromReceivingRing(){
        mCallRing.setVisibility( View.GONE );
        mCallButtons.setVisibility( View.VISIBLE );
    }




    private void callJavascriptFunction( final String functionString) {
        webView.post(new Runnable() {
            String s = functionString;
            @Override
            public void run() {
                webView.evaluateJavascript( s, null);
            }
        });

    }

    public void onPeerConnected() {
        isPeerConnected = true;
    }

    private String getMessagesChild(){
        return "/" + MESSAGES_CHILD + "/" + sessionID;
    }

    private String getActionsChild(){
        return "/" + ACTIONS_CHILD + "/" + sessionID;
    }


    private void setMic( boolean b ){
        callJavascriptFunction( "javascript:toggleAudio(\"" + b + "\")" );
    }

    private void setCam( boolean b ){
        callJavascriptFunction( "javascript:toggleVideo(\"" + b + "\")" );
    }

    private void setSpk( boolean b ){
        callJavascriptFunction( "javascript:mute(\"" + b + "\")" );
    }

    private void askPermissions( String[] permissions ){
        ActivityCompat.requestPermissions( this, permissions, requestCode );

    }
    private boolean isPermissionGranted( String[] permissions ){
        for( String permission: permissions){
            if( ActivityCompat.checkSelfPermission( this, permission ) != PackageManager.PERMISSION_GRANTED ){
                return false;
            }
        }

        return true;
    }

}
