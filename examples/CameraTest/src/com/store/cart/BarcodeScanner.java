package com.store.cart;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

public class BarcodeScanner implements ICameraListener {
	
	static {
		System.loadLibrary("iconv");
	} 
	
	ImageScanner mScanner;
	IBarcodeScannerListener mListener;
	
	public BarcodeScanner() {
		mScanner = new ImageScanner();
		mScanner.setConfig(0, Config.X_DENSITY, 3);
		mScanner.setConfig(0, Config.Y_DENSITY, 3);
	}
	
	public void setListener(IBarcodeScannerListener listener) {
		mListener = listener;
	}

	@Override
	public void onCameraStarted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCameraStopped() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFrameCaptured(byte[] frame, int width, int height) {
		
		Image barcode = new Image(width, height, "Y800");
		barcode.setData(frame);

		int result = mScanner.scanImage(barcode);

		if (result != 0) {
			SymbolSet syms = mScanner.getResults();
			for (Symbol sym : syms) {
				String scannedCode = sym.getData();
				if(scannedCode != null && mListener != null){
					mListener.onBarcodeScanned(scannedCode);
				}
			}
		}
	}

	public void release() {
		mScanner = null;
		mListener = null;
	}

}
