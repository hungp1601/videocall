package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    Button saveBtn;
    EditText userNameET, userBioET;
    ImageView profileImageView;

    private static final int GalleryPick = 1;
    private Uri ImageUri;

    private StorageReference userProfileImgRef;
    private String downloadUrl;
    private DatabaseReference userRef;

    private ProgressDialog progressDialog;
    private DatabaseReference mDatabase;

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        saveBtn = findViewById(R.id.save_settings_btn);
        userNameET = findViewById(R.id.username_settings);
        userBioET = findViewById(R.id.bio_settings);
        profileImageView = findViewById(R.id.settings_profile_image);

        userProfileImgRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        progressDialog = new ProgressDialog(this);

        //mDatabase = FirebaseDatabase.getInstance().getReference();

        profileImageView.setOnClickListener(view -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GalleryPick);
        });
        saveBtn.setOnClickListener((v) -> saveUserData());
        retrieveUserInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            ImageUri = data.getData();
            profileImageView.setImageURI(ImageUri);
        }
    }

    private void saveUserData() {
        final String getUserName = userNameET.getText().toString();
        final String getUserStatus = userBioET.getText().toString();
        if (ImageUri == null) {
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("image")) {
                        saveInfoOnlyWithoutImage();
                    } else {
                        Toast.makeText(SettingsActivity.this, "Please select image", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SettingsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (getUserName.equals("")) {
            Toast.makeText(SettingsActivity.this, "username is empty.", Toast.LENGTH_SHORT).show();
        } else if (getUserStatus.equals("")) {
            Toast.makeText(SettingsActivity.this, "bio is empty.", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setTitle("Account Settings");
            progressDialog.setMessage("Please wait...");
            //progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            final StorageReference filePath =
                    userProfileImgRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            final UploadTask uploadTask = filePath.putFile(ImageUri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                downloadUrl = filePath.getDownloadUrl().toString();
                return filePath.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    downloadUrl = task.getResult().toString();
                    HashMap<String, Object> profileMap = new HashMap<>();
                    profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    profileMap.put("name", getUserName);
                    profileMap.put("status", getUserStatus);
                    profileMap.put("image", downloadUrl);

                    userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .updateChildren(profileMap).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {

                                    Intent intent = new Intent(SettingsActivity.this, ContactsActivity.class);
                                    startActivity(intent);
                                    finish();
                                    Toast.makeText(SettingsActivity.this,
                                            "Profile has been updated", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }
    }

    private void saveInfoOnlyWithoutImage() {
        final String getUserName = userNameET.getText().toString();
        final String getUserStatus = userBioET.getText().toString();

        HashMap<String, Object> profileMap = new HashMap<>();
        profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        profileMap.put("name", getUserName);
        profileMap.put("status", getUserStatus);


        if (getUserName.equals("")) {
            Toast.makeText(SettingsActivity.this, "username is empty.", Toast.LENGTH_SHORT).show();
        } else if (getUserStatus.equals("")) {
            Toast.makeText(SettingsActivity.this, "bio is empty.", Toast.LENGTH_SHORT).show();
        } else {
            userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .updateChildren(profileMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(SettingsActivity.this, ContactsActivity.class);
                    startActivity(intent);
                    finish();
                    progressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this,
                            "Profile has been updated", Toast.LENGTH_SHORT).show();
                }

            });
        }
    }

    private void retrieveUserInfo() {
        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String imageDB = snapshot.child("image").getValue().toString();
                            String nameDB = snapshot.child("name").getValue().toString();
                            String bioDB = snapshot.child("status").getValue().toString();

                            userNameET.setText(nameDB);
                            userBioET.setText(bioDB);
                            Picasso.get().load(imageDB).placeholder(R.drawable.profile_image).into(profileImageView);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SettingsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}