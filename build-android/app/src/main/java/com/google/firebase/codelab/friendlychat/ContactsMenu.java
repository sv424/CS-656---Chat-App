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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsMenu extends AppCompatActivity implements View.OnClickListener {

    private Session session;//global variable
    private Context cntx = null;

    private static final String TAG = "ContactsMenu";
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    private SharedPreferences mSharedPreferences;

    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private Button contact_1;
    private Button contact_2;

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
        setContentView(R.layout.contacts_menu);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        contact_1 = findViewById(R.id.contact_1);
        contact_1.setOnClickListener((View.OnClickListener) this);
        contact_2 = findViewById(R.id.contact_2);
        contact_2.setOnClickListener((View.OnClickListener) this);

        //session = new Session(cntx); //onclick create new session

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
