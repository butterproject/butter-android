package pct.droid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

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
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_overview);
        setSupportActionBar(toolbar);

        mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);

        mProvider.getList(null, mCallback);
    }

    private OverviewGridAdapter.OnItemClickListener mOnItemClickListener = new OverviewGridAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, MediaProvider.Video item, int position) {
            Intent intent = new Intent(view.getContext(), MovieDetailActivity.class);
            intent.putExtra("item", item);
            view.getContext().startActivity(intent);
        }
    };

    private MediaProvider.Callback mCallback = new MediaProvider.Callback() {
        @Override
        public void onSuccess(ArrayList<MediaProvider.Video> items) {
            mAdapter = new OverviewGridAdapter(items);
            mAdapter.setOnItemClickListener(mOnItemClickListener);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    recyclerView.setAdapter(mAdapter);
                }
            });
        }

        @Override
        public void onFailure(Exception exception) {

        }
    };

}
