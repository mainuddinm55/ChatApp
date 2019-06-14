package com.kcirqueapps.chatapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.adapter.AddFriendGroupAdapter;
import com.kcirqueapps.chatapp.network.api.Api;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.Group;
import com.kcirqueapps.chatapp.network.model.GroupMember;
import com.kcirqueapps.chatapp.network.model.HttpResponse;
import com.kcirqueapps.chatapp.network.model.User;
import com.kcirqueapps.chatapp.utils.PrefUtils;

import net.igenius.customcheckbox.CustomCheckBox;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class CreateGroupActivity extends AppCompatActivity implements AddFriendGroupAdapter.OnItemClickedListener {
    private CompositeDisposable disposable = new CompositeDisposable();
    private List<User> memberList = new ArrayList<>();
    private User currentUser;
    private Api api;
    private TextInputLayout nameLayout;
    private TextInputEditText nameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        whiteNotificationBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView userRecyclerView = findViewById(R.id.user_recycler_view);
        nameEditText = findViewById(R.id.group_name_edit_text);
        nameLayout = findViewById(R.id.group_name_layout);
        final AddFriendGroupAdapter addFriendGroupAdapter = new AddFriendGroupAdapter();
        userRecyclerView.setHasFixedSize(true);
        userRecyclerView.setAdapter(addFriendGroupAdapter);
        addFriendGroupAdapter.setOnItemClickedListener(this);
        currentUser = new PrefUtils(this).getUser();
        api = ApiClient.getInstance().getApi();

        disposable.add(api.friendList(currentUser.getId()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<HttpResponse<List<User>>>() {
                    @Override
                    public void onSuccess(HttpResponse<List<User>> listHttpResponse) {
                        if (!listHttpResponse.isError()) {
                            addFriendGroupAdapter.setUserList(listHttpResponse.getResponse());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.next_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next:
                createGroup();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
    private void createGroup() {
        if (TextUtils.isEmpty(nameEditText.getText())) {
            nameLayout.setError("Name is required");
            nameLayout.requestFocus();
            return;
        }
        disposable.add(
                api.createGroup(nameEditText.getText().toString(), "Private", currentUser.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<HttpResponse<Group>>() {
                            @Override
                            public void onSuccess(HttpResponse<Group> httpResponse) {
                                if (!httpResponse.isError()) {
                                    FirebaseMessaging.getInstance().subscribeToTopic(String.format("%s%s",httpResponse.getResponse().getName(),httpResponse.getResponse().getId()))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.e("Subscribe with topic", "onSuccess: " );
                                        }
                                    });
                                    Toasty.success(CreateGroupActivity.this, httpResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                    for (User user : memberList) {
                                        disposable.add(
                                                api.addGroupMember(httpResponse.getResponse().getId(), user.getId(), currentUser.getId()).subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribeWith(new DisposableSingleObserver<HttpResponse<GroupMember>>() {
                                                            @Override
                                                            public void onSuccess(HttpResponse<GroupMember> user) {

                                                            }

                                                            @Override
                                                            public void onError(Throwable e) {
                                                                Log.e("Add Group Member Error", e.getMessage());
                                                                e.printStackTrace();
                                                            }
                                                        })
                                        );
                                    }
                                    finish();
                                } else {
                                    Toasty.error(CreateGroupActivity.this, httpResponse.getMessage(), Toasty.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e( "Create Group Error: " , e.getMessage() );
                                e.printStackTrace();
                            }
                        })
        );

    }

    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    @Override
    public void onItemClicked(User user) {

    }

    @Override
    public void onCheckedChanged(User user, CustomCheckBox customCheckBox, boolean isChecked) {
        if (isChecked) {
            memberList.add(user);
        } else {
            memberList.remove(user);
        }
    }
}
