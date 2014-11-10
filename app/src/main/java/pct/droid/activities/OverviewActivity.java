package pct.droid.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.InjectView;
import pct.droid.R;
import pct.droid.adapters.OverviewGridAdapter;
import pct.droid.fragments.OverviewActivityTaskFragment;
import pct.droid.providers.media.MediaProvider;
import pct.droid.providers.media.YTSProvider;
import pct.droid.utils.PixelUtils;

public class OverviewActivity extends BaseActivity implements MediaProvider.Callback {

    private OverviewActivityTaskFragment mTaskFragment;
    private OverviewGridAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private Call mCall;
    private YTSProvider mProvider = new YTSProvider();
    private Integer mColumns = 2, mRetries = 0;
    private boolean mLoading = true, mEndOfListReached = false, mLoadingDetails = false;
    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount = 0, mLoadingTreshold = mColumns * 4, mPreviousTotal = 0;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.progressOverlay)
    LinearLayout progressOverlay;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.emptyView)
    TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_overview);
        setSupportActionBar(toolbar);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            toolbar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material) + PixelUtils.getStatusBarHeight(this)));
        } else {
            toolbar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material)));
        }

        recyclerView.setHasFixedSize(true);
        mColumns = getResources().getInteger(R.integer.overview_cols);
        mLayoutManager = new GridLayoutManager(this, mColumns);
        recyclerView.setLayoutManager(mLayoutManager);

        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (OverviewActivityTaskFragment) fm.findFragmentByTag(OverviewActivityTaskFragment.TAG);

        if (mTaskFragment == null || mTaskFragment.getExistingItems() == null) {
            mTaskFragment = new OverviewActivityTaskFragment();
            fm.beginTransaction().add(mTaskFragment, OverviewActivityTaskFragment.TAG).commit();

            mProvider.getList(mTaskFragment.getFilters(), mTaskFragment);
        } else {
            onSuccess(mTaskFragment.getExistingItems());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_overview, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchViewAction = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchViewAction.setQueryHint("Title, year, actors"); //TODO: translation
        searchViewAction.setOnQueryTextListener(mSearchListener);
        searchViewAction.setIconifiedByDefault(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if(mLoadingDetails) {
            progressOverlay.setVisibility(View.GONE);
            mCall.cancel();
            mLoadingDetails = false;
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onSuccess(final ArrayList<MediaProvider.Video> items) {
        mEndOfListReached = false;
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
                    emptyView.setVisibility(View.GONE);
                }
            });
            mLoading = false;
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.setItems(items);
                    emptyView.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onFailure(Exception e) {
        if(e.getMessage().equals(YTSProvider.NO_MOVIES_ERROR)) {
            mEndOfListReached = true;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.removeLoading();

                    if(mAdapter.getItemCount() <= 0) {
                        emptyView.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            e.printStackTrace();
            Log.e("OverviewActivity", e.getMessage());
            if (mRetries > 1) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(OverviewActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show(); //TODO: translation (by sv244)
                        emptyView.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                mProvider.getList(null, mTaskFragment);
            }
            mRetries++;
        }
    }

    private OverviewGridAdapter.OnItemClickListener mOnItemClickListener = new OverviewGridAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, final MediaProvider.Video item, int position) {
            progressOverlay.setBackgroundColor(getResources().getColor(R.color.overlay_bg));
            progressOverlay.setVisibility(View.VISIBLE);

            mLoadingDetails = true;
            mCall = mProvider.getDetail(item.imdbId, new MediaProvider.Callback() {
                @Override
                public void onSuccess(ArrayList<MediaProvider.Video> items) {
                    if (items.size() <= 0 || !mLoadingDetails) return;
                    mLoadingDetails = false;

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
                    if(!e.getMessage().equals("Canceled")) {
                        e.printStackTrace();
                        Log.e("OverviewActivity", e.getMessage());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(OverviewActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    mLoadingDetails = false;
                }
            });
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
                    mAdapter.removeLoading();
                }
            }

            if (!mEndOfListReached && !mLoading && (mTotalItemCount - mVisibleItemCount) <= (mFirstVisibleItem + mLoadingTreshold)) {
                mLoading = true;
                HashMap<String, String> filters = mTaskFragment.getFilters();
                filters.put("page", Integer.toString(mTaskFragment.getCurrentPage()));
                mProvider.getList(mAdapter.getItems(), filters, mTaskFragment);
                mTaskFragment.setFilters(filters);
                mAdapter.addLoading();
            }
        }
    };

    private SearchView.OnQueryTextListener mSearchListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            mEndOfListReached = false;
            if(mAdapter != null) {
                mAdapter.clearItems();
                mAdapter.addLoading();
            }
            HashMap<String, String> filters = mTaskFragment.getFilters();
            if(s.equals("")) {
                filters.remove("keywords");
            } else {
                filters.put("keywords", s);
            }
            filters.put("page", "1");
            mTaskFragment.setCurrentPage(1);
            mTaskFragment.setFilters(filters);
            mProvider.getList(filters, mTaskFragment);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            if(s.equals("")) {
                onQueryTextSubmit(s);
            }
            return false;
        }
    };
}
