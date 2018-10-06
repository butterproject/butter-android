package butter.droid.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.util.Log;

import butter.droid.R;
import butter.droid.MobileButterApplication;
import butter.droid.base.providers.media.AnimeProvider;
import butter.droid.base.providers.media.TVProvider;
import butter.droid.base.providers.media.models.Show;
import butter.droid.base.utils.VersionUtils;
import butter.droid.base.utils.FavouriteUtils;
import butter.droid.fragments.base.BaseDetailFragment;
import butter.droid.fragments.dialog.SynopsisDialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShowDetailAboutFragment extends BaseDetailFragment {

    private static Show sShow;

    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.meta)
    TextView mMeta;
    @BindView(R.id.synopsis)
    TextView mSynopsis;
    @BindView(R.id.rating)
    RatingBar mRating;
    @BindView(R.id.read_more)
    Button mReadMore;
    @BindView(R.id.toggle_favourite)
    Button mToggleFavourite;
    @BindView(R.id.info_buttons)
    LinearLayout mInfoButtons;
    @BindView(R.id.magnet)
    ImageButton mOpenMagnet;

    public static ShowDetailAboutFragment newInstance(Show show) {
        Bundle b = new Bundle();
        sShow = show;
        ShowDetailAboutFragment showDetailFragment = new ShowDetailAboutFragment();
        showDetailFragment.setArguments(b);
        return showDetailFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_detail_about, container, false);
        ButterKnife.bind(this, mRoot);
        if (VersionUtils.isJellyBean() && container != null) {
            mRoot.setMinimumHeight(container.getMinimumHeight());
        }

        if(sShow == null)
            return mRoot;

        mTitle.setText(sShow.title);
        if (!sShow.rating.equals("-1")) {
            Double rating = Double.parseDouble(sShow.rating);
            mRating.setProgress(rating.intValue());
            mRating.setContentDescription("Rating: " + rating.intValue() + " out of 10");
            mRating.setVisibility(View.VISIBLE);
        } else {
            mRating.setVisibility(View.GONE);
        }

        StringBuilder metaDataStr = new StringBuilder();
        metaDataStr.append(sShow.year);

        if (sShow.status != Show.Status.UNKNOWN) {
            metaDataStr.append(" • ");
            if (sShow.status == Show.Status.CONTINUING) {
                metaDataStr.append(getString(R.string.continuing));
            } else {
                metaDataStr.append(getString(R.string.ended));
            }
        }

        if (!TextUtils.isEmpty(sShow.genre)) {
            metaDataStr.append(" • ");
            metaDataStr.append(sShow.genre);
        }

        mMeta.setText(metaDataStr.toString());

        if (!TextUtils.isEmpty(sShow.synopsis)) {
            mSynopsis.setText(sShow.synopsis);
            mSynopsis.post(new Runnable() {
                @Override
                public void run() {
                    boolean ellipsized = false;
                    Layout layout = mSynopsis.getLayout();
                    if (layout == null) return;
                    int lines = layout.getLineCount();
                    if (lines > 0) {
                        int ellipsisCount = layout.getEllipsisCount(lines - 1);
                        if (ellipsisCount > 0) {
                            ellipsized = true;
                        }
                    }
                    mReadMore.setVisibility(ellipsized ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mReadMore.setVisibility(View.GONE);
        }

        Context context = MobileButterApplication.getAppContext();
        Class provider = sShow.isAnime ? AnimeProvider.class : TVProvider.class;
        mToggleFavourite.setText(FavouriteUtils.isFavourite(context, provider, sShow) ?
            R.string.remove_from_favourites : R.string.add_to_favourites);

        mOpenMagnet.setVisibility(View.GONE);

        return mRoot;
    }

    @OnClick(R.id.read_more)
    public void openReadMore(View v) {
        if (getFragmentManager().findFragmentByTag("overlay_fragment") != null)
            return;
        SynopsisDialogFragment synopsisDialogFragment = new SynopsisDialogFragment();
        Bundle b = new Bundle();
        b.putString("text", sShow.synopsis);
        synopsisDialogFragment.setArguments(b);
        synopsisDialogFragment.show(getFragmentManager(), "overlay_fragment");
    }

    @OnClick(R.id.toggle_favourite)
    public void toggleFavourite(View v) {
        Context context = MobileButterApplication.getAppContext();
        Class provider = sShow.isAnime ? AnimeProvider.class : TVProvider.class;
        if (FavouriteUtils.isFavourite(context, provider, sShow)) {
            FavouriteUtils.removeFavourite(context, provider, sShow);
            mToggleFavourite.setText(R.string.add_to_favourites);
        } else {
            FavouriteUtils.addFavourite(context, provider, sShow);
            mToggleFavourite.setText(R.string.remove_from_favourites);
        }
    }

}
