package com.example.appchat.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appchat.databinding.ItemContainerReceivedMessageBinding;
import com.example.appchat.databinding.ItemContainerSentMessageBinding;
import com.example.appchat.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessage;
    private final Bitmap receiverProfileImage;
    private final String senderId;


    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;


    public ChatAdapter(List<ChatMessage> chatMessage, Bitmap receiverProfileImage, String senderId) {
        this.chatMessage = chatMessage;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SendMessageViewHolder(ItemContainerSentMessageBinding.
                    inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            return new ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding.
                    inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SendMessageViewHolder) holder).setData(chatMessage.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessage.get(position), receiverProfileImage);
        }
    }


    @Override
    public int getItemCount() {
        return chatMessage.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessage.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SendMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;

        SendMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            binding.imageProfile.setImageBitmap(receiverProfileImage);
        }
    }
}
