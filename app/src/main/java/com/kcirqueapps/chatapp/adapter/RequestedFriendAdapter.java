package com.kcirqueapps.chatapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.network.model.User;

import java.util.ArrayList;
import java.util.List;

public class RequestedFriendAdapter extends RecyclerView.Adapter<RequestedFriendAdapter.FriendHolder> {
    private List<User> userList = new ArrayList<>();
    private OnItemClickedListener onItemClickedListener;

    @NonNull
    @Override
    public FriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_requested, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendHolder holder, int position) {
        User user = userList.get(position);
        holder.bindTo(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    class FriendHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView profileImageView, rejectImageView, acceptImageView;
        final TextView nameTextView, isFriendTextView;

        public FriendHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            rejectImageView = itemView.findViewById(R.id.reject_image_view);
            acceptImageView = itemView.findViewById(R.id.accept_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            isFriendTextView = itemView.findViewById(R.id.connected_text_view);
            acceptImageView.setOnClickListener(this);
            rejectImageView.setOnClickListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickedListener != null)
                        onItemClickedListener.onItemClicked(userList.get(getAdapterPosition()));
                }
            });
        }

        void bindTo(final User user) {
            nameTextView.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
            Glide.with(itemView.getContext()).load(R.drawable.ic_user).into(profileImageView);
            isFriendTextView.setText("want to be your friend");
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.accept_image_view:
                    if (onItemClickedListener != null)
                        onItemClickedListener.onAcceptClicked(userList.get(getAdapterPosition()));
                    break;
                case R.id.reject_image_view:
                    if (onItemClickedListener != null)
                        onItemClickedListener.onRejectClicked(userList.get(getAdapterPosition()));
                    break;
            }
        }
    }

    public interface OnItemClickedListener {
        void onAcceptClicked(User user);

        void onRejectClicked(User user);

        void onItemClicked(User user);
    }
}