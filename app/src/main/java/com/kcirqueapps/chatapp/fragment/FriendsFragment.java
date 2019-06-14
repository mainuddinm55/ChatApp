package com.kcirqueapps.chatapp.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.activity.ChatActivity;
import com.kcirqueapps.chatapp.adapter.FriendAdapter;
import com.kcirqueapps.chatapp.network.api.Api;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.HttpResponse;
import com.kcirqueapps.chatapp.network.model.User;
import com.kcirqueapps.chatapp.utils.PrefUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment implements FriendAdapter.OnItemClickListener, View.OnClickListener {

    private CompositeDisposable disposable = new CompositeDisposable();
    private Context context;
    private Api api;
    private User currentUser;
    private FriendAdapter adapter;
    private TextView noDataTextView;
    private ProgressBar progressBar;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView userRecyclerView = view.findViewById(R.id.user_recycler_view);
        CircleImageView profileImageView = view.findViewById(R.id.profile_image_view);
        noDataTextView = view.findViewById(R.id.no_data_found);
        progressBar = view.findViewById(R.id.progress_bar);
        adapter = new FriendAdapter(context);
        userRecyclerView.setHasFixedSize(true);
        userRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        profileImageView.setOnClickListener(this);

        api = ApiClient.getInstance().getApi();

        currentUser = new PrefUtils(context).getUser();
        Glide.with(this).load(ApiClient.URL + currentUser.getPhotoUrl()).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .placeholder(R.drawable.profile_user).error(R.drawable.profile_user).into(profileImageView);
        progressBar.setVisibility(View.VISIBLE);
        getPendingRequest();
    }

    private void getPendingRequest() {
        disposable.add(api.getFriends(currentUser.getId()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<HttpResponse<List<User>>>() {
                    @Override
                    public void onSuccess(HttpResponse<List<User>> listHttpResponse) {
                        progressBar.setVisibility(View.GONE);
                        if (!listHttpResponse.isError() && listHttpResponse.getResponse().size() > 0) {
                            noDataTextView.setVisibility(View.GONE);
                            adapter.setUserList(listHttpResponse.getResponse());
                        } else {
                            noDataTextView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressBar.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                }));
    }

    @Override
    public void addFriendClicked(User user) {

    }

    @Override
    public void onAcceptClicked(User user) {
        disposable.add(api.acceptRequest(user.getId(), currentUser.getId()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<HttpResponse>() {
                    @Override
                    public void onSuccess(HttpResponse httpResponse) {
                        if (httpResponse.isError()) {
                            Toasty.error(context, httpResponse.getMessage(), Toasty.LENGTH_SHORT).show();
                        } else {
                            getPendingRequest();
                            Toasty.success(context, httpResponse.getMessage(), Toasty.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                }));
    }

    @Override
    public void onRejectClicked(User user) {

    }

    @Override
    public void onItemClicked(User user,int status) {

        gotoChatActivity(user);
    }

    private void gotoChatActivity(User user) {
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra(ChatActivity.EXTRA_USER, user);
        chatIntent.putExtra(ChatActivity.EXTRA_TYPE,ChatActivity.SINGLE_CHAT);
        startActivity(chatIntent);
        getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.profile_image_view) {
            NavController navController = Navigation.findNavController(getView());
            navController.navigate(R.id.nav_profile);
        }
    }
}
