package com.kcirqueapps.chatapp.datasource;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.paging.PageKeyedDataSource;

import com.kcirqueapps.chatapp.network.api.Api;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.Conversion;
import com.kcirqueapps.chatapp.network.model.HttpResponse;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class ChatDataSource extends PageKeyedDataSource<Integer, Conversion> {
    private CompositeDisposable disposable = new CompositeDisposable();
    private static final String TAG = "ChatDataSource";
    private static final int PAGE = 1;
    public static final int PAGE_SIZE = 20;
    private static Api api;
    private int senderId;
    private int receiverId;
    private int groupId;
    private String type;


    public ChatDataSource(int senderId, int receiverId, String type, int groupId) {
        api = ApiClient.getInstance().getApi();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
        this.groupId = groupId;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, Conversion> callback) {
        if (type.equals("Single")) {
            disposable.add(
                    api.getSingleConversions(senderId, receiverId,1, PAGE, PAGE_SIZE).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableSingleObserver<HttpResponse<List<Conversion>>>() {
                                @Override
                                public void onSuccess(HttpResponse<List<Conversion>> listHttpResponse) {
                                    if (!listHttpResponse.isError()) {
                                        callback.onResult(listHttpResponse.getResponse(), null, PAGE + 1);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e(TAG, "Initial Load: " + e.getMessage());
                                    e.getMessage();
                                }
                            })
            );
        } else if (type.equals("Group")) {
            disposable.add(
                    api.getGroupConversions(groupId, 1,PAGE, PAGE_SIZE).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableSingleObserver<HttpResponse<List<Conversion>>>() {
                                @Override
                                public void onSuccess(HttpResponse<List<Conversion>> listHttpResponse) {
                                    if (!listHttpResponse.isError()) {
                                        callback.onResult(listHttpResponse.getResponse(), null, PAGE + 1);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e(TAG, "Initial Load: " + e.getMessage());
                                    e.getMessage();
                                }
                            })
            );

        }
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull final LoadCallback<Integer, Conversion> callback) {
        final Integer key = params.key > 1 ? params.key - 1 : null;
        if (type.equals("Single")) {
            disposable.add(
                    api.getSingleConversions(senderId, receiverId, 1,params.key, PAGE_SIZE).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableSingleObserver<HttpResponse<List<Conversion>>>() {
                                @Override
                                public void onSuccess(HttpResponse<List<Conversion>> listHttpResponse) {
                                    if (!listHttpResponse.isError()) {
                                        callback.onResult(listHttpResponse.getResponse(), key);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e(TAG, "Initial Load: " + e.getMessage());
                                    e.getMessage();
                                }
                            })
            );
        } else if (type.equals("Group")) {
            disposable.add(
                    api.getGroupConversions(groupId, 1,params.key, PAGE_SIZE).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableSingleObserver<HttpResponse<List<Conversion>>>() {
                                @Override
                                public void onSuccess(HttpResponse<List<Conversion>> listHttpResponse) {
                                    if (!listHttpResponse.isError()) {
                                        callback.onResult(listHttpResponse.getResponse(), key);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e(TAG, "Initial Load: " + e.getMessage());
                                    e.getMessage();
                                }
                            })
            );

        }
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull final LoadCallback<Integer, Conversion> callback) {
        final Integer key = params.key + 1;
        if (type.equals("Single")) {
            disposable.add(
                    api.getSingleConversions(senderId, receiverId,1, params.key, PAGE_SIZE).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableSingleObserver<HttpResponse<List<Conversion>>>() {
                                @Override
                                public void onSuccess(HttpResponse<List<Conversion>> listHttpResponse) {
                                    if (!listHttpResponse.isError()) {
                                        callback.onResult(listHttpResponse.getResponse(), key);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e(TAG, "Initial Load: " + e.getMessage());
                                    e.getMessage();
                                }
                            })
            );
        } else if (type.equals("Group")) {
            disposable.add(
                    api.getGroupConversions(groupId,1, params.key, PAGE_SIZE).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableSingleObserver<HttpResponse<List<Conversion>>>() {
                                @Override
                                public void onSuccess(HttpResponse<List<Conversion>> listHttpResponse) {
                                    if (!listHttpResponse.isError()) {
                                        callback.onResult(listHttpResponse.getResponse(), key);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e(TAG, "Initial Load: " + e.getMessage());
                                    e.getMessage();
                                }
                            })
            );

        }
    }
}
