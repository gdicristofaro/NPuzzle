package com.gdicristofaro.npuzzle;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class PictureInfoBaseAdapter extends BaseAdapter {
    private static ArrayList<PictureInfo> PictureInfoList;
    private LayoutInflater l_Inflater;
    int maxSize;
    Activity activity;

    public PictureInfoBaseAdapter(Activity activity, ArrayList<PictureInfo> results) {
        PictureInfoList = results;
        l_Inflater = LayoutInflater.from(activity);
        maxSize = getMaxDimension(activity) / 6;
        this.activity = activity;
    }

    public int getCount() {
        return PictureInfoList.size();
    }

    public Object getItem(int position) {
        return PictureInfoList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = l_Inflater.inflate(R.layout.image_picker_list_item, null);
            holder = new ViewHolder();
            holder.Name = (TextView) convertView.findViewById(R.id.name);
            holder.Image = (ImageView) convertView.findViewById(R.id.image);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.Name.setText(PictureInfoList.get(position).getName());
        holder.Image.setImageBitmap(loadImage(activity, PictureInfoList.get(position).getImageNumber(), maxSize, maxSize));
        //holder.Image.setImageResource(PictureInfoList.get(position).getImageNumber());

        return convertView;
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

    static class ViewHolder {
        ImageView Image;
        TextView Name;
    }
}
