package com.kcirqueapps.chatapp.datasource;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

import com.kcirqueapps.chatapp.network.model.Conversion;

public class DataSourceFactory extends DataSource.Factory {
    private int receiverId;
    private int senderId;
    private String type;
    private int groupId;
    private MutableLiveData<PageKeyedDataSource<Integer, Conversion>> mutableLiveData = new MutableLiveData<>();

    public DataSourceFactory(int receiverId, int senderId, String type, int groupId) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.type = type;
        this.groupId = groupId;
    }

    @NonNull
    @Override
    public DataSource create() {
        ChatDataSource chatDataSource = new ChatDataSource(senderId,receiverId,type,groupId);
        mutableLiveData.postValue(chatDataSource);
        return chatDataSource;
    }

    public MutableLiveData<PageKeyedDataSource<Integer, Conversion>> getMutableLiveData() {
        return mutableLiveData;
    }
}
