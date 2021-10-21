package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView navView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

    }



    public BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener;

    {
        navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_home:
                        Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        break;
                    case R.id.navigation_settings:
                        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(settingsIntent);
                        break;
                    case R.id.navigation_notifications:
                        Intent notificationIntent = new Intent(MainActivity.this, NotificationsActivity.class);
                        startActivity(notificationIntent);
                        break;
                    case R.id.navigation_logout:
                        FirebaseAuth.getInstance().signOut();
                        Intent logoutIntent = new Intent(MainActivity.this, RegistrationActivity.class);
                        startActivity(logoutIntent);
                        finish();
                        break;
                }
                return true;
            }

        };
    }
}