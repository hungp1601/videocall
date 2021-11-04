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

public class FriendActivity extends AppCompatActivity {
    private String receiverUserID="",receiverUserImage="", receiverUserName="", receiverUserStatus="", currentID="",receiverToken="";
    private ImageView background_profile_view;
    private TextView name_profile,status_profile;
    private Button call, unfriend;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        receiverUserID= getIntent().getExtras().get("visit_user_id").toString();
        receiverUserImage= getIntent().getExtras().get("profile_image").toString();
        receiverUserName= getIntent().getExtras().get("profile_name").toString();
        receiverUserStatus= getIntent().getExtras().get("profile_status").toString();

        currentID= FirebaseAuth.getInstance().getCurrentUser().getUid();

        background_profile_view=findViewById(R.id.image_contact);
        name_profile= findViewById(R.id.name_contact);
        status_profile=findViewById(R.id.status_contact);
        call=findViewById(R.id.call_btn);
        unfriend=findViewById(R.id.unfriend_btn);


        userRef= FirebaseDatabase.getInstance().getReference().child("users");

        Picasso.get().load(receiverUserImage).into(background_profile_view);
        name_profile.setText(receiverUserName);
        status_profile.setText(receiverUserStatus);

        call.setOnClickListener(v->{
            Toast.makeText(FriendActivity.this,"Calling...",Toast.LENGTH_LONG).show();
        });
        unfriend.setOnClickListener(v->{
            userRef.child(currentID).child("friends").child(receiverUserID).removeValue();
            userRef.child(receiverUserID).child("friends").child(currentID).removeValue();
            Intent intent= new Intent(FriendActivity.this,ContactsActivity.class);
            Toast.makeText(FriendActivity.this,"You unfriend him/her",Toast.LENGTH_SHORT).show();

            startActivity(intent);
            finish();
        });
    }
}