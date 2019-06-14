package com.kcirqueapps.chatapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.network.api.Api;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.Friendship;
import com.kcirqueapps.chatapp.network.model.HttpResponse;
import com.kcirqueapps.chatapp.network.model.User;
import com.kcirqueapps.chatapp.utils.CircleTransform;
import com.kcirqueapps.chatapp.utils.PrefUtils;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;


public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendHolder> {
    private List<User> userList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private User currentUser;
    private Api api;

    public FriendAdapter(Context context) {
        currentUser = new PrefUtils(context).getUser();
        api = ApiClient.getInstance().getApi();
    }

    @NonNull
    @Override
    public FriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_search_people, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendHolder holder, int position) {
        final User user = userList.get(position);
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

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class FriendHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView addFriendImageView, acceptImageView, rejectImageView;
        final CircleImageView profileImageView;
        final TextView nameTextView, isFriendTextView;
        final LinearLayout actionLayout;
        final RelativeLayout rootView;

        FriendHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.root_view);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            addFriendImageView = itemView.findViewById(R.id.add_friend_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            acceptImageView = itemView.findViewById(R.id.accept_image_view);
            rejectImageView = itemView.findViewById(R.id.reject_image_view);
            isFriendTextView = itemView.findViewById(R.id.connected_text_view);
            actionLayout = itemView.findViewById(R.id.action_layout);
            addFriendImageView.setOnClickListener(this);
            acceptImageView.setOnClickListener(this);
            rejectImageView.setOnClickListener(this);
        }

        void bindTo(final User user) {
            final String url = ApiClient.URL + user.getPhotoUrl();
            Glide.with(itemView).load(url).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)).placeholder(R.drawable.profile_user).error(R.drawable.profile_user).into(profileImageView);

            nameTextView.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
            api.friendshipStatus(currentUser.getId(), user.getId()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableSingleObserver<HttpResponse<Friendship>>() {
                @Override
                public void onSuccess(HttpResponse<Friendship> friendshipHttpResponse) {
                    if (!friendshipHttpResponse.isError()) {
                        final Friendship friendship = friendshipHttpResponse.getResponse();
                        if (user.getId() == friendship.getActionUserId() && friendship.getStatus() == 0) {
                            //User send request ot you
                            actionLayout.setVisibility(View.VISIBLE);
                            addFriendImageView.setVisibility(View.GONE);
                            isFriendTextView.setText("Want to be your friend");
                        } else if (currentUser.getId() == friendship.getActionUserId() && friendship.getStatus() == 0) {
                            actionLayout.setVisibility(View.GONE);
                            addFriendImageView.setVisibility(View.GONE);
                            isFriendTextView.setText("You send request");
                        } else if (user.getId() == friendship.getActionUserId() && friendship.getStatus() == 1) {
                            actionLayout.setVisibility(View.GONE);
                            addFriendImageView.setVisibility(View.GONE);
                            isFriendTextView.setText("Connected");
                        } else if (currentUser.getId() == friendship.getActionUserId() && friendship.getStatus() == 1) {
                            actionLayout.setVisibility(View.GONE);
                            addFriendImageView.setVisibility(View.GONE);
                            isFriendTextView.setText("Connected");
                        } else if (friendship.getStatus() != 4) {
                            actionLayout.setVisibility(View.GONE);
                            addFriendImageView.setVisibility(View.VISIBLE);
                            isFriendTextView.setText("Join with you");
                        }
                        rootView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (onItemClickListener != null) {
                                    onItemClickListener.onItemClicked(userList.get(getAdapterPosition()), friendship.getStatus());
                                }
                            }
                        });
                    } else {
                        actionLayout.setVisibility(View.GONE);
                        addFriendImageView.setVisibility(View.VISIBLE);
                        isFriendTextView.setText("Join with you");
                    }

                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }
            });

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.add_friend_image_view:
                    if (onItemClickListener != null) {
                        onItemClickListener.addFriendClicked(userList.get(getAdapterPosition()));
                    }
                    break;
                case R.id.accept_image_view:
                    if (onItemClickListener != null)
                        onItemClickListener.onAcceptClicked(userList.get(getAdapterPosition()));
                    break;
                case R.id.reject_image_view:
                    if (onItemClickListener != null)
                        onItemClickListener.onRejectClicked(userList.get(getAdapterPosition()));
                    break;
            }
        }
    }

    public interface OnItemClickListener {
        void addFriendClicked(User user);

        void onAcceptClicked(User user);

        void onRejectClicked(User user);

        void onItemClicked(User user, int status);
    }
}
