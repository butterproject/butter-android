package butter.droid.tv.ui.preferences.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.leanback.widget.BaseCardView.LayoutParams;
import butter.droid.tv.R;

public class TVWebViewFragment extends Fragment {

    public static final String TAG = TVWebViewFragment.class.getSimpleName();

    private static final String EXTRA_URL = "butter.droid.tv.ui.preferences.fragment.TVWebViewFragment.EXTRA_URL";

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_webview, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        final ProgressBar progressBar = view.findViewById(R.id.fragment_webview_progress);

        final WebView webView = view.findViewById(R.id.fragment_webview_webview);
        webView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(final WebView view, final String url) {
                progressBar.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);

        final String url = getArguments().getString(EXTRA_URL);
        webView.loadUrl(url);
    }

    public static TVWebViewFragment newInstance(final Uri uri) {
        return newInstance(uri.toString());
    }

    public static TVWebViewFragment newInstance(final String url) {
        final Bundle bundle = new Bundle();
        bundle.putString(EXTRA_URL, url);

        final TVWebViewFragment fragment = new TVWebViewFragment();
        fragment.setArguments(bundle);

        return fragment;
    }
}
