package com.store.cart;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.view.SurfaceHolder;

public class CameraController implements SurfaceHolder.Callback {

	private Handler mHandler;
	private Context mContext;
	private Camera mCamera;
	private ICameraListener mListener;
	private SurfaceHolder mSurfaceHolder;
	private boolean mStarted;
	
	public CameraController(Context context, SurfaceHolder holder) {
		mContext = context;
		mSurfaceHolder = holder;
		
		mCamera = getCameraInstance();
		mHandler = new Handler();
		mListener = null;
		
		mSurfaceHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e){
		}
		return c;
	}
	
	public void release() {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}
	
	public void setListener(ICameraListener listener) {
		mListener = listener;
	}
	
	public void stopCamera() {
		try {
            mCamera.stopPreview();
            mCamera.setPreviewDisplay(null);
            mCamera.setPreviewCallback(null);
            mCamera.autoFocus(null);
            
            mStarted = false;
            
            if(mListener != null) {
				mListener.onCameraStopped();
			}
        } catch (Exception e){
        }
	}

	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if(mCamera != null)
				mCamera.autoFocus(autoFocusCB);
		}
	};
	
	// Mimic continuous auto-focusing
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			mHandler.postDelayed(doAutoFocus, 1000);
		}
	};
	
	PreviewCallback previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();
			
			if(mListener != null) {
				mListener.onFrameCaptured(data, size.width, size.height);
			}

		}
	};
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int arg1, int arg2, int arg3) {
		mSurfaceHolder = holder;
		// stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.setPreviewCallback(previewCb);
            mCamera.startPreview();
            mCamera.autoFocus(autoFocusCB);
        } catch (Exception e){
        }
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mSurfaceHolder = holder;
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
		} catch (Exception e) {}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		stopCamera();
	}

}
