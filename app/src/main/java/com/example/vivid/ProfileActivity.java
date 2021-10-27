package com.example.vivid;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID="",receiverUserImage="", receiverUserName="", receiverUserStatus="";
    private ImageView background_profile_view;
    private  TextView name_profile,status_profile;
    private Button add_friend, decline_friend_request, call_friend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        receiverUserID= getIntent().getExtras().get("visit_user_id").toString();
        receiverUserImage= getIntent().getExtras().get("profile_image").toString();
        receiverUserName= getIntent().getExtras().get("profile_name").toString();
        receiverUserStatus= getIntent().getExtras().get("profile_status").toString();

        background_profile_view=findViewById(R.id.background_profile_view);
        name_profile= findViewById(R.id.name_profile);
        decline_friend_request=findViewById(R.id.decline_friend_request);
        status_profile=findViewById(R.id.status_profile);
        add_friend=findViewById(R.id.add_friend);
        call_friend = findViewById(R.id.call_friend);

        Picasso.get().load(receiverUserImage).into(background_profile_view);
        name_profile.setText(receiverUserName);
        status_profile.setText(receiverUserStatus);


    }
}