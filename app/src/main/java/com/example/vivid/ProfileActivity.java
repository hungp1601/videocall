package com.example.vivid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID="",receiverUserImage="", receiverUserName="", receiverUserStatus="", currentID="";
    private ImageView background_profile_view;
    private  TextView name_profile,status_profile;
    private Button add_friend, decline_friend_request, call_friend;
    private String currentImage="", currentStatus="", currentUserName="";

    private DatabaseReference userRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        receiverUserID= getIntent().getExtras().get("visit_user_id").toString();

        currentID=FirebaseAuth.getInstance().getCurrentUser().getUid();

        background_profile_view=findViewById(R.id.background_profile_view);
        name_profile= findViewById(R.id.name_profile);
        status_profile=findViewById(R.id.status_profile);
        decline_friend_request=findViewById(R.id.decline_friend_request);
        add_friend=findViewById(R.id.add_friend);
        call_friend = findViewById(R.id.call_friend);

        userRef= FirebaseDatabase.getInstance().getReference().child("users");


        add_friend.setOnClickListener(view -> {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentID);
            profileMap.put("name", currentUserName);
            profileMap.put("status", currentStatus);
            profileMap.put("image", currentImage);
            userRef.child(receiverUserID).child("requests").child(currentID).setValue(profileMap);
            Toast.makeText(ProfileActivity.this,"Request was sent",Toast.LENGTH_SHORT).show();
        });

        decline_friend_request.setOnClickListener(view -> {
            userRef.child(receiverUserID).child("requests").child(currentID).removeValue();
            Toast.makeText(ProfileActivity.this,"Request was cancelled",Toast.LENGTH_SHORT).show();
        });

        call_friend.setOnClickListener(view -> {
            Intent intent= new Intent(ProfileActivity.this,CallingActivity.class);
            intent.putExtra("visit_user_id", receiverUserID);
            startActivity(intent);
            finish();
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(currentID.equals(receiverUserID)){
                    add_friend.setVisibility(View.GONE);
                    decline_friend_request.setVisibility(View.GONE);
                    call_friend.setVisibility(View.GONE);
                }
                else{

                    if (snapshot.child(receiverUserID).child("requests").hasChild(currentID)) {
                        add_friend.setVisibility(View.GONE);
                        decline_friend_request.setVisibility(View.VISIBLE);
                        call_friend.setVisibility(View.GONE);

                    } else if(snapshot.child(receiverUserID).child("friends").hasChild(currentID)){
                        add_friend.setVisibility(View.GONE);
                        decline_friend_request.setVisibility(View.GONE);
                        call_friend.setVisibility(View.VISIBLE);
                    }
                    else{
                        add_friend.setVisibility(View.VISIBLE);
                        decline_friend_request.setVisibility(View.GONE);
                        call_friend.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    receiverUserName = snapshot.child("name").getValue().toString();
                    receiverUserStatus = snapshot.child("status").getValue().toString();
                    receiverUserImage = snapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverUserImage).into(background_profile_view);
                    name_profile.setText(receiverUserName);
                    status_profile.setText(receiverUserStatus);
                }
                else{
                    Toast.makeText(ProfileActivity.this,"error",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        userRef.child(currentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserName = snapshot.child("name").getValue().toString();
                    currentStatus = snapshot.child("status").getValue().toString();
                    currentImage = snapshot.child("image").getValue().toString();
                }
                else{
                    Toast.makeText(ProfileActivity.this,"error",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}