package com.github.rossdanderson.backlight.jni.screen.dxgi;

public class DXGICapture {

    public static class Dimensions {
        public int top;
        public int bottom;
        public int left;
        public int right;
    }

    public native void initialise();

    public native Dimensions getDimensions();

    public native byte[] capture();
}
