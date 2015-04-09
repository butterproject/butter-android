/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.R;
import pct.droid.adapters.GenreAdapter;
import pct.droid.adapters.decorators.DividerItemDecoration;
import pct.droid.base.providers.media.MediaProvider;
import pct.droid.base.providers.media.models.Genre;

public class MediaGenreSelectionFragment extends Fragment {

    public static final String EXTRA_PROVIDER = "extra_provider";

    private Context mContext;
    private RecyclerView.LayoutManager mLayoutManager;
    private GenreAdapter mAdapter;
    private MediaProvider mProvider;
    private Listener mListener;
    private int mSelectedPos = 0;

    @InjectView(R.id.progressOverlay)
    LinearLayout mProgressOverlay;
    @InjectView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @InjectView(R.id.emptyView)
    TextView mEmptyView;
    @InjectView(R.id.progress_textview)
    TextView mProgressTextView;

    public static MediaGenreSelectionFragment newInstance(MediaProvider provider, Listener listener) {
        MediaGenreSelectionFragment frag = new MediaGenreSelectionFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_PROVIDER, provider);
        frag.setArguments(args);
        frag.setListener(listener);
        return frag;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProvider = getArguments().getParcelable(EXTRA_PROVIDER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        View v = inflater.inflate(R.layout.fragment_media, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Genre> genreList = mProvider.getGenres();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST, R.drawable.list_divider_nospacing));
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        //adapter should only ever be created once on fragment initialise.
        mAdapter = new GenreAdapter(mContext, genreList, mSelectedPos);
        mAdapter.setOnItemSelectionListener(mOnItemSelectionListener);
        mRecyclerView.setAdapter(mAdapter);
    }

    private GenreAdapter.OnItemSelectionListener mOnItemSelectionListener = new GenreAdapter.OnItemSelectionListener() {
        @Override
        public void onItemSelect(View v, Genre item, int position) {
            mSelectedPos = position;
            if (mListener != null)
                mListener.onGenreSelected(item.getKey());
        }
    };

    public interface Listener {
        public void onGenreSelected(String genre);
    }

}
