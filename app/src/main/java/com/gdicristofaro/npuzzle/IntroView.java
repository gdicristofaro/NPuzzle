package com.gdicristofaro.npuzzle;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class IntroView extends FrameLayout {
    final NPuzzle activity;

    IntroView(NPuzzle activity, int IMGresource)
    {
        //do basic set up and hold variables
        super(activity);
        this.activity = activity;

        setView(IMGresource);
        countdown(3);
    }

    class scaleAnimList implements Animation.AnimationListener {
        final int counter;
        final ImageView view;
        final FrameLayout parent;

        scaleAnimList(FrameLayout parent, ImageView view, int counter) {
            this.parent = parent;
            this.view = view;
            this.counter = counter;
        }

        public void onAnimationEnd(Animation arg0) {
            parent.removeView(view);
            countdown(counter - 1);
        }

        public void onAnimationRepeat(Animation arg0) {}
        public void onAnimationStart(Animation arg0) {}

    }


    //triggered after layout: do a countdown
    private void countdown(int counter) {
        ImageView number = new ImageView(activity);

        Drawable numImage = null;
        switch (counter)
        {
            case 3:
                numImage = getResources().getDrawable(R.drawable.countdown3, null);
                break;
            case 2:
                numImage = getResources().getDrawable(R.drawable.countdown2, null);
                break;
            case 1:
                numImage = getResources().getDrawable(R.drawable.countdown1, null);
                break;
            case 0:
                getToGame();
                return;
        }
        number.setImageDrawable(numImage);

        this.addView(number);

        //for animating to center: http://stackoverflow.com/questions/7414065/android-scale-animation-on-view
        ScaleAnimation scalin = new ScaleAnimation(
                (float)1.5, 0,
                (float)1.5, 0,
                Animation.RELATIVE_TO_SELF, (float)0.5, Animation.RELATIVE_TO_SELF, (float)0.5);
        scalin.setAnimationListener(new scaleAnimList(this, number, counter));
        scalin.setDuration(1000);

        number.startAnimation(scalin);
    }

    //put the picture in view
    private void setView(int iMGresource) {
        ImageView image = new ImageView(activity);
        int maxSize = activity.getMaxDimension(activity);
        Bitmap bitmap = activity.loadImage(activity, iMGresource, maxSize, maxSize);
        image.setImageBitmap(bitmap);

        this.addView(image);
    }

    //do the logic to actually start up the game when done with animation
    private void getToGame() {
        this.removeAllViews();

        //if setting up images doesn't cause problems...
        if (activity.board.setupImageAndPieces())
        {
            activity.setContentView(activity.board);
            activity.board.mixup(activity.board.BoardSize * 15);
        }
    }
}


