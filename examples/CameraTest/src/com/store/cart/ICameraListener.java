package com.store.cart;

public interface ICameraListener {
	
	void onCameraStarted();
	void onCameraStopped();
	void onFrameCaptured(byte[] frame, int width, int height);

}
