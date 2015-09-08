package com.ankoma88.audiorecorder2.filter;

/** Abstract Filter (effect) class*/
public abstract class SingleSampleFilter implements Filter {
    @Override
    public void filter(int length, int[] data) {
        for (int i = 0; i < length; i++) {
            data[i] = transform(data[i]);
        }
    }

    protected abstract int transform(int x);
}
