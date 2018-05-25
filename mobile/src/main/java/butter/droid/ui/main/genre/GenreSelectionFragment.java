/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.ui.main.genre;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import butter.droid.R;
import butter.droid.base.widget.recycler.RecyclerClickListener;
import butter.droid.base.widget.recycler.RecyclerItemClickListener;
import butter.droid.ui.main.genre.list.GenreAdapter;
import butter.droid.ui.main.genre.list.model.UiGenre;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerFragment;

public class GenreSelectionFragment extends DaggerFragment implements GenreSelectionView, RecyclerClickListener {

    private static final String ARG_PROVIDER = "butter.droid.ui.main.genre.GenreSelectionFragment.provider";

    @Inject GenreSelectionPresenter presenter;

    @BindView(R.id.recyclerView) RecyclerView recyclerView;

    private GenreAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        Context context = getContext();

        recyclerView.setHasFixedSize(true);
        DividerItemDecoration decor = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        decor.setDrawable(ContextCompat.getDrawable(context, R.drawable.list_divider_nospacing));
        recyclerView.addItemDecoration(decor);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(context, this));

        //adapter should only ever be created once on fragment initialise.
        adapter = new GenreAdapter(context);
        recyclerView.setAdapter(adapter);

        int providerId = getArguments().getInt(ARG_PROVIDER);

        presenter.onViewCreated(providerId);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override public void onItemClick(View view, int position) {
        presenter.onGenreSelected(position);
    }

    @Override public void displayGenres(List<UiGenre> uiGenres) {
        adapter.setItems(uiGenres);
    }

    @Override public void notifyItemUpdated(int position) {
        adapter.notifyItemChanged(position);
    }

    public static GenreSelectionFragment newInstance(final int providerId) {
        Bundle args = new Bundle(1);
        args.putInt(ARG_PROVIDER, providerId);

        GenreSelectionFragment fragment = new GenreSelectionFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
