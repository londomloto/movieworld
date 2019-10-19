package com.digitalent.submission.movieworld.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.digitalent.submission.movieworld.R;
import com.digitalent.submission.movieworld.databinding.FavoriteListBinding;
import com.digitalent.submission.movieworld.model.Favorite;
import com.digitalent.submission.movieworld.vm.FavoriteViewModel;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class ListFavoriteAdapter extends RecyclerView.Adapter<ListFavoriteAdapter.ListViewHolder> {
    private final ArrayList<Favorite> favorites = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private final FavoriteViewModel favoriteViewModel;
    private OnItemClickHandler onItemClickHandler;

    public ListFavoriteAdapter(FavoriteViewModel favoriteViewModel) {
        this.favoriteViewModel = favoriteViewModel;
    }

    public void setFavorites(ArrayList<Favorite> favorites) {
        this.favorites.clear();
        this.favorites.addAll(favorites);

        notifyDataSetChanged();
    }

    public void setOnItemClickHandler(OnItemClickHandler handler) {
        this.onItemClickHandler = handler;
    }

    public ArrayList<Favorite> getFavorites() {
        return this.favorites;
    }

    public Favorite getItemAt(int position) {
        try {
            return favorites.get(position);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void removeItemAt(int position) {
        favorites.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, favorites.size());
    }

    @NonNull
    @Override
    public ListFavoriteAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }

        FavoriteListBinding binding = DataBindingUtil.inflate(
                layoutInflater,
                R.layout.favorite_list,
                parent,
                false
        );

        return new ListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListFavoriteAdapter.ListViewHolder holder, int position) {
        Favorite favorite = favorites.get(position);
        holder.bind(favorite, position, favoriteViewModel);
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        final FavoriteListBinding binding;

        ListViewHolder(FavoriteListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Favorite favorite, final int position, final FavoriteViewModel favoriteViewModel) {

            favorite.setLanguage(this.itemView.getResources().getString(R.string.language_param));

            binding.setFavorite(favorite);
            binding.setPosition(position);
            binding.setViewmodel(favoriteViewModel);
            binding.executePendingBindings();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickHandler.onClick(favorite);
                }
            });
        }
    }

    public interface OnItemClickHandler {
        void onClick(Favorite favorite);
    }

}
