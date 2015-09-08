package com.ankoma88.audiorecorder2.filter;

/**Distortion effect*/
public class Distortion extends SingleSampleFilter {
    @Override
    protected int transform(int x) {
        // f(x) = x/|x| (1-e^(x^2/|x|))

        int abs_x = x & Integer.MAX_VALUE;

        if (abs_x == 0) {
            return x;
        }

        return (int) (x / abs_x * (1 - Math.exp(Math.pow(x, 2) / abs_x)));
    }
}
