package com.digitalent.submission.movieworld.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FavoriteViewModel extends ViewModel {
    private final MutableLiveData<Integer> removed = new MutableLiveData<>();

    public LiveData<Integer> getRemoved(){
        return this.removed;
    }

    public void postRemoved(Integer position) {
        removed.postValue(position);
    }

    public void onRemoveClick(int position) {
        postRemoved(position);
    }
}
