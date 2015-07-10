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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;
import pct.droid.R;
import pct.droid.base.Constants;
import pct.droid.base.utils.IntentUtils;

public class AboutFragment extends Fragment {

    @Bind(R.id.logo_imageview)
    ImageView mLogoImageView;
    @Bind(R.id.facebook_button)
    TextView mFacebookButton;
    @Bind(R.id.git_button)
    TextView mGitButton;
    @Bind(R.id.blog_button)
    TextView mBlogButton;
    @Bind(R.id.popcorn_button)
    TextView mPopcornButton;
    @Bind(R.id.discuss_button)
    TextView mDiscussButton;
    @Bind(R.id.twitter_button)
    TextView mTwitterButton;

    private OnFragmentInteractionListener mListener;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        //		public void onFragmentInteraction(Uri uri);
    }


    @OnClick(R.id.logo_imageview)
    void onLogoClick() {
        startActivity(IntentUtils.getBrowserIntent(getActivity(), Constants.POPCORN_URL));
    }

    @OnClick(R.id.facebook_button)
    void onFacebookClick() {
        startActivity(IntentUtils.getBrowserIntent(getActivity(), Constants.FB_URL));
    }

    @OnClick(R.id.git_button)
    void onGitClick() {
        startActivity(IntentUtils.getBrowserIntent(getActivity(), Constants.GIT_URL));
    }

    @OnClick(R.id.blog_button)
    void onBlogClick() {
        startActivity(IntentUtils.getBrowserIntent(getActivity(), Constants.BLOG_URL));
    }

    @OnClick(R.id.popcorn_button)
    void onPopcornClick() {
        startActivity(IntentUtils.getBrowserIntent(getActivity(), Constants.POPCORN_URL));
    }

    @OnClick(R.id.discuss_button)
    void onDiscussClick() {
        startActivity(IntentUtils.getBrowserIntent(getActivity(), Constants.DISCUSS_URL));
    }

    @OnClick(R.id.twitter_button)
    void onTwitterClick() {
        startActivity(IntentUtils.getBrowserIntent(getActivity(), Constants.TWITTER_URL));
    }

}
