package com.signmaps;

public class ExampleItem {
    private int mImageResource;
    private String mText1;

    public ExampleItem(int imageResource, String text1) {
        mImageResource = imageResource;
        mText1 = text1;
    }

    public String getPlace(){
        return mText1;
    }

    public int getImageResource() {
        return mImageResource;
    }
    public String getText1() {
        return mText1;
    }

}
