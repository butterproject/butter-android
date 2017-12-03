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

package butter.droid.ui.media.detail.show.about;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import butter.droid.R;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.ui.media.detail.movie.dialog.SynopsisDialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import dagger.android.support.DaggerFragment;
import javax.inject.Inject;
import org.parceler.Parcels;

public class ShowDetailAboutFragment extends DaggerFragment implements ShowDetailAboutView {

    private static final String ARG_SHOW = "butter.droid.ui.media.detail.show.about.ShowDetailAboutFragment.show";

    @Inject ShowDetailAboutPresenter presenter;

    @BindView(R.id.title) TextView tvTitle;
    @BindView(R.id.meta) TextView tvMetaData;
    @BindView(R.id.synopsis) TextView tvSynopsis;
    @BindView(R.id.rating) RatingBar rbRating;
    @BindView(R.id.read_more) Button readMore;
    @BindView(R.id.info_buttons) LinearLayout infoButtons;
    @BindView(R.id.magnet) @Nullable ImageButton openMagnet;
    @Nullable @BindView(R.id.cover_image) ImageView coverImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MediaWrapper show = Parcels.unwrap(getArguments().getParcelable(ARG_SHOW));

        presenter.onCreate(show);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_about, container, false);

        if (container != null) {
            view.setMinimumHeight(container.getMinimumHeight());
        }

        return view;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (openMagnet != null) {
            openMagnet.setVisibility(View.GONE);
        }

        presenter.onViewCreated();
    }

    @OnClick(R.id.read_more) public void openReadMore() {
        presenter.readMoreClicked();
    }
    
    @Override public void displayTitle(String title) {
        tvTitle.setText(title);
    }

    @Override public void hideRating() {
        rbRating.setVisibility(View.GONE);
    }

    @Override public void displayRating(int rating, String cd) {
        rbRating.setProgress(rating);
        rbRating.setContentDescription(cd);
        rbRating.setVisibility(View.VISIBLE);
    }

    @Override public void displayMetaData(String metaData) {
        tvMetaData.setText(metaData);
    }

    @Override public void displaySynopsis(String synopsis) {
        tvSynopsis.setVisibility(View.VISIBLE);
        tvSynopsis.setText(synopsis);
        tvSynopsis.post(() -> {
            boolean ellipsized = false;
            Layout layout = tvSynopsis.getLayout();
            if (layout == null) {
                return;
            }
            int lines = layout.getLineCount();
            if (lines > 0) {
                int ellipsisCount = layout.getEllipsisCount(lines - 1);
                if (ellipsisCount > 0) {
                    ellipsized = true;
                }
            }
            infoButtons.setVisibility(ellipsized ? View.VISIBLE : View.GONE);
        });
    }

    @Override public void hideSynopsis() {
        tvSynopsis.setVisibility(View.GONE);
        infoButtons.setVisibility(View.GONE);
    }

    @Override public void openSynopsisDialog(String synopsis) {
        // TODO: 3/20/17 Make it nicer (part of SynopsisDialogFragment refactor)
        if (getFragmentManager().findFragmentByTag("overlay_fragment") != null) {
            return;
        }

        SynopsisDialogFragment synopsisDialogFragment = new SynopsisDialogFragment();
        Bundle args = new Bundle();
        args.putString("text", synopsis);
        synopsisDialogFragment.setArguments(args);
        synopsisDialogFragment.show(getFragmentManager(), "overlay_fragment");
    }

    @Override
    public void displayImage(final String image) {
        if (coverImage != null) {
            Picasso.with(coverImage.getContext()).load(image).into(coverImage);
        }
    }

    public static ShowDetailAboutFragment newInstance(MediaWrapper show) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_SHOW, Parcels.wrap(show));

        ShowDetailAboutFragment fragment = new ShowDetailAboutFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
