package com.example.rdas6313.nearu.Interfaces;

public interface SignUpCallback {
    void OnClickSignUp(String phoneNumber,String name);
    void onGetOtp(String otp);
    void sendSmsAgain();
}
