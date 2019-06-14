package com.kcirqueapps.chatapp.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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
import com.kcirqueapps.chatapp.activity.SearchActivity;
import com.kcirqueapps.chatapp.adapter.ChatListAdapter;
import com.kcirqueapps.chatapp.network.api.Api;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.Chat;
import com.kcirqueapps.chatapp.network.model.Conversion;
import com.kcirqueapps.chatapp.network.model.Group;
import com.kcirqueapps.chatapp.network.model.HttpResponse;
import com.kcirqueapps.chatapp.network.model.User;
import com.kcirqueapps.chatapp.service.MessagingService;
import com.kcirqueapps.chatapp.utils.PrefUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements View.OnClickListener, ChatListAdapter.OnItemClickListener {
    private CompositeDisposable disposable = new CompositeDisposable();
    private Context context;
    private Api api;
    private User currentUser;
    private ChatListAdapter adapter;
    private TextView noDataTextView;
    private ProgressBar progressBar;
    private BroadcastReceiver broadcastReceiver;

    public ChatFragment() {
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
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getChatList();
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter(MessagingService.ACTION_RECEIVED_MESSAGE));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView searchTextView = view.findViewById(R.id.search_text_view);
        searchTextView.setOnClickListener(this);
        RecyclerView userRecyclerView = view.findViewById(R.id.user_recycler_view);
        CircleImageView profileImageView = view.findViewById(R.id.profile_image_view);
        noDataTextView = view.findViewById(R.id.no_data_found);
        progressBar = view.findViewById(R.id.progress_bar);
        adapter = new ChatListAdapter(context);
        userRecyclerView.setHasFixedSize(true);
        userRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        profileImageView.setOnClickListener(this);

        api = ApiClient.getInstance().getApi();

        currentUser = new PrefUtils(context).getUser();
        progressBar.setVisibility(View.VISIBLE);
        Glide.with(this).load(ApiClient.URL + currentUser.getPhotoUrl()).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .placeholder(R.drawable.profile_user).error(R.drawable.profile_user).into(profileImageView);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MessagingService.ACTION_RECEIVED_MESSAGE)) {
                    getChatList();
                    Log.e("Chat", "onReceive: ");
                }
            }
        };

    }

    private void getChatList() {
        disposable.add(
                api.getChatList(currentUser.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<HttpResponse<List<Chat>>>() {
                            @Override
                            public void onSuccess(HttpResponse<List<Chat>> listHttpResponse) {
                                progressBar.setVisibility(View.GONE);
                                if (!listHttpResponse.isError() && listHttpResponse.getResponse().size() > 0) {
                                    noDataTextView.setVisibility(View.GONE);
                                    adapter.setChatList(listHttpResponse.getResponse());
                                } else {
                                    noDataTextView.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                progressBar.setVisibility(View.GONE);
                                e.printStackTrace();
                            }
                        })
        );
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_text_view) {
            gotoSearchActivity();
        } else if (v.getId() == R.id.profile_image_view) {
            NavController navController = Navigation.findNavController(getView());
            navController.navigate(R.id.nav_profile);
        }
    }

    private void gotoSearchActivity() {
        Intent searchIntent = new Intent(getContext(), SearchActivity.class);
        startActivity(searchIntent);
        getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }


    @Override
    public void onUserClicked(User user, Conversion conversion) {
        if (conversion != null && conversion.getReceiverId() == currentUser.getId()) {
            seenMessage(conversion);
        }
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra(ChatActivity.EXTRA_USER, user);
        chatIntent.putExtra(ChatActivity.EXTRA_TYPE, ChatActivity.SINGLE_CHAT);
        startActivity(chatIntent);
        getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void seenMessage(Conversion conversion) {
        disposable.add(api.seenMessage(conversion.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<HttpResponse>() {
                    @Override
                    public void onSuccess(HttpResponse httpResponse) {
                        Log.e("", "onSuccess: " + httpResponse.getMessage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                })
        );
    }

    @Override
    public void onGroupClicked(Group group, Conversion chat) {
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra(ChatActivity.EXTRA_GROUP, group);
        chatIntent.putExtra(ChatActivity.EXTRA_TYPE, ChatActivity.GROUP_CHAT);
        startActivity(chatIntent);
        getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
