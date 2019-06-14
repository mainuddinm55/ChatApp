package com.kcirqueapps.chatapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.network.api.Api;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.Group;
import com.kcirqueapps.chatapp.network.model.HttpResponse;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupHolder> {
    private CompositeDisposable disposable = new CompositeDisposable();
    private Api api;
    private List<Group> groupList = new ArrayList<>();
    private OnItemClickedListener onItemClickedListener;

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    public GroupAdapter(Context context) {
        api = ApiClient.getInstance().getApi();
    }

    @NonNull
    @Override
    public GroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_group, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupHolder holder, int position) {
        Group group = groupList.get(position);
        holder.bindTo(group);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList;
        notifyDataSetChanged();
    }

    class GroupHolder extends RecyclerView.ViewHolder {
        final CircleImageView profileImageView;
        final TextView nameTextView, memberCountTextView;

        GroupHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            memberCountTextView = itemView.findViewById(R.id.member_count_text_view);

           LinearLayout rootView = itemView.findViewById(R.id.root_view);
           rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickedListener != null)
                        onItemClickedListener.onItemClicked(groupList.get(getAdapterPosition()));
                }
            });
        }

        void bindTo(Group group) {
            Glide.with(itemView.getContext()).load(R.drawable.profile_user).into(profileImageView);
            nameTextView.setText(group.getName());

            disposable.add(
                    api.getGroupMemberCount(group.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableSingleObserver<HttpResponse<Integer>>() {
                                @Override
                                public void onSuccess(HttpResponse<Integer> integerHttpResponse) {
                                    memberCountTextView.setText(String.format("Total Member: %s", integerHttpResponse.getResponse()));
                                }

                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                }
                            })
            );
        }
    }

    public interface OnItemClickedListener {
        void onItemClicked(Group group);
    }
}
