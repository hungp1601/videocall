package com.example.vivid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ContactsActivity extends AppCompatActivity {
    BottomNavigationView navView;
    RecyclerView myContactList;
    ImageView findPepleBtn;

    private DatabaseReference contactsRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String userName="", profileImage="";
    private String calledBy="";
    String currentID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        findPepleBtn = findViewById(R.id.find_people_btn);
        myContactList = findViewById(R.id.contact_list);
        myContactList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        userRef= FirebaseDatabase.getInstance().getReference().child("users");
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        currentID= FirebaseAuth.getInstance().getCurrentUser().getUid();

        findPepleBtn.setOnClickListener(v -> {
            Intent findPeopleInent = new Intent(ContactsActivity.this, FindPeopleActivity.class);
            startActivity(findPeopleInent);
        });
    }

    protected void onStart(){
        super.onStart();
        checkForReceivingCall();
        validateUser();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser==null){

            Intent logoutIntent = new Intent(ContactsActivity.this, RegistrationActivity.class);
            startActivity(logoutIntent);
            finish();

        }
        getFriendList();
    }

    private void checkForReceivingCall() {
        userRef.child(currentID).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Ringing")){
                    calledBy = dataSnapshot.child("Ringing").getValue().toString();
                    Intent callingIntent = new Intent(ContactsActivity.this, CallingActivity.class);
                    callingIntent.putExtra("visit_user_id", calledBy);
                    startActivity(callingIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void validateUser() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("users").child(currentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    Intent settingIntent = new Intent(ContactsActivity.this, SettingsActivity.class);
                    startActivity(settingIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    void getFriendList(){
        FirebaseRecyclerOptions<Contacts> options=null;

        FirebaseRecyclerOptions.Builder<Contacts> contactsBuilder = new FirebaseRecyclerOptions.Builder<Contacts>();
        contactsBuilder.setQuery(userRef.child(currentID).child("friends").orderByChild("name"), Contacts.class);
        options = contactsBuilder.build();

        FirebaseRecyclerAdapter<Contacts, ContactsActivity.ContactViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsActivity.ContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactsActivity.ContactViewHolder holder, final int position, @NonNull Contacts model) {
                holder.userNameTxt.setText(model.getName());
                final String listUserId = getRef(position).getKey();
                Picasso.get().load(model.getImage()).into(holder.profileImageView);
                String visit_user_id = getRef(position).getKey();
                holder.itemView.setOnClickListener(v -> {

                    Intent intent = new Intent(ContactsActivity.this,FriendActivity.class);
                    intent.putExtra("visit_user_id",visit_user_id);
                    intent.putExtra("profile_image",model.getImage());
                    intent.putExtra("profile_name",model.getName());
                    intent.putExtra("profile_status",model.getStatus());
                    startActivity(intent);

                });
                holder.videCallBtn.setOnClickListener(v -> {


                    Intent intent= new Intent(ContactsActivity.this,CallingActivity.class);
                    intent.putExtra("visit_user_id", listUserId);
                    intent.putExtra("profile_image",model.getImage());
                    intent.putExtra("profile_name",model.getName());
                    intent.putExtra("profile_status",model.getStatus());
                    startActivity(intent);
                    finish();
                });
            }

            @NonNull
            @Override
            public ContactsActivity.ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent
                        .getContext()).inflate(R.layout.contact_design,parent,false);
                ContactsActivity.ContactViewHolder viewHolder = new ContactsActivity.ContactViewHolder(view);
                return viewHolder;
            }
        };
        myContactList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    public BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener;
    {
        navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    case R.id.navigation_settings:
                        Intent settingsIntent = new Intent(ContactsActivity.this, SettingsActivity.class);
                        startActivity(settingsIntent);

                        break;
                    case R.id.navigation_notifications:
                        Intent notificationIntent = new Intent(ContactsActivity.this, NotificationsActivity.class);
                        startActivity(notificationIntent);

                        break;
                    case R.id.navigation_logout:
                        FirebaseAuth.getInstance().signOut();
                        Intent logoutIntent = new Intent(ContactsActivity.this, RegistrationActivity.class);
                        startActivity(logoutIntent);

                        break;
                }
                return true;
            }
        };
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder
    {
        TextView userNameTxt;
        Button videCallBtn;
        ImageView profileImageView;
        RelativeLayout cardView;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTxt = itemView.findViewById(R.id.name_contact);
            videCallBtn = itemView.findViewById(R.id.call_btn);
            profileImageView = itemView.findViewById(R.id.image_contact);
            cardView = itemView.findViewById(R.id.card_view);
        }


    }

}