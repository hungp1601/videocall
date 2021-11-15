package com.example.vivid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
    private ImageView cancelCallBtn;
    private String receiverUserId="", receiverUserImage="", receiverUserName="";
    private String senderUserId="";
    private DatabaseReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        nameContact = findViewById(R.id.name_calling);
        profileImage = findViewById(R.id.profile_image_calling);
        cancelCallBtn = findViewById(R.id.cancel_call);


        cancelCallBtn.setOnClickListener(v -> cancelCallingUser());


        getAndSetReceiverProfileInfo();
        checkCallConnection();
    }

    @Override
    protected void onStart() {
        super.onStart();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(receiverUserId).hasChild("Calling")
                        && !dataSnapshot.child(receiverUserId).hasChild("Ringing")){

                    final HashMap<String, Object> callingInfo = new HashMap<>();
                    callingInfo.put("Calling",receiverUserId);

                    userRef.child(senderUserId).child("Calling").updateChildren(callingInfo).addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            final HashMap<String, Object>ringingInfo = new HashMap<>();
                            ringingInfo.put("Ringing",senderUserId);
                            userRef.child(receiverUserId).child("Ringing").updateChildren(ringingInfo);

                        }
                    });
                }
                else{
                    if(dataSnapshot.child(senderUserId).hasChild("Calling")){
                        userRef.child(senderUserId).child("Calling").removeValue();
                    }
                    Toast.makeText(CallingActivity.this,"This person is having a call",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CallingActivity.this, ContactsActivity.class);
                    startActivity(intent);

                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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


    private void checkCallConnection() {
;
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.child(receiverUserId).hasChild("Ringing")) {

                            userRef.child(senderUserId).child("Calling").removeValue().addOnCompleteListener(task -> {

                                    userRef.removeEventListener(this);
                                    Intent intent = new Intent(CallingActivity.this, ContactsActivity.class);
                                    finish();
                                    startActivity(intent);


                            });


                    }

                    if (dataSnapshot.child(receiverUserId).child("Ringing").hasChild("Picked")){
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
        }, 3000);


    }

    private void cancelCallingUser() {

        userRef.child(senderUserId).child("Calling").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("Calling")){
                    userRef.child(receiverUserId).child("Ringing").removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            userRef.child(senderUserId).child("Calling").removeValue().addOnCompleteListener(task12 -> {
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