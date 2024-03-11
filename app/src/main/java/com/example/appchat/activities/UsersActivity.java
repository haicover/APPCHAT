package com.example.appchat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.appchat.adapter.UserAdapter;
import com.example.appchat.databinding.ActivityUsersBinding;
import com.example.appchat.listener.UserListener;
import com.example.appchat.models.User;
import com.example.appchat.utilities.Constants;
import com.example.appchat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {
    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();

    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

    }

    private void getUsers(){
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            if(currentUserId.equals(documentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = documentSnapshot.getString(Constants.KEY_NAME);
                            user.email = documentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = documentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = documentSnapshot.getId();
                            users.add(user);
                        }if(users.size()>0){
                            UserAdapter userAdapter = new UserAdapter(users,this);
                            binding.userRecyclerView.setAdapter(userAdapter);
                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                        }else {
                            showErrorMessage();
                        }
                    }else {
                        showErrorMessage();
                    }
                });
    }
    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
    }


    @Override
    public void onUserListener(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}