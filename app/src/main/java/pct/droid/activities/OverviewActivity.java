package pct.droid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import butterknife.InjectView;
import pct.droid.R;
import pct.droid.providers.media.MediaProvider;
import pct.droid.providers.media.YTSProvider;
import pct.droid.utils.LogUtils;
import pct.droid.widget.OverviewGridAdapter;

public class OverviewActivity extends BaseActivity {

    private OverviewGridAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private YTSProvider mProvider = new YTSProvider();

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.progressOverlay)
    LinearLayout progressOverlay;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_overview);
        setSupportActionBar(toolbar);

        recyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);

        getApp().runScript("app", "9000");

        mProvider.getList(null, mCallback);
    }

    private OverviewGridAdapter.OnItemClickListener mOnItemClickListener = new OverviewGridAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, final MediaProvider.Video item, int position) {
            progressOverlay.setBackgroundColor(getResources().getColor(R.color.overlay_bg));
            progressOverlay.setVisibility(View.VISIBLE);

            mProvider.getDetail(item.imdbId, new MediaProvider.Callback() {
                @Override
                public void onSuccess(ArrayList<MediaProvider.Video> items) {
                    if (items.size() <= 0) return;

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressOverlay.setVisibility(View.GONE);
                            progressOverlay.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                        }
                    });

                    MediaProvider.Video item = items.get(0);
                    Intent intent = new Intent(OverviewActivity.this, MovieDetailActivity.class);
                    intent.putExtra("item", item);
                    startActivity(intent);
                }

                @Override
                public void onFailure(Exception exception) {

                }
            });
        }
    };

    private MediaProvider.Callback mCallback = new MediaProvider.Callback() {
        @Override
        public void onSuccess(ArrayList<MediaProvider.Video> items) {
            mAdapter = new OverviewGridAdapter(OverviewActivity.this, items);
            mAdapter.setOnItemClickListener(mOnItemClickListener);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressOverlay.setVisibility(View.GONE);
                    recyclerView.setAdapter(mAdapter);
                }
            });
        }

        @Override
        public void onFailure(Exception e) {
            e.printStackTrace();
        }
    };

}
