package com.example.appchat.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appchat.databinding.ItemContainerRecentConversionBinding;
import com.example.appchat.listener.ConversionListener;
import com.example.appchat.models.ChatMessage;
import com.example.appchat.models.User;

import java.util.Base64;
import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder>{
    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;
    public RecentConversationsAdapter(List<ChatMessage> chatMessages,ConversionListener conversionListener){
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(ItemContainerRecentConversionBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentConversionBinding binding;
        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding){
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    User user = new User();
                    user.id = chatMessage.conversionId;
                    user.name = chatMessage.conversionName;
                    user.image = chatMessage.conversionImage;
                    conversionListener.onConversionClicked(user);
                }
            });
        }
    }
    private Bitmap getConversionImage(String encodedImage){
        byte[] bytes = Base64.getDecoder().decode(encodedImage);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
