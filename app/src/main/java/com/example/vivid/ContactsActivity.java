package com.example.vivid;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ContactsActivity extends AppCompatActivity {
    BottomNavigationView navView;
    RecyclerView myContactList;
    ImageView findPepleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        findPepleBtn = findViewById(R.id.find_people_btn);
        myContactList = findViewById(R.id.contact_list);
        myContactList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        findPepleBtn.setOnClickListener(v -> {
            Intent findPeopleInent = new Intent(ContactsActivity.this, FindPeopleActivity.class);
            startActivity(findPeopleInent);
        });
    }

    protected void onStart(){
        super.onStart();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser==null){

            Intent logoutIntent = new Intent(ContactsActivity.this, RegistrationActivity.class);
            startActivity(logoutIntent);
            finish();
        }
    }


    public BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener;

    {
        navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    case R.id.navigation_settings:
                        Intent settingsIntent = new Intent(ContactsActivity.this, SettingsActivity.class);
                        startActivity(settingsIntent);
                        break;
                    case R.id.navigation_notifications:
                        Intent notificationIntent = new Intent(ContactsActivity.this, NotificationsActivity.class);
                        startActivity(notificationIntent);
                        break;
                    case R.id.navigation_logout:
                        FirebaseAuth.getInstance().signOut();
                        Intent logoutIntent = new Intent(ContactsActivity.this, RegistrationActivity.class);
                        startActivity(logoutIntent);
                        finish();
                        break;
                }
                return true;
            }

        };
    }
}