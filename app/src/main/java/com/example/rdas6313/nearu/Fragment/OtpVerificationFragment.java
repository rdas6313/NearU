package com.example.rdas6313.nearu.Fragment;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rdas6313.nearu.Interfaces.SignUpCallback;
import com.example.rdas6313.nearu.R;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;

/**
 * A simple {@link Fragment} subclass.
 */
public class OtpVerificationFragment extends Fragment implements View.OnClickListener {

    private Button submitButton;
    private OtpView otpView;

    private boolean shouldresendSms = false;

    private final long INTERVAL = 1000;

    SignUpCallback mCallback;

    public OtpVerificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_otp_verification, container, false);
        submitButton = (Button)root.findViewById(R.id.submit);
        otpView = (OtpView)root.findViewById(R.id.otp_view);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        submitButton.setOnClickListener(this);

        mCallback = (SignUpCallback) getActivity();
    }

    private void onHaveOtp(String otp){
        if(mCallback == null)
            return;
        mCallback.onGetOtp(otp);
    }

    private void changeSubmitButtonType(boolean isSubmit){
        if(isSubmit){
            submitButton.setText("Submit");
            shouldresendSms = false;
        }else{
            submitButton.setText("Resend Sms");
            shouldresendSms = true;
        }
    }

    public void resendSms(){
       changeSubmitButtonType(false);
    }

    public void onAuToVerification(String otp){
        if(otpView != null)
            otpView.setText(otp);
        onClick(otpView);
    }

    private boolean isEverythingOk(String code){
        String regx = "[0-9]+";
        if(code.isEmpty() || code.length() < 6 || !code.matches(regx))
            return false;
        return true;
    }

    public void onInvalidCode(){
        Toast.makeText(getContext(),"InValid Otp",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.submit){
            if(shouldresendSms){
                if(mCallback != null)
                    mCallback.sendSmsAgain();
                changeSubmitButtonType(true);
            }else{
                String code = otpView.getText().toString();
                if(isEverythingOk(code))
                    onHaveOtp(code);
                else
                    otpView.setError("Enter valid otp");
            }
        }
    }
}
