package com.normanou.bluegallery.ui;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.normanou.bluegallery.R;
import com.normanou.bluegallery.entity.TagEntity;
import com.normanou.bluegallery.network.RequestManager;
import com.normanou.bluegallery.protocol.TagProtocol;
import com.normanou.bluegallery.ui.adapter.TagAdapter;
import com.normanou.bluegallery.util.BLog;

import java.util.List;

public class TagActivity extends AppCompatActivity {

    private static final String TAG = "TagActivity";

    public static final String KEY_URL = "KEY_URL";
    public static final String KEY_TITLE = "KEY_TITLE";

    private String mUrl;
    private String mTitle;

    private ListView mListView;

    private View mLoadingView;

    private View mReloadView;

    private Button mReloadButton;

    private TagAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        mUrl = getIntent().getStringExtra(KEY_URL);
        mTitle = getIntent().getStringExtra(KEY_TITLE);

        initViews();
        loadFirstPage();
    }

    private void initViews() {
        setTitle(mTitle);
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new TagAdapter(this);

        mListView.setAdapter(mAdapter);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        final int screenHeight = size.y;

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
//                BLog.d(TAG, "firstVisibleItem is " + firstVisibleItem);
                int focusPos = -1;

                if (firstVisibleItem == 0) {
                    // if the list view is still on top, we focus on the first item
                    View childView = view.getChildAt(0);
                    if (childView != null && childView.getTop() >= 0) {
                        focusPos = 0;
                    }
                } else if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                    // if the list view is to the bottom, we focus on the bottom item
                    View childView = view.getChildAt(view.getChildCount() - 1);
                    if (childView != null && childView.getBottom() <= view.getHeight()) {
                        focusPos = firstVisibleItem + visibleItemCount - 1;
                    }
//                    BLog.d(TAG, "bottom pos " + childView.getTop() + ", " + childView.getBottom() + ", " + view.getHeight());
                }

                if (focusPos < 0) {
                    int middle = view.getHeight() / 5 * 2;
                    for (int i = 0; i < +visibleItemCount; i++) {
                        View childView = view.getChildAt(i);
                        if (childView != null) {
                            if (childView.getTop() <= middle && childView.getBottom() > middle) {
                                // if the item is in the middle of the list view
                                focusPos = i + firstVisibleItem;
                                break;
                            }
//                            BLog.d(TAG, "childView pos " + childView.getTop() + ", " + childView.getBottom() + ", " + view.getHeight());
                        }
                    }
                }

                mAdapter.updateFocusView(mListView, focusPos);
            }
        });

        mLoadingView = findViewById(R.id.loading);
        mReloadView = findViewById(R.id.retry);
        mReloadButton = (Button) mReloadView.findViewById(R.id.btn_reload);
        mReloadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showLoading();
                loadPage();
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

        RequestManager.cancelAll(TagActivity.this);
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
        mAdapter.clearData();
        loadPage();
    }

    private void loadPage() {
        BLog.d(TAG, "loading page 0");
        TagProtocol.getTags(mUrl, new Response.Listener<List<TagEntity>>() {

            @Override
            public void onResponse(List<TagEntity> response) {
                mAdapter.addData(response);
                showContent();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                showReload();

                Toast.makeText(TagActivity.this, R.string.msg_loading_failed,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }, TagActivity.this);
    }
}
