package com.example.rdas6313.nearu;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.rdas6313.nearu.Fragment.OtpVerificationFragment;
import com.example.rdas6313.nearu.Fragment.RegistrationFragment;
import com.example.rdas6313.nearu.Interfaces.SignUpCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.concurrent.TimeUnit;

public class SignUpActivity extends AppCompatActivity implements SignUpCallback {

    private static final String TAG = SignUpActivity.class.getSimpleName();

    private final long TIME_OUT_LENGTH = 60;


    private FragmentManager fragmentManager;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private RegistrationFragment registrationFragment;
    private OtpVerificationFragment otpVerificationFragment;

    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private String PhoneNumber,userName;

    private boolean isCodeSent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        init();
    }

    private void init(){
        registrationFragment = new RegistrationFragment();
        otpVerificationFragment = new OtpVerificationFragment();
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.signup_fragment_container,registrationFragment)
                .commit();

        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                //time out
                Log.d(TAG,"timeout:- "+s);
                isCodeSent = false;
                onTimeOut();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                //s is verification Id
                changeVerificationIdAndToken(s,forceResendingToken);
                Log.d(TAG,"onCodecent "+s);
                isCodeSent = true;
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG,"onVerification: "+phoneAuthCredential.getSmsCode());
                //Todo:- do something when verification completed and change Verification token and id to null
                isCodeSent = false;
                if(otpVerificationFragment != null)
                    otpVerificationFragment.onAuToVerification(phoneAuthCredential.getSmsCode());
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                isCodeSent = false;
                Log.e(TAG,"onVerificationFailed:- "+e.getMessage());
                if(e instanceof FirebaseAuthInvalidCredentialsException){
                    //invalid request
                    if(registrationFragment != null)
                        registrationFragment.OnInvalidNumber();

                }else{
                    Toast.makeText(SignUpActivity.this,"Internal Error",Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    public void OnClickSignUp(String phoneNumber,String name) {
        Log.d(TAG,"number "+phoneNumber);
        //Todo:- check internet connection first
        PhoneNumber = phoneNumber;
        userName = name;
        sendVerificationCode(phoneNumber,null);
    }

    @Override
    public void onGetOtp(String otp) {
        Log.d(TAG,"OTP "+otp);
        createCredentials(otp);
    }

    public void onTimeOut() {
        Log.d(TAG,"TimeOut happened");
        if(otpVerificationFragment != null)
            otpVerificationFragment.resendSms();
    }

    @Override
    public void sendSmsAgain() {
        sendVerificationCode(PhoneNumber,resendingToken);
    }

    private void createCredentials(String otp){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        Log.d(TAG,"createCredentials "+credential);
        signInWithCreadentials(credential);
    }

    private void signInWithCreadentials(PhoneAuthCredential phoneAuthCredential){
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            saveUserName();
                        }else{
                            Log.d(TAG,task.getException().getMessage());
                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                if(otpVerificationFragment != null)
                                    otpVerificationFragment.onInvalidCode();
                            }
                        }
                    }
                });
    }

    private void open(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendVerificationCode(String phoneNumber,PhoneAuthProvider.ForceResendingToken token){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                TIME_OUT_LENGTH,
                TimeUnit.SECONDS,
                this,
                mCallback,
                token
        );
        FirebaseAuth.getInstance().useAppLanguage();
        changeUiToOtpVerification();
    }

    private void saveUserName(){
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName).build();
        FirebaseAuth.getInstance().getCurrentUser()
                .updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            open();
                        }else{
                            //Todo:- show error here
                            Toast.makeText(SignUpActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT)
                                    .show();
                            Log.e(TAG,task.getException().getMessage());
                        }
                    }
                });
    }

    private void changeVerificationIdAndToken(String id,PhoneAuthProvider.ForceResendingToken token){
        verificationId = id;
        resendingToken = token;
    }


    private void changeUiToOtpVerification(){
        //Todo:- open otp verification Fragment and remove registration Fragment
        fragmentManager.beginTransaction().replace(R.id.signup_fragment_container,otpVerificationFragment)
                .commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            open();
        }
    }
}
