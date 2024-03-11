package com.example.appchat.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appchat.databinding.ItemContainerUserBinding;
import com.example.appchat.listener.UserListener;
import com.example.appchat.models.User;

import java.util.Base64;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{
    private final List<User> users;
    private final UserListener userListener;
    public UserAdapter(List<User> users,UserListener userListener){
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),parent,false
        );
        return new UserViewHolder(itemContainerUserBinding);

    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserdata(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        ItemContainerUserBinding binding;
        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding){
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }
        void setUserdata(User user){
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImages(user.image));
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userListener.onUserListener(user);
                }
            });
        }
    }

    private Bitmap getUserImages(String encodeImage){
        byte[] bytes = Base64.getDecoder().decode(encodeImage);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
