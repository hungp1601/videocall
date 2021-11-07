package com.example.vivid;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_KEY ="47371091";
    private static String SESSION_ID ="2_MX40NzM3MTA5MX5-MTYzNjAzNDM5NzcwNH5UQ2hGMlpBb2FScXZyT1p1UmNUV3VpVjN-fg";
    private static String TOKEN ="T1==cGFydG5lcl9pZD00NzM3MTA5MSZzaWc9ODYyOGNlMTg4MzdjODAzM2MxNzIwMzJiNTdiNGYyZTZkNmU4MzI2ZDpzZXNzaW9uX2lkPTJfTVg0ME56TTNNVEE1TVg1LU1UWXpOakF6TkRNNU56Y3dOSDVVUTJoR01scEJiMkZTY1haeVQxcDFVbU5VVjNWcFZqTi1mZyZjcmVhdGVfdGltZT0xNjM2MDM0NDMxJm5vbmNlPTAuNDQ4NjQ0MzEzNDkzMTg2MzQmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTYzODYyNjQyOCZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;
    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber msubscriber;


    private ImageView closeVideoChatBtn;
    private DatabaseReference userRef;
    private String userID="",receiverID;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverID =  getIntent().getExtras().get("visit_user_id").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        closeVideoChatBtn = findViewById(R.id.close_video_chat_btn);
        closeVideoChatBtn.setOnClickListener(v -> {
            mSession.disconnect();
            userRef.addValueEventListener(new ValueEventListener() {


                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(userID).hasChild("Ringing")) {
                        userRef.child(userID).child("Ringing").removeValue();

                        if (mPublisher != null) {
                            mPublisher.destroy();
                        }
                        if (msubscriber != null) {
                            msubscriber.destroy();
                        }
                        startActivity(new Intent(VideoChatActivity.this, ContactsActivity.class));
                        finish();
                    }
                    if (dataSnapshot.child(userID).hasChild("Calling")) {
                        userRef.child(userID).child("Calling").removeValue();

                        if (mPublisher != null) {
                            mPublisher.destroy();
                        }
                        if (msubscriber != null) {
                            msubscriber.destroy();
                        }

                        startActivity(new Intent(VideoChatActivity.this, ContactsActivity.class));
                        finish();
                    } else {
                        if (mPublisher != null) {
                            mPublisher.destroy();
                        }
                        if (msubscriber != null) {
                            msubscriber.destroy();
                        }
                        startActivity(new Intent(VideoChatActivity.this, ContactsActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        });

        requestPermissions();
    }

    public void onStart(){
        super.onStart();
        if(mSession==null){
            startActivity(new Intent(VideoChatActivity.this, ContactsActivity.class));
            finish();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoChatActivity.this);
    }
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions(){
        String [] perms = {Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};

        if (EasyPermissions.hasPermissions(this, perms)){

            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscriberViewController = findViewById(R.id.subscriber_container);


            //initialize and connect the session
            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);
        }
        else {
            EasyPermissions.requestPermissions(this, "Hey! this app needs Mic and Camera, Please allow.", RC_VIDEO_APP_PERM, perms);

        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {

        Log.i(LOG_TAG, "Session Connected");
        mPublisher =  new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);
        mPublisherViewController.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView){
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Stream Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

        Log.i(LOG_TAG,"Stream Received");
        if (msubscriber == null){
            msubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(msubscriber);
            mSubscriberViewController.addView(msubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");

        if (msubscriber != null){
            msubscriber = null;
            mSubscriberViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG, "Stream Error");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}