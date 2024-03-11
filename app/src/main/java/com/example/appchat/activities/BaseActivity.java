package com.example.appchat.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appchat.utilities.Constants;
import com.example.appchat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {

    private DocumentReference documentReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager  = new PreferenceManager(getApplicationContext());
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        documentReference = firestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Constants.KEY_AVAILABILITY,0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Constants.KEY_AVAILABILITY,1);
    }
}
