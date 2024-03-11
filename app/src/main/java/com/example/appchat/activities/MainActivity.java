package com.example.appchat.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.adapter.RecentConversationsAdapter;
import com.example.appchat.databinding.ActivityMainBinding;
import com.example.appchat.databinding.ActivitySignInBinding;
import com.example.appchat.listener.ConversionListener;
import com.example.appchat.models.ChatMessage;
import com.example.appchat.models.User;
import com.example.appchat.utilities.Constants;
import com.example.appchat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends BaseActivity implements ConversionListener {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        getToken();
        loadUserDentails();
        setListener();
        init();
        listenConversations();
    }

    private void init() {
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations,this);
        binding.conversationsRecyclerView.setAdapter(conversationsAdapter);
        firestore = FirebaseFirestore.getInstance();

    }

    private void listenConversations(){
        firestore.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        firestore.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    private void setListener() {
        binding.imagelogout.setOnClickListener(view -> signOut());
        binding.fabnewchat.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error!=null){
            return ;
        }
        if(value!=null){
            for(DocumentChange documentChange: value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    }else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0;i<conversations.size();i++){
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if(conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations,(obj1,obj2)->obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationsRecyclerView.smoothScrollToPosition(0);

        }
    };

    private void signOut() {
        showToast("Signing out...");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference reference = firestore.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        reference.update(updates).addOnSuccessListener(u -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Unable to sign out"));
    }

    private void loadUserDentails() {
        String imageData = preferenceManager.getString(Constants.KEY_IMAGE);
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.getDecoder().decode(imageData);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String Token) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference reference = firestore.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        reference.update(Constants.KEY_FCM_TOKEN, Token)
                .addOnSuccessListener(unused -> {
                    showToast("Token updated successfully");
                })
                .addOnFailureListener(e -> {
                    showToast("Unable to update token");
                });
    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}