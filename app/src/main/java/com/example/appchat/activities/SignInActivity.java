package com.example.appchat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.databinding.ActivitySignInBinding;
import com.example.appchat.utilities.Constants;
import com.example.appchat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.C;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager = new PreferenceManager(getApplicationContext());

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListener();
    }

    private void setListener(){
        binding.textSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignInActivity.this,SignUpActivity.class));
            }
        });
        binding.SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValidateSignInDentails()){
                    signIn();
                }
            }
        });
    }
    private void signIn(){
        FirebaseFirestore firestore  = FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()&& task.getResult() !=null && task.getResult().getDocuments().size()>0){
                       DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                       preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                       preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                       preferenceManager.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                       preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                       Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                       intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       startActivity(intent);
                   }else {
                       showToast("Unable to signin");
                   }
                });

    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private Boolean isValidateSignInDentails() {
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter validate email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        }
        return true;
    }
}