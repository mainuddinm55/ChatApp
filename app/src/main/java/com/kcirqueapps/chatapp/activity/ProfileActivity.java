package com.kcirqueapps.chatapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.network.api.Api;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.Group;
import com.kcirqueapps.chatapp.network.model.HttpResponse;
import com.kcirqueapps.chatapp.network.model.User;
import com.kcirqueapps.chatapp.utils.PrefUtils;

import java.util.HashSet;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ProfileActivity";
    private TextView groupCountTextView;
    private TextView friendCountTextView;
    private CompositeDisposable disposable = new CompositeDisposable();
    private Api api;
    public static final String EXTRA_USER = "com.kcirqueapps.chatapp.activity.EXTRA_USER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        api = ApiClient.getInstance().getApi();
        CircleImageView profileImageView = findViewById(R.id.profile_image_view);
        TextView nameTextView = findViewById(R.id.name_text_view);
        TextView emailTextView = findViewById(R.id.email_text_view);
        TextView phoneTextView = findViewById(R.id.mobile_text_view);
        TextView birthdayTextView = findViewById(R.id.birthday_text_view);
        friendCountTextView = findViewById(R.id.friend_count_text_view);
        groupCountTextView = findViewById(R.id.group_count_text_view);
        Button logoutBtn = findViewById(R.id.logout_btn);
        whiteNotificationBar(profileImageView);
        logoutBtn.setText("Back");
        logoutBtn.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            User chatUser = (User) bundle.getSerializable(EXTRA_USER);
            if (chatUser != null) {
                Log.e(TAG, "onViewCreated: ");
                nameTextView.setText(String.format("%s %s", chatUser.getFirstName(), chatUser.getLastName()));
                emailTextView.setText(chatUser.getEmail());
                phoneTextView.setText(chatUser.getMobile());
                birthdayTextView.setText(chatUser.getDateOfBirth().substring(0, 10));
                String url = ApiClient.URL + chatUser.getPhotoUrl();
                Log.e(TAG, "URL: " + url);

                Glide.with(this).load(url).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)).placeholder(R.drawable.profile_user).error(R.drawable.profile_user).into(profileImageView);

                getFriendCount(chatUser.getId());
                getGroupCount(chatUser.getId());
            }
        }

    }

    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    private void getGroupCount(int id) {
        disposable.add(
                api.getGroups(id).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<HttpResponse<List<Group>>>() {
                            @Override
                            public void onSuccess(HttpResponse<List<Group>> listHttpResponse) {
                                if (!listHttpResponse.isError()) {
                                    groupCountTextView.setText(String.format("%s", listHttpResponse.getResponse().size()));
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }
                        })
        );
    }

    private void getFriendCount(int id) {
        disposable.add(api.friendList(id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<HttpResponse<List<User>>>() {
                    @Override
                    public void onSuccess(HttpResponse<List<User>> listHttpResponse) {
                        if (!listHttpResponse.isError()) {
                            friendCountTextView.setText(String.format("%s", listHttpResponse.getResponse().size()));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                })
        );
    }

    @Override
    public void onClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
