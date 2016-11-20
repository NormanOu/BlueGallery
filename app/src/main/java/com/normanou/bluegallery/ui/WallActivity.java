package com.normanou.bluegallery.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.normanou.bluegallery.R;
import com.normanou.bluegallery.entity.WallEntity;
import com.normanou.bluegallery.network.RequestManager;
import com.normanou.bluegallery.protocol.WallProtocol;
import com.normanou.bluegallery.ui.adapter.WallAdapter;
import com.normanou.bluegallery.util.BLog;
import com.normanou.bluegallery.widgets.LoadingFooter;

import java.util.List;

public class WallActivity extends AppCompatActivity {

    private static final String TAG = "WallActivity";

    private ListView mListView;

    private View mLoadingView;

    private View mReloadView;

    private Button mReloadButton;

    private WallAdapter mAdapter;

    private LoadingFooter mLoadingFooter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall);

        initViews();
        loadFirstPage();
    }

    private void initViews() {
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new WallAdapter(this, new WallAdapter.OnWallClickListener() {
            @Override
            public void onWallClick(WallEntity entity) {
                // jump to detail activity
                if (entity != null && !TextUtils.isEmpty(entity.detailUrl) && !TextUtils.isEmpty(entity.name)) {
                    Intent intent = new Intent(WallActivity.this, TagActivity.class);
                    intent.putExtra(TagActivity.KEY_TITLE, entity.name);
                    intent.putExtra(TagActivity.KEY_URL, entity.detailUrl);
                    startActivity(intent);
                }
            }
        });
        mLoadingFooter = new LoadingFooter(this, new LoadingFooter.ReloadListener() {

            @Override
            public void onReload() {
                loadPage(mPage);
            }
        });

        mListView.addFooterView(mLoadingFooter.getView());
        mListView.setAdapter(mAdapter);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                if (mLoadingFooter.getState() != LoadingFooter.State.Idle) {
                    return;
                }

                if (firstVisibleItem + visibleItemCount >= totalItemCount - 3
                        && totalItemCount != 0
                        && totalItemCount != mListView.getHeaderViewsCount()
                        + mListView.getFooterViewsCount() && mAdapter.getCount() > 0) {
                    loadNextPage();
                }
            }
        });

        mLoadingView = findViewById(R.id.loading);
        mReloadView = findViewById(R.id.retry);
        mReloadButton = (Button) mReloadView.findViewById(R.id.btn_reload);
        mReloadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showLoading();
                loadPage(mPage);
            }
        });

        showLoading();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        RequestManager.cancelAll(WallActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showContent() {
        mLoadingView.setVisibility(View.GONE);
        mReloadView.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
        mReloadView.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
    }

    private void showReload() {
        mLoadingView.setVisibility(View.GONE);
        mReloadView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
    }

    private void loadFirstPage() {
        mPage = 0;
        mAdapter.clearData();
        loadPage(mPage);
    }

    private void loadNextPage() {
        loadPage(mPage);
    }

    private int mPage = 0;

    private void loadPage(int page) {
        BLog.d(TAG, "loading page " + mPage);
        mLoadingFooter.setState(LoadingFooter.State.Loading);
        WallProtocol.getWall(new Response.Listener<List<WallEntity>>() {

            @Override
            public void onResponse(List<WallEntity> response) {
                mAdapter.addData(response);
                mPage++;
                if (response.size() != 0) {
                    mLoadingFooter.setState(LoadingFooter.State.Idle);
                } else {
                    mLoadingFooter.setState(LoadingFooter.State.TheEnd);
                }

                showContent();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (mPage == 0) {
                    showReload();
                } else {
                    mLoadingFooter.setState(LoadingFooter.State.Error);
                }
                Toast.makeText(WallActivity.this, R.string.msg_loading_failed,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }, WallActivity.this);
    }

}
