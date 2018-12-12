package com.example.rdas6313.nearu.SignUp;

public interface SignUpCallback {
    void OnClickSignUp(String phoneNumber,String name);
    void onGetOtp(String otp);
    void sendSmsAgain();
}
