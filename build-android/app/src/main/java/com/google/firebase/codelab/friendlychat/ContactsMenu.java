package com.google.firebase.codelab.friendlychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsMenu extends AppCompatActivity implements View.OnClickListener {

    private Session session;//global variable
    private Context cntx = null;

    private static final String TAG = "ContactsMenu";
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    private SharedPreferences mSharedPreferences;

    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;


    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<FriendlyMessage, MainActivity.MessageViewHolder>
            mFirebaseAdapter;
    private GoogleSignInClient mSignInClient;
    public static final String FRIENDS_CHILD = "friends";

    private  ImageButton remove_contact1;
    private  ImageButton remove_contact2;
    private  ImageButton remove_contact3;
    private  ImageButton remove_contact4;

    private Button contact_1;
    private Button contact_2;
    private Button contact_3;
    private Button contact_4;

    ImageView messengerImageView1;
    ImageView messengerImageView2;
    ImageView messengerImageView3;
    ImageView messengerImageView4;

    DatabaseReference usersRef;

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.contact_1:
               // session.setusername("James");
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case R.id.contact_2:
               // session.setusername("Ann");
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case R.id.contact_3:
                // session.setusername("Ann");
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case R.id.contact_4:
                // session.setusername("Ann");
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            default:
                break;
        }

    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView messengerImageView;

        public ContactsViewHolder(View v) {
            super(v);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mSignInClient = GoogleSignIn.getClient(this, gso);

        setContentView(R.layout.contacts_menu);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        //Add friends list
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        usersRef = mFirebaseDatabaseReference.child("friends");


        Map<String, Friend> friendslist = new HashMap<>();
        friendslist.put("contact1", new Friend("1234", "James", UUID.randomUUID().toString()));
        friendslist.put("contact2", new Friend("2356", "Ann", UUID.randomUUID().toString()));
        friendslist.put("contact3", new Friend("5687", "Tom", UUID.randomUUID().toString()));
        friendslist.put("contact4", new Friend("7876", "Kate", UUID.randomUUID().toString()));

        usersRef.setValue(friendslist);

        //set onclick listener for contacts
        contact_1 = findViewById(R.id.contact_1);
        contact_1.setOnClickListener((View.OnClickListener) this);
        contact_2 = findViewById(R.id.contact_2);
        contact_2.setOnClickListener((View.OnClickListener) this);
        contact_3 = findViewById(R.id.contact_3);
        contact_3.setOnClickListener((View.OnClickListener) this);
        contact_4 = findViewById(R.id.contact_4);
        contact_4.setOnClickListener((View.OnClickListener) this);

        //set on click listener for remove
        remove_contact1 = findViewById(R.id.remove_contact_1);
        remove_contact1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messengerImageView1 = (ImageView) findViewById(R.id.messengerImageView1);
                messengerImageView1.setVisibility(View.INVISIBLE);
                contact_1.setVisibility(View.INVISIBLE);
                usersRef.child("contact1").removeValue();
                remove_contact1.setVisibility(View.INVISIBLE);
            }
        });

        remove_contact2 = findViewById(R.id.remove_contact_2);
        remove_contact2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                messengerImageView2 = (ImageView) findViewById(R.id.messengerImageView2);
                messengerImageView2.setVisibility((View.INVISIBLE));
                contact_2.setVisibility(View.INVISIBLE);
                usersRef.child("contact2").removeValue();
                remove_contact2.setVisibility(View.INVISIBLE);
            }
        });

        remove_contact3 = findViewById(R.id.remove_contact_3);
        remove_contact3.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                messengerImageView3 = (ImageView) findViewById(R.id.messengerImageView3);
                messengerImageView3.setVisibility((View.INVISIBLE));
                contact_3.setVisibility(View.INVISIBLE);
                usersRef.child("contact3").removeValue();
                remove_contact3.setVisibility(View.INVISIBLE);
            }
        });

        remove_contact4 = findViewById(R.id.remove_contact_4);
        remove_contact4.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                messengerImageView4 = (ImageView) findViewById(R.id.messengerImageView4);
                messengerImageView4.setVisibility((View.INVISIBLE));
                contact_4.setVisibility(View.INVISIBLE);
                usersRef.child("contact4").removeValue();
                remove_contact4.setVisibility(View.INVISIBLE);
            }
        });

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
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
