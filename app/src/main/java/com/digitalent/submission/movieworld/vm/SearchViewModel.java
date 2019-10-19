package com.digitalent.submission.movieworld.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SearchViewModel extends ViewModel {
    private final MutableLiveData<String> query = new MutableLiveData<>();

    public LiveData<String> getQuery() {
        return query;
    }

    public void postQuery(String value) {
        query.postValue(value);
    }
}
