package com.kcirqueapps.chatapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.User;

import net.igenius.customcheckbox.CustomCheckBox;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddFriendGroupAdapter extends RecyclerView.Adapter<AddFriendGroupAdapter.FriendHolder> {
    private List<User> userList = new ArrayList<>();
    private OnItemClickedListener onItemClickedListener;

    @NonNull
    @Override
    public FriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_add_group_friend, parent, false));
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

    class FriendHolder extends RecyclerView.ViewHolder {
        final CircleImageView profileImageView;
        final TextView nameTextView;
        final CustomCheckBox checkBox;
        final LinearLayout rootView;

        FriendHolder(@NonNull final View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            checkBox = itemView.findViewById(R.id.checkbox);
            rootView = itemView.findViewById(R.id.root_view);

            checkBox.setOnCheckedChangeListener(new CustomCheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
                    if (isChecked) {
                        itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.itemSelectedColor));
                    } else {
                        itemView.setBackgroundColor(Color.WHITE);
                    }
                    if (onItemClickedListener != null)
                        onItemClickedListener.onCheckedChanged(userList.get(getAdapterPosition()), checkBox, isChecked);
                }
            });
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBox.isChecked()) {
                        checkBox.setChecked(false, true);
                    } else {
                        checkBox.setChecked(true, true);
                    }
                    if (onItemClickedListener != null)
                        onItemClickedListener.onItemClicked(userList.get(getAdapterPosition()));
                }
            });
        }

        void bindTo(User user) {
            Glide.with(itemView.getContext()).load(ApiClient.URL + user.getPhotoUrl()).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)).placeholder(R.drawable.profile_user).error(R.drawable.profile_user).into(profileImageView);
            nameTextView.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));

        }
    }

    public interface OnItemClickedListener {
        void onItemClicked(User user);

        void onCheckedChanged(User user, CustomCheckBox customCheckBox, boolean isChecked);
    }
}
