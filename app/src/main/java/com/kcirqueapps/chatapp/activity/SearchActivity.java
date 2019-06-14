package com.kcirqueapps.chatapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.adapter.FriendAdapter;
import com.kcirqueapps.chatapp.network.api.Api;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.HttpResponse;
import com.kcirqueapps.chatapp.network.model.User;
import com.kcirqueapps.chatapp.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class SearchActivity extends AppCompatActivity implements FriendAdapter.OnItemClickListener {
    public static final String EXTRA_TYPE = "com.kcirqueapps.chatapp.activity.EXTRA_TYPE";
    private static final String TAG = "SearchActivity";
    private CompositeDisposable disposable = new CompositeDisposable();
    private PublishSubject<String> publishSubject = PublishSubject.create();
    private Api api;
    private FriendAdapter adapter;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        api = ApiClient.getInstance().getApi();
        final EditText editText = findViewById(R.id.search_edit_text);
        editText.requestFocus();
        currentUser = new PrefUtils(this).getUser();

        RecyclerView userRecyclerView = findViewById(R.id.user_recycler_view);
        userRecyclerView.setHasFixedSize(true);
        adapter = new FriendAdapter(this);
        adapter.setOnItemClickListener(this);
        userRecyclerView.setAdapter(adapter);
        whiteNotificationBar(userRecyclerView);
        DisposableObserver<HttpResponse<List<User>>> observer = getSearchObserver();

        disposable.add(publishSubject.debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .switchMap(new Function<String, Observable<HttpResponse<List<User>>>>() {
                    @Override
                    public Observable<HttpResponse<List<User>>> apply(String s) throws Exception {
                        return api.searchUser(currentUser.getId(), s)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread());
                    }
                })
                .subscribeWith(observer));


        // skipInitialValue() - skip for the first time when EditText empty
        disposable.add(RxTextView.textChangeEvents(editText)
                .skipInitialValue()
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(searchContactsTextWatcher()));

        disposable.add(observer);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String type = bundle.getString(EXTRA_TYPE);
            if (type != null && type.equals("friend")) {
                disposable.add(api.friendList(currentUser.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<HttpResponse<List<User>>>() {
                            @Override
                            public void onSuccess(HttpResponse<List<User>> listHttpResponse) {
                                if (!listHttpResponse.isError()) {
                                    adapter.setUserList(listHttpResponse.getResponse());
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        })
                );
            }
        }

    }

    private DisposableObserver<HttpResponse<List<User>>> getSearchObserver() {
        return new DisposableObserver<HttpResponse<List<User>>>() {
            @Override
            public void onNext(HttpResponse<List<User>> users) {
                if (!users.isError()) {
                    adapter.setUserList(users.getResponse());
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        };
    }

    private DisposableObserver<TextViewTextChangeEvent> searchContactsTextWatcher() {
        return new DisposableObserver<TextViewTextChangeEvent>() {
            @Override
            public void onNext(TextViewTextChangeEvent textViewTextChangeEvent) {
                Log.d(TAG, "Search query: " + textViewTextChangeEvent.text());
                if (!textViewTextChangeEvent.text().toString().isEmpty()) {
                    publishSubject.onNext(textViewTextChangeEvent.text().toString());
                }else {
                    adapter.setUserList(new ArrayList<User>());
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        };
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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
    public void addFriendClicked(User user) {
        disposable.add(
                api.sendRequest(currentUser.getId(), user.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<HttpResponse<String>>() {
                            @Override
                            public void onSuccess(HttpResponse<String> stringHttpResponse) {
                                Log.e(TAG, "onSuccess: " + stringHttpResponse.getResponse());
                                if (stringHttpResponse.isError()) {
                                    Toasty.error(SearchActivity.this, stringHttpResponse.getMessage(), Toasty.LENGTH_SHORT).show();
                                } else {
                                    Toasty.success(SearchActivity.this, stringHttpResponse.getMessage(), Toasty.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                Toasty.error(SearchActivity.this, e.getMessage(), Toasty.LENGTH_SHORT).show();
                            }
                        })
        );
    }

    @Override
    public void onAcceptClicked(User user) {

    }

    @Override
    public void onRejectClicked(User user) {

    }

    @Override
    public void onItemClicked(User user, int status) {
        if (status == 1) {
            gotoChatActivity(user);
        } else {
            gotoProfileActivity(user);
        }
    }

    private void gotoProfileActivity(User user) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.EXTRA_USER, user);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void gotoChatActivity(User user) {
        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra(ChatActivity.EXTRA_USER, user);
        chatIntent.putExtra(ChatActivity.EXTRA_TYPE, ChatActivity.SINGLE_CHAT);
        startActivity(chatIntent);
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
