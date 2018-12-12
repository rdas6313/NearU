package com.example.rdas6313.nearu.Fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.rdas6313.nearu.Interfaces.SignUpCallback;
import com.example.rdas6313.nearu.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegistrationFragment extends Fragment implements View.OnClickListener {

    private View root;
    private EditText nameView,phNumberEdittext;
    private Button signUpButton;
    private SignUpCallback mCallback;

    public RegistrationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_registration, container, false);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init(){
        mCallback = (SignUpCallback)getActivity();
        nameView = (EditText)root.findViewById(R.id.name);
        signUpButton = (Button)root.findViewById(R.id.signUp);
        phNumberEdittext = (EditText)root.findViewById(R.id.ph_number);
        signUpButton.setOnClickListener(this);
    }

    private boolean isNameOk(String name){
        String msg = null;
        if(name.isEmpty() || name.length() == 0){
            msg = "This field can't be empty";
        }
        if(msg != null){
            nameView.setError(msg);
            return false;
        }else
            return true;

    }


    @Override
    public void onClick(View v) {
        if(v.getId() != R.id.signUp)
            return;
        String name = nameView.getText().toString();
        String phNumber = phNumberEdittext.getText().toString();
        if(!isPhoneNumberOk(phNumber) || !isNameOk(name))
            return;

        if(mCallback != null)
            mCallback.OnClickSignUp("+91"+phNumber,name);
    }

    private boolean isPhoneNumberOk(String phone){
        String regix = "[0-9]+";
        String msg = null;
        if(phone.isEmpty() || phone.length() != 10){
            msg = "Enter a valid phone number";
        }else if(!phone.matches(regix)){
            msg = "only digits are allowed";
        }
        if(msg != null) {
            phNumberEdittext.setError(msg);
            return false;
        }else
            return true;
    }

    public void OnInvalidNumber(){
        if(phNumberEdittext != null)
            phNumberEdittext.setError("Enter a valid phone number");
    }
}
