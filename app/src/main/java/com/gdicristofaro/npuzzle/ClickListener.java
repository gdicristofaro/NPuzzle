package com.gdicristofaro.npuzzle;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class ClickListener implements AdapterView.OnItemClickListener {
    Context context;
    ListView listview;

    ClickListener(Context context, ListView listview)
    {
        this.context = context;
        this.listview = listview;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterview, View view, int position, long thelong) {
        Intent i = new Intent(context, NPuzzle.class);
        PictureInfo info = (PictureInfo) listview.getItemAtPosition(position);
        //inspiration from: http://stackoverflow.com/questions/2736389/how-to-pass-object-from-one-activity-to-another-in-android
        i.putExtra("imageNum", info.getImageNumber());
        context.startActivity(i);
    }
}



