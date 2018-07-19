package com.uber.motif.sample.app.photo_list_item;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.uber.motif.sample.R;
import com.uber.motif.sample.lib.db.Photo;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoListItemView extends FrameLayout {

    @BindView(R.id.image)
    ImageView imageView;

    public PhotoListItemView(@NonNull Context context) {
        this(context, null);
    }

    public PhotoListItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoListItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPhoto(Photo photo) {
        Glide.with(this)
                .load(photo.location)
                .thumbnail(0.1f)
                .into(imageView);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public static PhotoListItemView create(ViewGroup parent) {
        return (PhotoListItemView) LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_list_item, parent, false);
    }
}