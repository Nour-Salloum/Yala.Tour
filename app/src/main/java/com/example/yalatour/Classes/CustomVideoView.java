package com.example.yalatour.Classes;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.VideoView;

public class CustomVideoView extends VideoView {
    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Adjust the aspect ratio here as per your requirement
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = width * 1 / 1;

        setMeasuredDimension(width, height);
    }
    @Override
    public void setVideoURI(Uri uri) {
        super.setVideoURI(uri);
        // Ensure the video starts from the beginning
        seekTo(0);
    }

    @Override
    public void pause() {
        super.pause();
        // Ensure the video remains at the first frame
        seekTo(0);
    }
}
