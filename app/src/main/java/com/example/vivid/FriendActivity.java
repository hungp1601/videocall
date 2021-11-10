package com.example.vivid;

import android.content.Intent;
import android.os.Bundle;
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
        currentID= FirebaseAuth.getInstance().getCurrentUser().getUid();

        background_profile_view=findViewById(R.id.image_contact);
        name_profile= findViewById(R.id.name_contact);
        status_profile=findViewById(R.id.status_contact);
        call=findViewById(R.id.call_btn);
        unfriend=findViewById(R.id.unfriend_btn);

        userRef= FirebaseDatabase.getInstance().getReference().child("users");

        call.setOnClickListener(v->{
            Intent intent= new Intent(FriendActivity.this,CallingActivity.class);
            intent.putExtra("visit_user_id", receiverUserID);
            startActivity(intent);
            finish();
        });
        unfriend.setOnClickListener(v->{
            userRef.child(currentID).child("friends").child(receiverUserID).removeValue();
            userRef.child(receiverUserID).child("friends").child(currentID).removeValue();
            Intent intent= new Intent(FriendActivity.this,ContactsActivity.class);
            Toast.makeText(FriendActivity.this,"You unfriend him/her",Toast.LENGTH_SHORT).show();

            startActivity(intent);
            finish();
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
                    Toast.makeText(FriendActivity.this,"error",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FriendActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}