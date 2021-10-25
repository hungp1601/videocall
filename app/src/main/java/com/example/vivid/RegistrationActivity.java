package com.example.vivid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegistrationActivity extends AppCompatActivity {
    private CountryCodePicker ccp;
    private EditText phoneText,codeText;
    private Button continueAndNextBtn;
    private String checker = "", phoneNumber="";
    private RelativeLayout relativeLayout;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth=FirebaseAuth.getInstance();
        loadingBar= new ProgressDialog(this);
        GetViews();
        continueAndNextBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(continueAndNextBtn.getText().equals("Submit") || checker.equals("Code sent")){
                    String verificationCode=codeText.getText().toString();
                    if(verificationCode.equals("")){
                        Toast.makeText(RegistrationActivity.this, "Please write verification code", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        loadingBar.setTitle("Code Verification");
                        loadingBar.setTitle("Code Verifying...");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();
                        PhoneAuthCredential credential= PhoneAuthProvider.getCredential(mVerificationId,verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                }
                else{
                    phoneNumber = ccp.getFullNumberWithPlus();
                    if(!phoneNumber.equals("")){
                        loadingBar.setTitle("Phone number verification");
                        loadingBar.setMessage("Please wait for the verification");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,60, TimeUnit.SECONDS,RegistrationActivity.this,mCallbacks);
                    }
                    else{
                        Toast.makeText(RegistrationActivity.this, "Please write a valid phone number", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(RegistrationActivity.this,"Invalid Phone Number...",Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();

                relativeLayout.setVisibility(View.VISIBLE);
                continueAndNextBtn.setText("Continue");
                codeText.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                relativeLayout.setVisibility(View.GONE);

                mVerificationId = s;
                mResendToken=forceResendingToken;

                checker = "Code sent";
                continueAndNextBtn.setText("Submit");
                codeText.setVisibility(View.VISIBLE);
                loadingBar.dismiss();
                Toast.makeText(RegistrationActivity.this,"Code has been sent, Check your message.",Toast.LENGTH_SHORT).show();
            }
        };
    }
    private void GetViews(){
        phoneText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        continueAndNextBtn = findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);
    }
    protected void onStart(){
        super.onStart();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            sendUserToMainActivity();
        }
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(RegistrationActivity.this,"Sign In Successful",Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        } else {
                            // Sign in failed, display a message and update the UI
                            loadingBar.dismiss();
                            String e= task.getException().toString();
                            Toast.makeText(RegistrationActivity.this,"Error: "+e,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void sendUserToMainActivity(){
        Intent intent = new Intent(RegistrationActivity.this,ContactsActivity.class);
        startActivity(intent);
        finish();
    }
}