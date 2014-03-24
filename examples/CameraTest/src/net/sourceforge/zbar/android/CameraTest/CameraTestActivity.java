/*
 * Basic no frills app which integrates the ZBar barcode scanner with
 * the camera.
 * 
 * Created by lisah0 on 2012-02-24
 */
package net.sourceforge.zbar.android.CameraTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sourceforge.zbar.android.CameraTest.CameraPreview;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;

import android.widget.TextView;
import android.graphics.ImageFormat;

/* Import ZBar Class files */
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;

class ProductsListAdapter extends BaseAdapter {
	
	private static class ViewHolder {
		TextView lpText;
		TextView barcodeText;
	}

	ArrayList<String> productArray;
	private Context mContext;
	
	public ProductsListAdapter(Context context) {
		super();
		mContext = context;
		productArray = new ArrayList<String>();
	}
	
	public void addItem(String s) {
		productArray.add(s);
	}

	@Override
	public int getCount() {
		return productArray.size();
	}

	@Override
	public Object getItem(int arg0) {
		return productArray.get(arg0);
	}
	
	private View createView(ViewHolder holder) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.product_item, null);
		holder.lpText = (TextView) view.findViewById(
				R.id.lp_text);
		holder.barcodeText = (TextView) view.findViewById(
				R.id.barcode_string);
		view.setTag(holder);
		return view;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView != null) {
			holder = (ViewHolder) convertView.getTag();
		}
		else {
			holder = new ViewHolder();
			convertView = createView(holder);
		}
		
		holder.lpText.setText("" + (position + 1));
		holder.barcodeText.setText(productArray.get(position));
		
		return convertView;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

}

public class CameraTestActivity extends Activity
{
	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;

	TextView scanText;
	ImageView scanButton;

	ImageScanner scanner;

	ListView productsListView;
	ProductsListAdapter productListAdapter;

	private boolean barcodeScanned = false;
	private boolean previewing = true;

	static {
		System.loadLibrary("iconv");
	} 

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main2);

		autoFocusHandler = new Handler();
		mCamera = getCameraInstance();

		/* Instance barcode scanner */
		scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 3);
		scanner.setConfig(0, Config.Y_DENSITY, 3);

		mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
		LinearLayout preview = (LinearLayout)findViewById(R.id.cameraPreview);
		preview.addView(mPreview);

		//        scanText = (TextView)findViewById(R.id.scanText);

		scanButton = (ImageView)findViewById(R.id.ScanImage);


		productsListView = (ListView) findViewById(R.id.listView1);

		productListAdapter = new ProductsListAdapter(this);
		productsListView.setAdapter(productListAdapter);

		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (barcodeScanned) {
					barcodeScanned = false;
					//                        scanText.setText("Scanning...");
					mCamera.setPreviewCallback(previewCb);
					mCamera.startPreview();
					previewing = true;
					mCamera.autoFocus(autoFocusCB);
				}
			}
		});
	}

	public void onPause() {
		super.onPause();
		releaseCamera();
	}
	
	private void playScannedNotification() {
		try {
			new Thread(){
	            public void run(){
	            	MediaPlayer mp = MediaPlayer.create(CameraTestActivity.this, R.raw.beep);   
	                 mp.start();
	            }
	        }.start();
		} catch (Exception e) {
		    e.printStackTrace();
		}
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

	private void releaseCamera() {
		if (mCamera != null) {
			previewing = false;
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (previewing)
				mCamera.autoFocus(autoFocusCB);
		}
	};

	PreviewCallback previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();

			Image barcode = new Image(size.width, size.height, "Y800");
			barcode.setData(data);

			int result = scanner.scanImage(barcode);

			if (result != 0) {
				previewing = false;
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();

				SymbolSet syms = scanner.getResults();
				for (Symbol sym : syms) {
					//                        scanText.setText("barcode result " + sym.getData());
					productListAdapter.addItem(sym.getData());
					productListAdapter.notifyDataSetChanged();
					barcodeScanned = true;
					playScannedNotification();
				}
			}
		}
	};

	// Mimic continuous auto-focusing
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};
}
