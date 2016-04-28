package com.gdicristofaro.npuzzle;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

/*
 * ThumbsUp.java
 * A second activity in Intents06.
 */

public class YouWin extends Activity implements OnClickListener {
	
	public void onCreate(Bundle savedInstanceState) {
		// call the parent's onCreate() method.
        super.onCreate(savedInstanceState);
        if (this.getIntent().getBooleanExtra("win", false))
        	setContentView(getWinView());
        else
        	setContentView(getPreview());
    }
	
	private View getWinView() {
       //based on this layout
       FrameLayout frameLayout = new FrameLayout(this);
        	ImageView image = new ImageView(this);
    			int imgID = getIntent().getIntExtra("imageNum", 0);
    			int maxDim = getMaxDimension(this);
    			Bitmap bitmap = loadImage(this, imgID, maxDim, maxDim);
    			image.setImageBitmap(bitmap);
    		frameLayout.addView(image);
    		TextView txt = new TextView(this);
        		int numMoves = getIntent().getIntExtra("numMoves", 0);
    			String thetext = "Congratulations!\nYou won in " + numMoves + " moves.";
    			txt.setText(thetext);
    			txt.setBackgroundResource(R.drawable.gradient);
    			txt.setTextSize(17);
    			
    			//derived from https://groups.google.com/forum/?fromgroups=#!topic/android-developers/bhERcwu0cyc
    			txt.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    			txt.setTextColor(0xFFFFFFFF);
    			txt.setPadding(40, 20, 40, 20);
    			//taken from here: http://stackoverflow.com/questions/3775705/android-set-the-gravity-for-a-textview-programmatically
    			txt.setGravity(Gravity.CENTER);
    			LayoutParams txtlayout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    				txtlayout.topMargin = 20;
    				txtlayout.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
				txt.setLayoutParams(txtlayout);
			frameLayout.addView(txt);
			Button ok = new Button(this);
				ok.setText("Play Again");
				LayoutParams okLayout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					okLayout.bottomMargin = 20;
					okLayout.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
					okLayout.setMargins(15, 10, 15, 10);
				ok.setLayoutParams(okLayout);
				ok.setOnClickListener(this);
			frameLayout.addView(ok);
		return frameLayout;
	}
	
	private View getPreview() {
        //based on this layout
        FrameLayout frameLayout = new FrameLayout(this);
        	ImageView image = new ImageView(this);
    			int imgID = getIntent().getIntExtra("imageNum", 0);
    			int maxDim = getMaxDimension(this);
    			Bitmap bitmap = loadImage(this, imgID, maxDim, maxDim);
    			image.setImageBitmap(bitmap);
    		frameLayout.addView(image);
    		TextView txt = new TextView(this);
    			txt.setText("Tap to Return");
    			txt.setTextColor(0xFFFFFFFF);
    			txt.setBackgroundResource(R.drawable.gradient);

    			txt.setPadding(15, 15, 15, 15);
    			//taken from here: http://stackoverflow.com/questions/3775705/android-set-the-gravity-for-a-textview-programmatically
    			txt.setGravity(Gravity.CENTER);
    			LayoutParams txtlayout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    				txtlayout.bottomMargin = 20;
    				txtlayout.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
				txt.setLayoutParams(txtlayout);
			frameLayout.addView(txt);
			frameLayout.setOnClickListener(this);
			return frameLayout;
	}
	
	// get display metrics taken from: http://stackoverflow.com/questions/1016896/android-how-to-get-screen-dimensions
    private int getMaxDimension(Activity activity) {
    	DisplayMetrics metrics = new DisplayMetrics();
    	activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return Math.max(metrics.widthPixels, metrics.heightPixels);
    }
	
    
	//based on android source here: http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
	private Bitmap loadImage(Activity activity, int ResourceID, int MaxWidth, int MaxHeight) {		
		//get image proportions without loading
		BitmapFactory.Options bitmapcheck = new BitmapFactory.Options();
		bitmapcheck.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(activity.getResources(), ResourceID, bitmapcheck);
		// Raw height and width of image
		int imageHeight = bitmapcheck.outHeight;
		int imageWidth = bitmapcheck.outWidth;
		
		
	    // determine options
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(activity.getResources(), ResourceID, options);

	    if (imageHeight > MaxHeight || imageWidth > MaxWidth) {
	        if (imageHeight > imageWidth) {
	        	options.inSampleSize = Math.round((float)imageHeight / (float)MaxHeight);
	        } else {
	        	options.inSampleSize = Math.round((float)imageWidth / (float)MaxWidth);
	        }
	    }

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(activity.getResources(), ResourceID, options);
	}

	public void onPause() {
        //idea came from http://stackoverflow.com/questions/3389501/activity-transition-in-android
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		super.onPause();
	}

	public void onClick(View v) {
		finish();
	}

}

