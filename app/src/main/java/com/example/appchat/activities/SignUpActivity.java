package com.example.appchat.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.databinding.ActivitySignInBinding;
import com.example.appchat.databinding.ActivitySignUpBinding;
import com.example.appchat.utilities.Constants;
import com.example.appchat.utilities.PreferenceManager;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.guieffect.qual.UI;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListener();
    }

    private void setListener() {
        binding.textSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            }
        });
        binding.SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidateSignUpDentails()) {
                    signUp();
                }
            }
        });
        binding.layoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }
        });
    }
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHight = bitmap.getHeight() * previewWidth/bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.PNG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
           new ActivityResultContracts.StartActivityForResult(),
            result ->{
               if (result.getResultCode() == RESULT_OK){
                   if(result.getData() != null){
                       Uri uri = result.getData().getData();
                       try {
                           InputStream inputStream = getContentResolver().openInputStream(uri);
                           Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                           binding.imageProfile.setImageBitmap(bitmap);
                           binding.textAddImage.setVisibility(View.GONE);
                           encodedImage = encodeImage(bitmap);
                       }catch (FileNotFoundException e){
                           e.printStackTrace();
                       }
                   }
               }
            }
    );
    private void signUp() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        HashMap<String, Object> users = new HashMap<>();
        users.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        users.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        users.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        users.put(Constants.KEY_IMAGE,encodedImage);
        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .add(users)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                })
                .addOnFailureListener(e -> {
                    showToast(e.getMessage());
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidateSignUpDentails() {
        if (encodedImage == null) {
            showToast("Select Profile image");
            return false;
        }
        else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Enter name");
            return false;
        }else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Enter name");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter validate email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter confirm password");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Password & ConfirmPassword must be same");
            return false;
        }
        return true;
    }
}