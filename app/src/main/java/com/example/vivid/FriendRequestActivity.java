package com.example.vivid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class FriendRequestActivity extends AppCompatActivity {

    private String receiverUserID="",receiverUserImage="", receiverUserName="",receiverUserStatus="", currentID="";
    private ImageView background_profile_view;
    private TextView name_profile;
    private Button accept, decline;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        receiverUserImage = getIntent().getExtras().get("profile_image").toString();
        receiverUserName = getIntent().getExtras().get("profile_name").toString();
        receiverUserStatus = getIntent().getExtras().get("profile_status").toString();

        background_profile_view=findViewById(R.id.image_notification);
        name_profile= findViewById(R.id.name_notification);
        decline=findViewById(R.id.request_decline_btn);
        accept=findViewById(R.id.request_accept_btn);

        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        currentID= FirebaseAuth.getInstance().getCurrentUser().getUid();

        Picasso.get().load(receiverUserImage).into(background_profile_view);
        name_profile.setText(receiverUserName);

        accept.setOnClickListener(view -> {

            Map<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentID);
            profileMap.put("name", userRef.child(currentID).child("name").toString());
            profileMap.put("status", userRef.child(currentID).child("status").toString());
            profileMap.put("image", userRef.child(currentID).child("image").toString());

            Map<String, Object> profileReceiver = new HashMap<>();
            profileReceiver.put("uid", receiverUserID);
            profileReceiver.put("name", userRef.child(receiverUserID).child("name").toString());
            profileReceiver.put("status", userRef.child(receiverUserID).child("status").toString());
            profileReceiver.put("image", userRef.child(receiverUserID).child("image").toString());


            userRef.child(currentID).child("friends").child(receiverUserID).setValue(profileReceiver);
            userRef.child(receiverUserID).child("friends").child(currentID).setValue(profileMap);

            userRef.child(currentID).child("requests").child(receiverUserID).removeValue();
            userRef.child(receiverUserID).child("requests").child(currentID).removeValue();

            Toast.makeText(FriendRequestActivity.this,receiverUserID,Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(FriendRequestActivity.this,NotificationsActivity.class);
            startActivity(intent);
            finish();
        });

        decline.setOnClickListener(view -> {
            userRef.child(currentID).child("requests").child(receiverUserID).removeValue();
            userRef.child(receiverUserID).child("requests").child(currentID).removeValue();
            Toast.makeText(FriendRequestActivity.this,"Request was cancelled",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(FriendRequestActivity.this,NotificationsActivity.class);
            startActivity(intent);
            finish();
        });




    }
}