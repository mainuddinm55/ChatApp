package com.kcirqueapps.chatapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.adapter.GroupAdapter;
import com.kcirqueapps.chatapp.network.api.Api;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.Group;
import com.kcirqueapps.chatapp.network.model.HttpResponse;
import com.kcirqueapps.chatapp.network.model.User;
import com.kcirqueapps.chatapp.utils.PrefUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class GroupActivity extends AppCompatActivity implements GroupAdapter.OnItemClickedListener {
    private CompositeDisposable disposable = new CompositeDisposable();
    private static final String TAG = "GroupActivity";
    private PublishSubject<String> publishSubject = PublishSubject.create();
    private Api api;
    private User currentUser;

    private GroupAdapter groupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        whiteNotificationBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        api = ApiClient.getInstance().getApi();
        currentUser = new PrefUtils(this).getUser();
        groupAdapter = new GroupAdapter(this);
        groupAdapter.setOnItemClickedListener(this);
        final EditText editText = findViewById(R.id.search_edit_text);
        editText.requestFocus();
        TextView newGroupTextView = findViewById(R.id.new_group_text_view);
        RecyclerView groupRecyclerView = findViewById(R.id.group_recycler_view);
        groupRecyclerView.setHasFixedSize(true);
        groupRecyclerView.setAdapter(groupAdapter);

        newGroupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GroupActivity.this, CreateGroupActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
            }
        });

        getGroups();

        DisposableObserver<HttpResponse<List<Group>>> observer = getSearchObserver();

        disposable.add(publishSubject.debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .switchMap(new Function<String, Observable<HttpResponse<List<Group>>>>() {
                    @Override
                    public Observable<HttpResponse<List<Group>>> apply(String s) throws Exception {
                        return api.searchGroup(currentUser.getId(), s)
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

    }

    private void getGroups() {
        disposable.add(
                api.getGroups(currentUser.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<HttpResponse<List<Group>>>() {
                            @Override
                            public void onSuccess(HttpResponse<List<Group>> listHttpResponse) {
                                if (!listHttpResponse.isError()) {
                                    groupAdapter.setGroupList(listHttpResponse.getResponse());
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }
                        })
        );
    }

    private DisposableObserver<HttpResponse<List<Group>>> getSearchObserver() {
        return new DisposableObserver<HttpResponse<List<Group>>>() {
            @Override
            public void onNext(HttpResponse<List<Group>> users) {
                if (!users.isError()) {
                    groupAdapter.setGroupList(users.getResponse());
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
                publishSubject.onNext(textViewTextChangeEvent.text().toString());
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
        if (item.getItemId() == android.R.id.home) onBackPressed();
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
    public void onItemClicked(Group group) {
        Intent chatIntent = new Intent(this,ChatActivity.class);
        chatIntent.putExtra(ChatActivity.EXTRA_GROUP,group);
        chatIntent.putExtra(ChatActivity.EXTRA_TYPE,ChatActivity.GROUP_CHAT);
        startActivity(chatIntent);
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
