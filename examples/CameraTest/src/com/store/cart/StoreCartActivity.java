package com.store.cart;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import android.widget.TextView;

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

public class StoreCartActivity extends Activity implements IBarcodeScannerListener {
	
	private CameraController mCameraController;
	private BarcodeScanner mBarcodeScanner;
	
	private DBStub dbStub;
	private ArrayList<String> products;

	private SurfaceView mCameraPreview;
	private TextView mScanText;
	private ImageView mScanButton;
	private TextView mPriceSumText;

	ListView productsListView;
	ProductsListAdapter productListAdapter;

	private void refreshPriceSum() {
		Float sum = new Float(0);
		for (String item: products) {
			sum += dbStub.getProductById(item).getProductPrice();
		}
		mPriceSumText.setText(String.format("%.2f", sum) );
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		mCameraPreview = (SurfaceView)findViewById(R.id.cameraPreview);
		mScanButton = (ImageView)findViewById(R.id.ScanImage);
		mPriceSumText = (TextView)findViewById(R.id.priceSum);
		
		
		mScanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO ?
			}
		});
		
		mBarcodeScanner = new BarcodeScanner();
		mBarcodeScanner.setListener(this);
		
		mCameraController = new CameraController(this, mCameraPreview.getHolder());
		mCameraController.setListener(mBarcodeScanner);

		dbStub = new DBStub();
		products = new ArrayList<String>();

		productsListView = (ListView) findViewById(R.id.listView1);

		productListAdapter = new ProductsListAdapter(this);
		productsListView.setAdapter(productListAdapter);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//TODO: dodac sterowanie stanem proximity sensor?
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mCameraController.stopCamera();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		mCameraController.release();
		mCameraController = null;
		
		mBarcodeScanner.release();
		mBarcodeScanner = null;
	}
	
	@Override
	public void onBarcodeScanned(String barcode) {
		ProductItem product = dbStub.getProductById(barcode);
		if (product != null) {
			products.add(barcode);
			productListAdapter.addItem(product);
			refreshPriceSum();
		}
		productListAdapter.notifyDataSetChanged();
		playScannedNotification();
	}
	
	private void playScannedNotification() {
		try {
			new Thread(){
	            public void run(){
	            	MediaPlayer mp = MediaPlayer.create(StoreCartActivity.this, R.raw.beep);   
	                 mp.start();
	            }
	        }.start();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
}
