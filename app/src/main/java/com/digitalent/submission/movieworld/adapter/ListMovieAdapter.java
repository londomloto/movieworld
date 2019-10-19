package com.digitalent.submission.movieworld.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.digitalent.submission.movieworld.R;
import com.digitalent.submission.movieworld.databinding.MovieListBinding;
import com.digitalent.submission.movieworld.model.Movie;
import com.digitalent.submission.movieworld.vm.MovieViewModel;

import java.util.ArrayList;

public class ListMovieAdapter extends RecyclerView.Adapter<ListMovieAdapter.ListViewHolder> {
    private final ArrayList<Movie> movies = new ArrayList<>();

    private LayoutInflater layoutInflater;
    private final MovieViewModel movieViewModel;
    private OnItemClickHandler itemClickHandler;

    public ListMovieAdapter(MovieViewModel movieViewModel) {
        this.movieViewModel = movieViewModel;
    }

    public void setMovies(ArrayList<Movie> movies) {
        this.movies.clear();
        this.movies.addAll(movies);
        notifyDataSetChanged();
    }

    public void setOnItemClickHandler(OnItemClickHandler handler) {
        itemClickHandler = handler;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }

        MovieListBinding binding = DataBindingUtil.inflate(
                layoutInflater,
                R.layout.movie_list,
                parent,
                false);

        return new ListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.bind(movie, movieViewModel);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        private final MovieListBinding binding;

        ListViewHolder(final MovieListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Movie movie, MovieViewModel movieViewModel) {
            movie.setLanguage(this.itemView.getResources().getString(R.string.language_param));

            binding.setMovie(movie);
            binding.setViewmodel(movieViewModel);
            binding.executePendingBindings();

            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickHandler.onClick(movie);
                }
            });
        }
    }

    public interface OnItemClickHandler {
        void onClick(Movie movie);
    }
}
