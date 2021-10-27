package com.example.vivid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class NotificationsActivity extends AppCompatActivity {

    RecyclerView notifications_list;
    private DatabaseReference userRef;
    private String currentID="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notifications_list = findViewById(R.id.notifications_list);
        notifications_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        currentID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("users");

    }
    public void onStart(){
        super.onStart();
        getFriendRequest();
    }
    void getFriendRequest(){

        FirebaseRecyclerOptions<Contacts> options=null;

        FirebaseRecyclerOptions.Builder<Contacts> contactsBuilder = new FirebaseRecyclerOptions.Builder<Contacts>();
        contactsBuilder.setQuery(userRef.child(currentID).child("requests").orderByChild("name"), Contacts.class);
        options= contactsBuilder.build();

        FirebaseRecyclerAdapter<Contacts, NotificationsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Contacts, NotificationsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull NotificationsViewHolder holder, final int position, @NonNull Contacts model) {
                holder.userNameTxt.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.profileImageView);

                holder.itemView.setOnClickListener(v -> {
                    String visit_user_id = getRef(position).getKey();
                    Intent intent = new Intent(NotificationsActivity.this,FriendRequestActivity.class);
                    intent.putExtra("visit_user_id",visit_user_id);
                    intent.putExtra("profile_image",model.getImage());
                    intent.putExtra("profile_name",model.getName());
                    intent.putExtra("profile_status",model.getStatus());
                    startActivity(intent);
                    finish();
                });
            }

            @NonNull
            @Override
            public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent
                        .getContext()).inflate(R.layout.find_friend_design,parent,false);
                NotificationsViewHolder viewHolder = new NotificationsViewHolder(view);
                return viewHolder;
            }
        };
        notifications_list.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    public static class NotificationsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userNameTxt;
        ImageView profileImageView;
        RelativeLayout cardView;

        public NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTxt = itemView.findViewById(R.id.name_notification);
            profileImageView = itemView.findViewById(R.id.image_notification);
            cardView = itemView.findViewById(R.id.card_view);

        }

    }


}