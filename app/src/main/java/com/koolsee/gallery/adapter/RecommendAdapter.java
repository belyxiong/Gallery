package com.koolsee.gallery.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageView.ScaleType;

import com.koolsee.gallery.R;
import com.koolsee.gallery.model.Recommend;
import com.koolsee.gallery.widget.BorderImageView;
import com.koolsee.gallery.widget.GalleryFlow;

/**
 * 首页推荐位适配器
 * @author zengxiaotao
 */
public class RecommendAdapter extends BaseAdapter {

    private Context mContext;

    private LayoutInflater mInflater = null;

    private int mSize;

    private List<Recommend> mRecommends;

    private Map<Integer, BorderImageView> mChildren;

    private final Object mLock = new Object();

    public RecommendAdapter(Context context, List<Recommend> recommends) {
        this.mContext = context;
        this.mRecommends = recommends;
        this.mInflater = LayoutInflater.from(context);
        this.mSize = mRecommends.size();
        this.mChildren = new HashMap<Integer, BorderImageView>();
    }

    @Override
    public int getCount() {
        if (mSize < GalleryFlow.MIN_CYCLE_NUMS) {
            return mSize;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public Object getItem(int position) {
        if (mSize == 0) {
            return null;
        }
        position = position % mSize;
        return this.mRecommends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        position = position % mSize;
        final int pos = position;
        BorderImageView imageView = this.getChildAt(position);
        if (imageView == null) {

            imageView = (BorderImageView) this.mInflater.inflate(R.layout.recommend_item, null);

            Recommend recommend = mRecommends.get(position);
            int imgRes = recommend.getRes();
            imageView.setImageResource(imgRes);
            imageView.setScaleType(ScaleType.FIT_CENTER);
            //TODO FIX density
            DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
            float density = dm.density;
            //TODO 这里必须重新初始化图片比例，宽度初始化为398确保控件能展示5张图片
            int[] params = mContext.getResources().getIntArray(R.array.gallery_img_layout_params);
            imageView.setLayoutParams(new LayoutParams(Math.round(params[0] * density), Math
                    .round(params[1] * density)));

            synchronized (this.mLock) {
                if (!this.mChildren.containsKey(position)) {
                    this.mChildren.put(position, imageView);
                }
            }

        }
        return imageView;
    }

    public BorderImageView getChildAt(int position) {
        synchronized (this.mLock) {
            return this.mChildren.get(position);
        }
    }
}
