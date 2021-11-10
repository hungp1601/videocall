package com.example.vivid;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

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

public class ReceivingActivity extends AppCompatActivity {
    private TextView nameContact;
    private ImageView profileImage;
    private ImageView cancelCallBtn, acceptCallBtn;
    private String receiverUserId="", receiverUserImage="", receiverUserName="";
    private String senderUserId="";
    private DatabaseReference userRef;

    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving);
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mediaPlayer = MediaPlayer.create(this, R.raw.iphone);

        nameContact = findViewById(R.id.name_calling);
        profileImage = findViewById(R.id.profile_image_calling);
        cancelCallBtn = findViewById(R.id.cancel_call);
        acceptCallBtn = findViewById(R.id.make_call);

        mediaPlayer.start();

        cancelCallBtn.setOnClickListener(v -> {
            mediaPlayer.stop();
            cancelCallingUser();
        });

        acceptCallBtn.setOnClickListener(v -> {

            mediaPlayer.stop();

            final HashMap<String, Object> callingPickUpMap = new HashMap<>();
            callingPickUpMap.put("Picked", "Picked");
            userRef.child(senderUserId).child("Ringing")
                    .updateChildren(callingPickUpMap)
                    .addOnCompleteListener(task -> {
                        if (task.isComplete()){
                            Intent intent = new Intent(ReceivingActivity.this, VideoChatActivity.class);
                            intent.putExtra("visit_user_id", receiverUserId);
                            finish();
                            startActivity(intent);
                        }
                    });
        });

        getAndSetReceiverProfileInfo();

    }
    private void getAndSetReceiverProfileInfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(receiverUserId).exists()){
                    receiverUserImage = dataSnapshot.child(receiverUserId).child("image").getValue().toString();
                    receiverUserName = dataSnapshot.child(receiverUserId).child("name").getValue().toString();
                    nameContact.setText(receiverUserName);
                    Picasso.get().load(receiverUserImage).into(profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    protected void onStart(){
        super.onStart();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.child(receiverUserId).hasChild("Calling")) {
                    if(dataSnapshot.child(senderUserId).hasChild("Ringing")){
                        userRef.child(senderUserId).child("Ringing").removeValue().addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                userRef.removeEventListener(this);
                            }
                            mediaPlayer.stop();
                            Intent intent = new Intent(ReceivingActivity.this, ContactsActivity.class);
                            finish();
                            startActivity(intent);
                        });
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }










    private void cancelCallingUser() {
        // from receiver side
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(senderUserId).hasChild("Ringing")){
                    userRef.child(senderUserId).child("Ringing").removeValue();
                    if (dataSnapshot.child(receiverUserId).hasChild("Calling")){
                        userRef.child(receiverUserId).child("Calling").removeValue();
                    }

                    startActivity(new Intent(ReceivingActivity.this, ContactsActivity.class));
                    finish();
                }
                else {
                    startActivity(new Intent(ReceivingActivity.this, ContactsActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}