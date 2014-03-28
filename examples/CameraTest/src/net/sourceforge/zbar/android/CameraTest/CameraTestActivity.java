/*
 * Basic no frills app which integrates the ZBar barcode scanner with
 * the camera.
 * 
 * Created by lisah0 on 2012-02-24
 */
package net.sourceforge.zbar.android.CameraTest;

import java.util.ArrayList;

import net.sourceforge.zbar.android.CameraTest.CameraPreview;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;

import android.widget.TextView;
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
		TextView priceText;
	}

	ArrayList<ProductItem> productArray;
	private Context mContext;
	
	public ProductsListAdapter(Context context) {
		super();
		mContext = context;
		productArray = new ArrayList<ProductItem>();
	}
	
	public void addItem(ProductItem s) {
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
		holder.priceText = (TextView) view.findViewById(
				R.id.price);
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
		holder.barcodeText.setText(productArray.get(position).getProductName());
		holder.priceText.setText(String.format("%.2f", productArray.get(position).getProductPrice()));
		
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
	private DBStub dbStub;
	private ArrayList<String> products;

	TextView scanText;
	ImageView scanButton;
	TextView priceSumText;

	ImageScanner scanner;

	ListView productsListView;
	ProductsListAdapter productListAdapter;

	private boolean barcodeScanned = false;
	private boolean previewing = true;

	static {
		System.loadLibrary("iconv");
	} 

	private void refreshPriceSum() {
		Float sum = new Float(0);
		for (String item: products) {
			sum += dbStub.getProductById(item).getProductPrice();
		}
		priceSumText.setText(String.format("%.2f", sum) );
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
		priceSumText = (TextView)findViewById(R.id.priceSum);

		dbStub = new DBStub();
		products = new ArrayList<String>();

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
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//TODO: consider that, or maybe power management options is better idea.
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
				for (Symbol sym : syms) {//in final version it will be unable to scan more then one product in row 
					//                        scanText.setText("barcode result " + sym.getData());
					String scannedCode = sym.getData();
					ProductItem product = dbStub.getProductById(scannedCode);
					if (product != null) {
						products.add(scannedCode);
						productListAdapter.addItem(product);
						refreshPriceSum();
					}
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
