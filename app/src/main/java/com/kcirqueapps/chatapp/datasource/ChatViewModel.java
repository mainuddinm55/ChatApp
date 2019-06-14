package com.kcirqueapps.chatapp.datasource;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PageKeyedDataSource;
import androidx.paging.PagedList;

import com.kcirqueapps.chatapp.network.model.Conversion;

public class ChatViewModel extends ViewModel {
    public LiveData<PagedList<Conversion>> getConversionList(int receiverId, int senderId, String type, int groupId) {
        DataSourceFactory dataSourceFactory = new DataSourceFactory(senderId, receiverId, type, groupId);
        LiveData<PageKeyedDataSource<Integer, Conversion>> dataSourceLiveData = dataSourceFactory.getMutableLiveData();
        PagedList.Config config = new PagedList.Config.Builder()
                .setPageSize(ChatDataSource.PAGE_SIZE)
                .setEnablePlaceholders(false)
                .build();
        LiveData<PagedList<Conversion>> conversionList = (new LivePagedListBuilder<Integer, Conversion>(dataSourceFactory, config)).build();
        return conversionList;
    }
}
