package pct.droid.fragments;

import android.annotation.TargetApi;
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

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;
import pct.droid.R;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.utils.VersionUtils;
import pct.droid.dialogfragments.SynopsisDialogFragment;
import pct.droid.fragments.base.BaseDetailFragment;

public class ShowDetailAboutFragment extends BaseDetailFragment {

    private static Show sShow;

    @Bind(R.id.title)
    TextView mTitle;
    @Bind(R.id.meta)
    TextView mMeta;
    @Bind(R.id.synopsis)
    TextView mSynopsis;
    @Bind(R.id.rating)
    RatingBar mRating;
    @Bind(R.id.read_more)
    Button mReadMore;
    @Bind(R.id.info_buttons)
    LinearLayout mInfoButtons;
    @Bind(R.id.magnet)
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

        mTitle.setText(sShow.title);
        if (!sShow.rating.equals("-1")) {
            Double rating = Double.parseDouble(sShow.rating);
            mRating.setProgress(rating.intValue());
            mRating.setContentDescription("Rating: " + rating.intValue() + " out of 10");
            mRating.setVisibility(View.VISIBLE);
        } else {
            mRating.setVisibility(View.GONE);
        }

        String metaDataStr = sShow.year;

        if (sShow.status != Show.Status.UNKNOWN) {
            metaDataStr += " • ";
            if (sShow.status == Show.Status.CONTINUING) {
                metaDataStr += getString(R.string.continuing);
            } else {
                metaDataStr += getString(R.string.ended);
            }
        }

        if (!TextUtils.isEmpty(sShow.genre)) {
            metaDataStr += " • ";
            metaDataStr += sShow.genre;
        }

        mMeta.setText(metaDataStr);

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
                    mInfoButtons.setVisibility(ellipsized ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mInfoButtons.setVisibility(View.GONE);
        }

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

}
