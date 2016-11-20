package com.normanou.bluegallery.ui.adapter;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.normanou.bluegallery.R;
import com.normanou.bluegallery.entity.TagEntity;
import com.normanou.bluegallery.network.RequestManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bluewinter on 20/11/2016.
 */
public class TagAdapter extends BaseAdapter {

    private List<TagEntity> mData;

    private LayoutInflater mLayoutInflater;

    private Drawable mLoadingImageDrawable;

    private Drawable mFailedImageDrawable;

    private int mScreenWidth;
    private int mScreenHeight;

    private int mFocusPos = 0;

    public TagAdapter(Activity activity) {
        mLayoutInflater = activity.getLayoutInflater();
        mData = new ArrayList<TagEntity>();

        mLoadingImageDrawable = activity.getResources().getDrawable(R.drawable.loading);
        mFailedImageDrawable = activity.getResources().getDrawable(R.drawable.loading_fail);

        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        mScreenWidth = size.x;
        mScreenHeight = size.y;
    }

    public void setFocusPosition(int pos) {
        if (pos != mFocusPos) {
            mFocusPos = pos;
            notifyDataSetChanged();
        }
    }

    public void addData(List<TagEntity> data) {
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void clearData() {
        mData.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mData == null) {
            return 0;
        }

        return mData.size();
    }

    @Override
    public TagEntity getItem(int position) {
        if (mData == null || position < 0 || position >= mData.size()) {
            return null;
        }

        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Holder holder;
        if (view != null && view.getTag() != null) {
            holder = (Holder) convertView.getTag();
        } else {
            view = mLayoutInflater.inflate(R.layout.item_tag, null);
            holder = new Holder(view);
            view.setTag(holder);
        }

        if (holder.imageRequest != null) {
            holder.imageRequest.cancelRequest();
        }

        TagEntity item = getItem(position);
        if (item != null) {

            holder.imageRequest = RequestManager
                    .loadImage(item.imgUrl, RequestManager
                            .getImageListener(holder.image, mLoadingImageDrawable,
                                    mFailedImageDrawable, true), mScreenWidth / 2, mScreenHeight * 2);
            holder.image.setTag(position * 2);
            holder.title.setText(item.msg);

            if (mFocusPos == position) {
                holder.imgCover.setVisibility(View.GONE);
            } else {
                holder.imgCover.setVisibility(View.VISIBLE);
            }
        } else {
            holder.title.setText("");
        }

        return view;
    }

    private class Holder {
        public ImageView image;
        public TextView title;
        public View imgCover;

        public ImageLoader.ImageContainer imageRequest;

        public Holder(View view) {
            image = (ImageView) view.findViewById(R.id.image);
            title = (TextView) view.findViewById(R.id.title);
            imgCover = view.findViewById(R.id.img_cover);
        }
    }
}
