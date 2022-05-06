package com.example.pictureperfect;

import android.widget.ImageView;

import java.util.ArrayList;

public class MainActivityPresenter {

    private View view;
    private ImageView imageView;
    private PixelAnalyser pixelAnalyser;
    private ArrayList<RGB> rgbArrayList;

    public MainActivityPresenter(View view, ImageView imageView)
    {
        this.view = view;
        this.imageView = imageView;
        pixelAnalyser = new PixelAnalyser(imageView);
        pixelAnalyser.start();
    }

    public void set()
    {
        rgbArrayList = pixelAnalyser.getRgbGroup();
        view.topRgb(rgbArrayList);
    }

    public interface View
    {
        public void topRgb(ArrayList<RGB> rgbArrayList);
    }

}
