package com.example.vivid;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
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

public class CallingActivity extends AppCompatActivity {

    private TextView nameContact;
    private ImageView profileImage;
    private ImageView cancelCallBtn, acceptCallBtn;
    private String receiverUserId="", receiverUserImage="", receiverUserName="";
    private String senderUserId="", senderUserImage="", senderUserName="", checker="";
    private String callingID="", ringingID="";
    private DatabaseReference userRef;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mediaPlayer = MediaPlayer.create(this, R.raw.iphone);

        nameContact = findViewById(R.id.name_calling);
        profileImage = findViewById(R.id.profile_image_calling);
        cancelCallBtn = findViewById(R.id.cancel_call);
        acceptCallBtn = findViewById(R.id.make_call);



        cancelCallBtn.setOnClickListener(v -> {
            mediaPlayer.stop();
            checker = "Clicked";
            cancelCallingUser();
        });

        acceptCallBtn.setOnClickListener(v -> {

            mediaPlayer.stop();

            final HashMap<String, Object>callingPickUpMap = new HashMap<>();
            callingPickUpMap.put("Picked", "Picked");
            userRef.child(senderUserId).child("Ringing")
                    .updateChildren(callingPickUpMap)
                    .addOnCompleteListener(task -> {
                        if (task.isComplete()){
                            Intent intent = new Intent(CallingActivity.this, VideoChatActivity.class);
                            intent.putExtra("visit_user_id", receiverUserId);
                            startActivity(intent);
                            finish();
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
                if (dataSnapshot.child(senderUserId).exists()){
                    senderUserImage = dataSnapshot.child(senderUserId).child("image").getValue().toString();
                    senderUserImage = dataSnapshot.child(senderUserId).child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer.start();
        userRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!checker.equals("Clicked") && !dataSnapshot.hasChild("Calling") && !dataSnapshot.hasChild("Ringing")){

                    final HashMap<String, Object> callingInfo = new HashMap<>();
                    callingInfo.put("Calling",receiverUserId);

                    userRef.child(senderUserId).child("Calling").updateChildren(callingInfo).addOnCompleteListener(task -> {

                        if (task.isSuccessful()){
                            final HashMap<String, Object>ringingInfo = new HashMap<>();
                            ringingInfo.put("Ringing",senderUserId);
                            //Toast.makeText(CallingActivity.this,"call", Toast.LENGTH_SHORT).show();
                            userRef.child(receiverUserId).child("Ringing").setValue(ringingInfo);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(senderUserId).hasChild("Ringing") && !dataSnapshot.child(senderUserId).hasChild("Calling")){
                    acceptCallBtn.setVisibility(View.VISIBLE);
                }

                if (dataSnapshot.child(receiverUserId).child("Ringing").hasChild("Picked")){
                    mediaPlayer.stop();
                    Intent intent = new Intent(CallingActivity.this, VideoChatActivity.class);
                    intent.putExtra("visit_user_id", receiverUserId);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void cancelCallingUser() {

        userRef.child(senderUserId).child("Calling").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("Calling")){
                    callingID = dataSnapshot.child("Calling").getValue().toString();

                    userRef.child(callingID).child("Ringing").removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            userRef.child(senderUserId).child("Calling").removeValue().addOnCompleteListener(task12 -> {
                                startActivity(new Intent(CallingActivity.this, ContactsActivity.class));
                                finish();
                            });
                        }
                        else Toast.makeText(CallingActivity.this,"call", Toast.LENGTH_SHORT).show();
                    });
                }
                else {
                    startActivity(new Intent(CallingActivity.this, ContactsActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // from receiver side
        userRef.child(senderUserId).child("Ringing").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("Ringing")){

                    ringingID = dataSnapshot.child("Ringing").getValue().toString();

                    userRef.child(ringingID).child("Calling").removeValue().addOnCompleteListener(task -> {

                        if (task.isSuccessful()){
                            userRef.child(senderUserId).child("Ringing").removeValue().addOnCompleteListener(task1 -> {
                                startActivity(new Intent(CallingActivity.this, ContactsActivity.class));
                                finish();
                            });
                        }
                    });
                }
                else {
                    startActivity(new Intent(CallingActivity.this, ContactsActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}