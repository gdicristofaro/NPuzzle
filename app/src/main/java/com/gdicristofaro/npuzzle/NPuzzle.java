package com.gdicristofaro.npuzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import com.gdicristofaro.npuzzle.NPuzzleBoard.movement;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;


public class NPuzzle extends Activity {
	final int defaultImg = R.mipmap.miles;
	
	NPuzzleBoard board = null;
	int imgResource = defaultImg;
	
	//if there is an error; this changes to true so we don't save the board
	boolean error = false;
	
	//our shared preferences
	SharedPreferences prefs;
	
	//the editor for preferences
	SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBoard();
    }

    private void getBoard() {
    	//set up preferences
    	prefs = getPreferences(MODE_PRIVATE);
    	
    	//set up editor
    	editor = prefs.edit();
    	
    	//either load our previous preference or set to default as 4
    	int BoardSize = prefs.getInt("BoardSize", 4);
		
    	//inspiration from: http://stackoverflow.com/questions/2736389/how-to-pass-object-from-one-activity-to-another-in-android
		int imgResource = getIntent().getIntExtra("imageNum", -1);
		Log.i("img resource", "img resource is " + imgResource );
		
		//if we are coming from an intent
		if (imgResource != -1)
		{
			//get rid of intent extra so we don't get confused in the future
			getIntent().removeExtra("imageNum");
			createNewInstance(imgResource, BoardSize);
		}
		//not coming from intent
		else
		{
        	if (!loadInstance())
        	{
        		// if all else fails, load a new instance with default image
            	createNewInstance(defaultImg, BoardSize);
        	}     			
		}
    }
    
    private void createNewInstance(int ImgNum, int BoardSize) {
    	//hold a copy of our image resource
    	imgResource = ImgNum;
    	board = new NPuzzleBoard(this, ImgNum, BoardSize);
    	setContentView(new IntroView(this, imgResource));
    }
    

    private boolean loadInstance() {
    	Log.i("loadInstance", "preparing to load instance");
    	
    	//if we don't have the image in there, then return false so we start to load a new object
    	int imgID = prefs.getInt("imageID", -1);
    		//error checking...
    		if (imgID == -1)
    			return false;
    	
    	//set the imgResource for later
		imgResource = imgID;
    		
    	Log.i("loadInstance", "loaded image.  loading BoardSize");
    	
    	//get the boardSize
    	int BoardSize = prefs.getInt("BoardSize", 4);

    	Log.i("loadInstance", "loaded BoardSize");
		
    	//if we actually started the board...otherwise do the countdown
    	if (prefs.getBoolean("started", false)) {
			//establish our new board
			board = new NPuzzleBoard(this, imgID, BoardSize);
			
			//if there is a problem loading an image we want it to halt what we are doing
			if (!board.setupImageAndPieces())
				return true;
				
			int sizeOfPieces = BoardSize * BoardSize - 1;
	    	for (int i = 0; i < sizeOfPieces; i++)
	    	{
	    		int currentLoc = prefs.getInt("piece" + i, -1);    		
	    		//error checking...
	    		if (currentLoc > (sizeOfPieces + 1) || currentLoc < 0)
	    			return false;
	    		
	    		board.pieces[i].CurrentLoc = currentLoc;
	    	}
	    	
	    	Log.i("loadInstance", "loaded pieces");
	
	    		
	    	int EmptySpot = prefs.getInt("EmptySpotLoc", -1);
				if (EmptySpot == -1)
					return false;    	
	    	
			board.EmptySpotLoc = EmptySpot;
			
	    	Log.i("loadInstance", "loaded EmptySpot");
	
	
	    	
	    	
			int mixMoves = prefs.getInt("mixMovesLeft", 0);

			
			
			String movesFromSave = prefs.getString("MovesToMake", "");
			LinkedList<movement> movesToMake = new LinkedList<movement>();
			
			//concept of split from http://pages.cs.wisc.edu/~hasti/cs302/examples/Parsing/parseString.html
			String delims = "[ ]+";
			String[] moves = movesFromSave.split(delims);
			
			for (int i = 0; i < moves.length; i++)
				try {
					movesToMake.add(movement.valueOf(moves[i]));
				} catch (Exception e) {}
				
			
			Log.i("loadInstance", "size of board is " + movesToMake);
			
	    	
			//set up the content
			setContentView(board);
			if (mixMoves > 0)
				board.mixup(mixMoves);
			else if (!movesToMake.isEmpty())
			{
				board.currentAction = NPuzzleBoard.ActionType.solveIt;
				board.MovesToMake = movesToMake;
				board.executeMove();
			}
			
	    	Log.i("loadInstance", "should have loaded");
    	}
    	else {
    		//we were still in countdown
    		createNewInstance(imgID, BoardSize);
    	}
    	return true;
    }
    
    public void onPause() {
        //idea came from http://stackoverflow.com/questions/3389501/activity-transition-in-android
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

    	//hopefully save our board if we leave the program
    	saveBoard();
    	super.onPause();
    }
    
    void saveBoard() {
    	//save image id
    	editor.putInt("imageID", board.resourceID);
    	
    	// put in started
    	editor.putBoolean("started", board.started);
    	
    	//if board has truly been initialized and got past the initial animation
    	if (board.started) {
	    	Log.i("saveBoard", "preparing to save board");
	    	
	    	//if we are done or we never started, don't save it
	    	if (board == null || board.allDone)
	    	{
	        	Log.i("saveBoard", "done so removing imageID");
	    		editor.remove("imageID");
	    	}
	    	
	    	//save all necessary preferences
	    	else {
		    	// save pieces order
		    	int sizeToAdd = board.pieces.length;
		    	for (int i = 0; i < sizeToAdd; i++)
		    		editor.putInt("piece" + i, board.pieces[i].CurrentLoc);
		    	
		    	
		    	// put in EmptySpotLoc
		    	editor.putInt("EmptySpotLoc", board.EmptySpotLoc);
		    	
		    	//put in the boardsize
		    	editor.putInt("BoardSize", board.BoardSize);
		    	
		    	//save where we are in mixing up pieces
		    	editor.putInt("mixMovesLeft", board.mixMovesLeft);
		    	
		    	//save solver moves		
		    	//save solver moves		
		    	String movesToSave = ""; 
		    	
		    	Iterator<movement> iterator = board.MovesToMake.iterator();
		    	while (iterator.hasNext())
	    		{
		    		movement move = iterator.next();
		    		Log.i("saveBoard", "saving move of " + move);
		    		movesToSave += move.name() + " ";
	    		}
		    	editor.putString("MovesToMake", movesToSave);
	    	}
    	}
    	
    	editor.commit();
    	Log.i("saveBoard", "saved");
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
    	// show menu when menu button is pressed
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.puzzle_menu, menu);
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId())
    	{
    		case R.id.preview:
    			Intent i = new Intent(this, YouWin.class);
    			i.putExtra("win", false);
    			i.putExtra("imageNum", board.resourceID);
    			startActivity(i);
    			break;
    		case R.id.shuffle:
        		board.mixup(board.BoardSize * 8);

    			break;   
    		case R.id.solveIt:
    			board.AStar();
    			break;
    		case R.id.another:
        		finish();
    			break;
    		case R.id.difficulty:
				setDifficulty();
    			break;
    	}
    	return true;
    }
    
    //based off of the android docs for dialogs: http://developer.android.com/guide/topics/ui/dialogs.html
	private void setDifficulty() {
	    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, 0));
	    builder.setTitle("Choose the Difficulty...");
	    builder.setCancelable(true);
    	builder.setItems(new String[]{"Easy", "Medium", "Hard"}, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					int num = id + 3;
	    			if (board == null || board.BoardSize != num) {
	    				editor.putInt("BoardSize", num);
	    				editor.commit();
	    				createNewInstance(imgResource, num);
	    			}
				}
       });
    	
	   AlertDialog difficulty = builder.create();
	   difficulty.show();
    }
	
	
	// get display metrics taken from: http://stackoverflow.com/questions/1016896/android-how-to-get-screen-dimensions
    int getMaxDimension(Activity activity) {
    	DisplayMetrics metrics = new DisplayMetrics();
    	activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return Math.max(metrics.widthPixels, metrics.heightPixels);
    }
	
    
	//based on android source here: http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
	Bitmap loadImage(Activity activity, int ResourceID, int MaxWidth, int MaxHeight) {		
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
}

