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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import javax.inject.Inject;

import butter.droid.R;
import butter.droid.adapters.decorators.DividerItemDecoration;
import butter.droid.base.widget.recycler.RecyclerClickListener;
import butter.droid.base.widget.recycler.RecyclerItemClickListener;
import butter.droid.ui.main.MainActivity;
import butter.droid.ui.main.genre.list.GenreAdapter;
import butter.droid.ui.main.genre.list.model.UiGenre;
import butterknife.BindView;
import butterknife.ButterKnife;

public class GenreSelectionFragment extends Fragment implements GenreSelectionView, RecyclerClickListener {

    @Inject GenreSelectionPresenter presenter;

    @BindView(R.id.recyclerView) RecyclerView recyclerView;

    private GenreAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainActivity) getActivity()).getComponent()
                .genreSelectionBuilder()
                .genresSelectionModule(new GenreSelectionModule(this))
                .build()
                .inject(this);
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
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST,
                R.drawable.list_divider_nospacing));
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(context, this));

        //adapter should only ever be created once on fragment initialise.
        adapter = new GenreAdapter(context);
        recyclerView.setAdapter(adapter);

        presenter.onViewCreated();
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

    public static GenreSelectionFragment newInstance() {
        return new GenreSelectionFragment();
    }
}
