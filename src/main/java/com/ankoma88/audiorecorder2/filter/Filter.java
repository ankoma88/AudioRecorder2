package com.ankoma88.audiorecorder2.filter;

/** Modifies the data array */
public interface Filter {
    void filter(int length, int[] data);
}
