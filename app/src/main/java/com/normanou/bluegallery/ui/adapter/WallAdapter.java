package com.normanou.bluegallery.ui.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.normanou.bluegallery.R;
import com.normanou.bluegallery.entity.WallEntity;
import com.normanou.bluegallery.network.RequestManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bluewinter on 20/11/2016.
 */
public class WallAdapter extends BaseAdapter {

    private List<WallEntity> mData;

    private LayoutInflater mLayoutInflater;

    private Drawable mLoadingImageDrawable;// = new
    // ColorDrawable(Color.argb(255, 201,
    // 201, 201));

    private Drawable mFailedImageDrawable;

    private OnWallClickListener mOnWallClickListener;

    public WallAdapter(Activity activity, OnWallClickListener listener) {
        mLayoutInflater = activity.getLayoutInflater();
        mData = new ArrayList<WallEntity>();

        mLoadingImageDrawable = activity.getResources().getDrawable(R.drawable.loading);
        mFailedImageDrawable = activity.getResources().getDrawable(R.drawable.loading_fail);

        mOnWallClickListener = listener;
    }

    public void addData(List<WallEntity> data) {
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void clearData() {
        mData.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mData == null || mData.size() == 0) {
            return 0;
        }

        return (mData.size() - 1) / 2 + 1;
    }

    @Override
    public Pair<WallEntity, WallEntity> getItem(int position) {
        if (mData == null || position < 0 || position >= mData.size()) {
            return null;
        }

        WallEntity itemLeft = mData.size() > position * 2 ? mData.get(position * 2) : null;
        WallEntity itemRight = mData.size() > position * 2 + 1 ? mData.get(position * 2 + 1) : null;

        return new Pair<>(itemLeft, itemRight);
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
            view = mLayoutInflater.inflate(R.layout.item_wall, null);
            holder = new Holder(view);
            view.setTag(holder);
        }

        if (holder.imageRequestLeft != null) {
            holder.imageRequestLeft.cancelRequest();
        }

        if (holder.imageRequestRight != null) {
            holder.imageRequestRight.cancelRequest();
        }

        Pair<WallEntity, WallEntity> item = getItem(position);
        if (item.first != null) {
            holder.imageRequestLeft = RequestManager
                    .loadImage(item.first.imgUrl, holder.imageLeft, mLoadingImageDrawable,
                            mFailedImageDrawable);
            holder.imageLeft.setTag(position * 2);
            holder.imageLeft.setClickable(true);
            holder.titleLeft.setText(item.first.name);
        } else {
            holder.imageLeft.setClickable(false);
            holder.titleLeft.setText("");
        }

        if (item.second != null) {
            holder.imageRequestRight = RequestManager
                    .loadImage(item.second.imgUrl, holder.imageRight, mLoadingImageDrawable,
                            mFailedImageDrawable);
            holder.imageRight.setTag(position * 2 + 1);
            holder.imageRight.setClickable(true);
            holder.titleRight.setText(item.second.name);
        } else {
            holder.imageRight.setClickable(false);
            holder.titleRight.setText("");
        }

        return view;
    }

    private class Holder {
        public ImageView imageLeft;
        public ImageView imageRight;
        public TextView titleLeft;
        public TextView titleRight;

        public ImageLoader.ImageContainer imageRequestLeft;
        public ImageLoader.ImageContainer imageRequestRight;

        public Holder(View view) {
            imageLeft = (ImageView) view.findViewById(R.id.imageLeft);
            imageRight = (ImageView) view.findViewById(R.id.imageRight);
            titleLeft = (TextView) view.findViewById(R.id.titleLeft);
            titleRight = (TextView) view.findViewById(R.id.titleRight);

            imageLeft.setOnClickListener(mOnViewClickListener);
            imageRight.setOnClickListener(mOnViewClickListener);
        }
    }

    private View.OnClickListener mOnViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnWallClickListener != null) {
                Object tag = v.getTag();
                if (tag != null && tag instanceof Integer) {
                    mOnWallClickListener.onWallClick(mData.get((Integer) tag));
                }
            }
        }
    };

    public static interface OnWallClickListener {
        public void onWallClick(WallEntity entity);
    }
}
