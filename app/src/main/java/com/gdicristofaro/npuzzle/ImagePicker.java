package com.gdicristofaro.npuzzle;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

//adapted from http://www.javasrilankansupport.com/2012/05/android-listview-example-with-image-and.html
public class ImagePicker extends Activity {
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_picker);

        ArrayList<PictureInfo> image_details = GetImages();
        
        final ListView imagePickerView = (ListView) findViewById(R.id.imagepicker_listview);
        imagePickerView.setAdapter(new PictureInfoBaseAdapter(this, image_details));
        
        imagePickerView.setOnItemClickListener(new ClickListener(this, imagePickerView));
    }
    
	public void onPause() {
        //idea came from http://stackoverflow.com/questions/3389501/activity-transition-in-android
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		super.onPause();
	}
    
    private ArrayList<PictureInfo> GetImages(){
    	ArrayList<PictureInfo> results = new ArrayList<PictureInfo>();

    	results.add(new PictureInfo("The Violin", R.mipmap.violin));
    	results.add(new PictureInfo("Miles Davis", R.mipmap.miles));
    	results.add(new PictureInfo("The Piano", R.mipmap.piano));
    	results.add(new PictureInfo("Music Room", R.mipmap.musicclub));

    	return results;
    }
}



