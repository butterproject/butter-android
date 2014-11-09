package pct.droid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.InjectView;
import pct.droid.R;
import pct.droid.providers.media.MediaProvider;
import pct.droid.providers.media.YTSProvider;
import pct.droid.adapters.OverviewGridAdapter;
import pct.droid.utils.LogUtils;

public class OverviewActivity extends BaseActivity {

    private OverviewGridAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private YTSProvider mProvider = new YTSProvider();
    private HashMap<String, String> mFilters = new HashMap<String, String>();
    private Integer mColumns = 2, mRetries = 0, mPage = 1;
    private boolean mLoading = true;
    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount = 0, mLoadingTreshold = mColumns * 4, mPreviousTotal = 0;

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
        mColumns = getResources().getInteger(R.integer.overview_cols);
        mLayoutManager = new GridLayoutManager(this, mColumns);
        recyclerView.setLayoutManager(mLayoutManager);

        mProvider.getList(null, mCallback);
        mPage++;
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

                    final MediaProvider.Video item = items.get(0);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(OverviewActivity.this, MovieDetailActivity.class);
                            intent.putExtra("item", item);
                            startActivity(intent);
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    Log.e("OverviewActivity", e.getMessage());
                    Toast.makeText(OverviewActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private MediaProvider.Callback mCallback = new MediaProvider.Callback() {
        @Override
        public void onSuccess(final ArrayList<MediaProvider.Video> items) {
            if(mTotalItemCount <= 0) {
                mAdapter = new OverviewGridAdapter(OverviewActivity.this, items, mColumns);
                mAdapter.setOnItemClickListener(mOnItemClickListener);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressOverlay.setVisibility(View.GONE);
                        recyclerView.setAdapter(mAdapter);
                        recyclerView.setOnScrollListener(mScrollListener);
                        mPreviousTotal = mTotalItemCount = mAdapter.getItemCount();
                    }
                });
                mLoading = false;
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setItems(items);
                    }
                });
            }
        }

        @Override
        public void onFailure(Exception e) {
            e.printStackTrace();
            Log.e("OverviewActivity", e.getMessage());
            if(mRetries > 1) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(OverviewActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show(); //TODO: translation (by sv244)
                    }
                });
            } else {
                mProvider.getList(null, mCallback);
            }
            mRetries++;
        }
    };

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            mVisibleItemCount = mLayoutManager.getChildCount();
            mTotalItemCount = mLayoutManager.getItemCount();
            mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

            if (mLoading) {
                if (mTotalItemCount > mPreviousTotal) {
                    mLoading = false;
                    mPreviousTotal = mTotalItemCount;
                    mPage++;
                }
            }

            if (!mLoading && (mTotalItemCount - mVisibleItemCount) <= (mFirstVisibleItem + mLoadingTreshold)) {
                mLoading = true;
                mFilters.put("page", Integer.toString(mPage));
                mProvider.getList(mAdapter.getItems(), mFilters, mCallback);
            }
        }
    };

}
