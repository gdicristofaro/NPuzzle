package com.gdicristofaro.npuzzle;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class NPuzzleBoard extends RelativeLayout {
    // a puzzle piece and its characteristics
    class NPuzzlePiece extends ImageView implements OnClickListener  {

        // the place where it ought to be
        int CurrentLoc;

        NPuzzlePiece(Context context, Bitmap image, int CurrentLoc) {
            super(context);
            setImageBitmap(image);
            setOnClickListener(this);
            this.CurrentLoc = CurrentLoc;
        }

        public void onClick(View shouldbethis) {
            if (currentAction == ActionType.userAction)
            {
                movePiece(this);
                Log.i("click", "yes on " + CurrentLoc);
            }
        }
    }


    // internal representation of where pieces should be and also holds onto all the pieces
    NPuzzlePiece[] pieces;

    //i.e. 4 for a 4x4 board
    public final int BoardSize;

    //index in pieces of empty Spot
    int EmptySpotLoc;

    //all picture related items
    int imageHeight;
    int imageWidth;
    private Bitmap reflection;


    //for accurate placing of views
    private int marginWidth;
    private int marginHeight;

    private int pieceWidth;
    private int pieceHeight;

    //for animation
    private TranslateAnimation slide;
    //in ms how long we should animate
    private int animationTime = 200;

    //specifies if we are accepting actions from user at this point
    //private boolean acceptingAction = true;

    enum ActionType {mixUp, userAction, solveIt};
    ActionType currentAction = ActionType.userAction;




    //is it actually initialized
    boolean started = false;

    int resourceID;

    NPuzzle activity;

    //to keep track of whether or not the game is finished for saving purposes
    boolean allDone = false;


    public NPuzzleBoard(NPuzzle activity, int ResourceID, int BoardSize)
    {
        super(activity);
        this.activity = activity;
        this.resourceID = ResourceID;
        this.BoardSize = BoardSize;
    }

    boolean setupImageAndPieces() {
        int maxDim = activity.getMaxDimension(activity);
        Bitmap completeImage = activity.loadImage(activity, resourceID, maxDim, maxDim);

        imageHeight = completeImage.getHeight();
        imageWidth = completeImage.getWidth();

        pieces = new NPuzzlePiece[BoardSize * BoardSize - 1];

        //set Empty Spot to last Empty Spot
        EmptySpotLoc = BoardSize * BoardSize - 1;

        makePieces(activity, completeImage);
        started = true;
        return true;
    }

    void mixup(int MixMoves) {
        animationTime = 75;
        currentAction = ActionType.mixUp;
        mixMovesLeft = MixMoves;
        mixUpPieces();
    }




    //divy up pieces into images
    private void makePieces(Context context, Bitmap puzzleImage) {
        int TotalWidth = puzzleImage.getWidth();
        int pieceWidth = TotalWidth / BoardSize;
        int TotalHeight = puzzleImage.getHeight();
        int pieceHeight = TotalHeight / BoardSize;


        // scale the image based on our piece size and draw the resource
        reflection = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.reflection), pieceWidth, pieceHeight, true);

        //for each piece
        for (int i = 0; i < pieces.length; i++) {
            Log.i("makepieces", "creating piece " + i);

            //cut up the image
            Bitmap theimage = Bitmap.createBitmap(puzzleImage, (i % BoardSize) * pieceWidth, (i / BoardSize) * pieceHeight, pieceWidth, pieceHeight);

            // borders and reflection
            theimage = setupImage(theimage);

            //put it in the pieces array
            pieces[i] = new NPuzzlePiece(context, theimage, i);

            this.addView(pieces[i]);
        }
    }

    //this method sets up the image with a border and reflection
    //drawing on the image from http://stackoverflow.com/questions/1540272/android-how-to-overlay-a-bitmap-draw-over-a-bitmap
    private Bitmap setupImage(Bitmap orig) {
        Canvas canvas = new Canvas(orig);
        Paint paint = new Paint();

        //draw reflection on the image
        canvas.drawBitmap(reflection, 0, 0, paint);

        //set paint to a light grey
        paint.setColor(Color.rgb(236, 236, 236));

        //draw top and left piece
        canvas.drawRect(0, 0, canvas.getWidth(), 2, paint);
        canvas.drawRect(0, 0, 2, canvas.getHeight(), paint);

        //set paint to a dark grey
        paint.setColor(Color.argb(255, 30, 30, 30));

        //draw bottom and right piece
        canvas.drawRect(0, canvas.getHeight() - 2, canvas.getWidth(), canvas.getHeight(), paint);
        canvas.drawRect(canvas.getWidth() - 2, 0, canvas.getWidth(), canvas.getHeight(), paint);

        return orig;
    }


    NPuzzlePiece getByLoc(int loc)
    {
        Log.i("getByLoc", "looking for piece at " + loc);

        //max of where pieces should be in our array
        int totalLocs = pieces.length;

        //if loc is < 0 or greater than the boardsize, we won't find it
        if (loc < 0 || loc > totalLocs)
        {
            Log.i("getByLoc", "    returns null...");
            return null;
        }

        //go and look for it
        for (int i = 0; i < totalLocs; i++)
            if (pieces[i].CurrentLoc == loc)
                return pieces[i];

        Log.i("getByLoc", "    returns null...");

        //if we can't find it
        return null;
    }


    //this is for mixUpPieces, which needs to keep track of what the last move was
    int lastMove = -1;

    //initialize to 0 because our constructor should take care of this
    int mixMovesLeft = 0;

    /* this is for mixing up the pieces
     * Empty spot is where the empty spot is right now
     * timesLeft is how many more moves we want to mix it up
     * lastMove indicates the last move that was made so we don't do it again
     */
    private void mixUpPieces()
    {
        int nextMove;

        while(true)
        {
            nextMove = (int) (Math.random() * 4);

            // 0 will be for move empty spot right and we aren't coming back from lastmove and we are not end of the row
            if (nextMove == 0 && lastMove != 2 && moveBlankRight())
                lastMove = 0;
                // 2 will be for move left
            else if (nextMove == 2 && lastMove != 0 && moveBlankLeft())
                lastMove = 2;
                // 1 will be for move up
            else if (nextMove == 1 && lastMove != 3 && moveBlankUp())
                lastMove = 1;
                // 1 will be for move down
            else if (nextMove == 3 && lastMove != 1 && moveBlankDown())
                lastMove = 3;
            else
                continue;

            return;
        }
    }

    private boolean moveBlankDown() {
        return movePiece(getByLoc(EmptySpotLoc + BoardSize));
    }

    private boolean moveBlankUp() {
        return movePiece(getByLoc(EmptySpotLoc - BoardSize));
    }

    private boolean moveBlankLeft() {
        return movePiece(getByLoc(EmptySpotLoc - 1));
    }

    private boolean moveBlankRight() {
        return movePiece(getByLoc(EmptySpotLoc + 1));
    }

    /**
     * Identify if piece is next to emptyspot
     * @param 		piece the piece to check
     * @return		returns true if next to
     */
    public boolean nextTo(NPuzzlePiece piece) {
        if (piece == null)
            return false;

        // if puzzle piece is above empty spot
        int currentLoc = piece.CurrentLoc;

        // if piece is above EmptySpot
        if ((currentLoc == EmptySpotLoc - BoardSize) ||
                // if piece is below EmptySpot
                (currentLoc == EmptySpotLoc + BoardSize) ||
                // if piece is to the left of EmptySpot and make sure it is actually in same row
                (currentLoc == EmptySpotLoc - 1 && currentLoc / BoardSize == EmptySpotLoc / BoardSize) ||
                // if piece is to the right of EmptySpot
                (currentLoc == EmptySpotLoc + 1 && currentLoc / BoardSize == EmptySpotLoc / BoardSize))
        {
            return true;
        }
        return false;
    }

    public boolean movePiece(NPuzzlePiece piece)
    {
        if (piece == null)
            return false;

        // if puzzle piece is above empty spot
        int currentLoc = piece.CurrentLoc;

        // if piece is above EmptySpot
        if (nextTo(piece))
        {
            //set up where the piece should be
            piece.CurrentLoc = EmptySpotLoc;
            EmptySpotLoc = currentLoc;
            currentLoc = piece.CurrentLoc;

            //fixing flicker in slide animation from: http://www.clingmarks.com/how-to-permanently-move-view-with-animation-effect-in-android/400


            //adapted from http://stackoverflow.com/questions/7386703/android-animate-translate-in-android-2-2
            slide = new TranslateAnimation(Animation.ABSOLUTE, (pieceWidth * (EmptySpotLoc % BoardSize - currentLoc % BoardSize)),
                    Animation.ABSOLUTE, 0,
                    Animation.ABSOLUTE, (pieceHeight * (EmptySpotLoc / BoardSize - currentLoc / BoardSize)),
                    Animation.ABSOLUTE, 0);

            slide.setDuration(animationTime);

            // call back on animation complete
            slide.setAnimationListener(
                    new SlideList(piece,
                            marginWidth + (pieceWidth * (currentLoc % BoardSize)),
                            marginHeight + (pieceHeight * (currentLoc / BoardSize))
                    )
            );


            piece.startAnimation(slide);
            requestLayout();
            return true;
        }
        //otherwise...do nothing
        return false;
    }

    //tasked with animating the movement of our pieces
    class SlideList implements Animation.AnimationListener {
        private NPuzzlePiece piece;
        private int x;
        private int y;

        SlideList(NPuzzlePiece piece, int x, int y) {
            this.piece = piece;
            this.x = x;
            this.y = y;
        }

        //determines the steps to occur after animation has finished
        @Override
        public void onAnimationEnd(Animation animation) {
            // This will handle actions to occur after piece animation
            switch (currentAction)
            {
                case userAction:
                    //we made another move
                    numMoves++;
                    checkForWin();
                    break;
                case mixUp:
                    mixMovesLeft--;

                    //if we are done, lets accept user input
                    if (mixMovesLeft == 0)
                    {
                        //we don't need to be so quick with the animations
                        animationTime = 200;
                        currentAction = ActionType.userAction;
                    }

                    //otherwise, mix up the pieces
                    else
                        mixUpPieces();
                    break;
                case solveIt:
                    if (MovesToMake.isEmpty())
                    {
                        checkForWin();
                        currentAction = ActionType.userAction;
                    }
                    else
                        numMoves++;
                    executeMove();
                    break;
            }
        }

        public void onAnimationRepeat(Animation animation) {}

        public void onAnimationStart(Animation animation) {
            //set location of piece actually
            LayoutParams params = (LayoutParams) piece.getLayoutParams();
            params.setMargins(x, y, 0, 0);
        }
    }

    private void drawViews(int screenWidth, int screenHeight)
    {

        double ratio = Math.min(((double) screenHeight / imageHeight) , ((double) screenWidth / imageWidth) );

        int viewHeight = (int) (imageHeight * ratio);
        int viewWidth = (int) (imageWidth * ratio);

        Log.i("dimensions: ", + viewWidth + ", " + viewHeight);


        //idea from here: http://stackoverflow.com/questions/12909741/android-positioning-views-programatically
        marginWidth = (screenWidth - viewWidth) / 2;
        marginHeight = (screenHeight - viewHeight) / 2;

        pieceWidth = viewWidth / BoardSize;
        pieceHeight = viewHeight / BoardSize;

        for (int i = 0; i < pieces.length; i++)
        {
            int pieceloc = pieces[i].CurrentLoc;

            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(marginWidth + (pieceloc % BoardSize) * pieceWidth, marginHeight + (pieceloc / BoardSize) * pieceHeight, 0, 0);

            params.height = pieceHeight;
            params.width = pieceWidth;

            pieces[i].setLayoutParams(params);
        }
    }


    //overriding onMeasure... http://stackoverflow.com/questions/2991110/android-how-to-stretch-an-image-to-the-screen-width-while-maintaining-aspect-ra
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        // find out what is the max ratio we can fit in this thing
        int screenHeight = MeasureSpec.getSize(heightMeasureSpec);
        int screenWidth = MeasureSpec.getSize(widthMeasureSpec);

        drawViews(screenWidth, screenHeight);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    //to keep track of how many moves have been made
    private int numMoves;


    private boolean checkForWin() {
        //go through all the pieces and see if they are in order or not
        int allPiecesNum = BoardSize * BoardSize - 1;

        for (int i = 0; i < allPiecesNum; i++)
        {
            if (pieces[i].CurrentLoc != i) {
                return false;
            }
        }

        Log.i("Good Game!", "You Win!");
        Intent i = new Intent(this.getContext(), YouWin.class);
        i.putExtra("win", true);
        i.putExtra("numMoves", numMoves);
        i.putExtra("imageNum", resourceID);
        allDone = true;

        this.getContext().startActivity(i);
        ((Activity) this.getContext()).finish();

        return true;
    }


    //keeps track of movements we can make
    enum movement {Left, Right, Down, Up, None};

    //this class is representation of board states and potential future board states considered by the solver
    private class BoardModel implements Comparable<Object> {
        final LinkedList<movement> moves;

        //index is where it belongs, and the int represents where it currently is
        final int[] boardRepr;
        final int movesMade;
        final int EmptySpot;

        int currentScore;

        //create representation of actual board - pieceIndexes represents the pieces we need to move in place
        BoardModel()
        {
            movesMade = 0;

            moves = new LinkedList<movement>();

            EmptySpot = EmptySpotLoc;

            int piecesLength = pieces.length;
            boardRepr = new int[piecesLength];

            for (int i = 0; i < piecesLength; i++)
            {
                boardRepr[i] = pieces[i].CurrentLoc;
                //Log.i("Board Model", "current Pos for " + i + " is " + boardRepr[i]);
            }

        }

        //sets the score for the Astar piece algorithm
        void setScore(int index, int loc)
        {

            //generate real current score: 6 times positions away + how far empty spot is from piece

            int totalDist = getManhattanDistance(boardRepr[index], loc);
            int minDist = getManhattanDistance(boardRepr[index], EmptySpot);

            //totaldistance we still have to travel * 6 because it could take 6 moves to move the piece and + minDist of distance of empty spot from closest piece
            currentScore = -1 * (totalDist * 6 + minDist);
            //Log.i("BoardModel", " curr score is " + currentScore);
        }

        // helper method for setscore
        private int getManhattanDistance(int first, int last) {
            int vert = Math.abs(first / BoardSize - last / BoardSize);
            int horz = Math.abs(first % BoardSize - last % BoardSize);
            return vert + horz;
        }


        // do copy...  - pieceIndexes represents the pieces we need to move in place
        BoardModel(BoardModel oldGuess, movement newMovement)
        {
            //add all old moves and new moves
            moves = new LinkedList<movement>();
            moves.addAll(oldGuess.moves);
            moves.add(newMovement);

            //adjust for new move accordingly
            switch (newMovement) {
                case Left:
                    EmptySpot = oldGuess.EmptySpot - 1;
                    break;
                case Right:
                    EmptySpot = oldGuess.EmptySpot + 1;
                    break;
                case Up:
                    EmptySpot = oldGuess.EmptySpot - BoardSize;
                    break;
                case Down:
                    EmptySpot = oldGuess.EmptySpot + BoardSize;
                    break;
                default:
                    EmptySpot = oldGuess.EmptySpot;
            }

            movesMade = oldGuess.movesMade - 1;


            //copy board: switch locations
            int boardLength = oldGuess.boardRepr.length;
            boardRepr = new int[boardLength];

            for (int i = 0; i < boardLength; i++)
            {
                int currplace;

                //we switch places with where old Empty Spot used to be
                if (oldGuess.boardRepr[i] == EmptySpot)
                    currplace = oldGuess.EmptySpot;
                else
                    currplace = oldGuess.boardRepr[i];

                boardRepr[i] = currplace;
            }
        }

        //I am using hash code as a means for comparing scores of moves since it is object implemented
        @Override
        public int hashCode() {
            return currentScore + movesMade;
        }

        @Override
        public int compareTo(Object arg) {
            //Log.i("compareTo", "hashcode is " + hashCode() + " and other is " + arg.hashCode() + " with compareto as " + (hashCode() - arg.hashCode()));
            //int hashcodeDiff = arg.hashCode() - hashCode();

            return  arg.hashCode() - hashCode();
        }

    }


    /*
     * This algorithm is not purely an A* algorithm
     * It is based off of the algorithm presented here: http://larc.unt.edu/ian/pubs/saml.pdf
     * and for individual piece moves, it performs some sort of A* algorithm
     */
    boolean AStar() {
        if (currentAction != ActionType.userAction)
            return false;

        currentAction = ActionType.solveIt;

        try {
            HashSet<Integer> setPieces = new HashSet<Integer>();

            BoardModel theboard = new BoardModel();

            int currBoardWidth = BoardSize;
            while (currBoardWidth > 2)
            {
                //identifies top left corner of inner box
                int cornerPiece = (BoardSize - currBoardWidth) * (BoardSize + 1);

                //set corner piece
                theboard = AStarPiece(setPieces, theboard, cornerPiece, cornerPiece);

                int widthToSet = currBoardWidth - 2;
                for (int i = 1; i < widthToSet; i++)
                {
                    //go column wise
                    int columnItem = cornerPiece + i;
                    theboard = AStarPiece(setPieces, theboard, columnItem, columnItem);

                    //go row wise
                    int rowItem = cornerPiece + i * BoardSize;
                    theboard = AStarPiece(setPieces, theboard, rowItem, rowItem);
                }

                //move almost end piece into position
                int almostEndColPos = BoardSize * (BoardSize - currBoardWidth) + (BoardSize - 2);
                int endColPos = BoardSize * (BoardSize - currBoardWidth) + (BoardSize - 1);

                theboard = AStarPiece(setPieces, theboard, almostEndColPos, endColPos);

                //if end piece is in a bad place
                if (theboard.boardRepr[endColPos] == almostEndColPos ||
                        (theboard.boardRepr[endColPos] == almostEndColPos + BoardSize && theboard.EmptySpot == almostEndColPos))
                {
                    //remove the neighboring piece in endColPos so we don't have a locked piece
                    setPieces.remove(endColPos);

                    //move the piece to the end of the board
                    theboard = AStarPiece(setPieces, theboard, endColPos, BoardSize * BoardSize - 1);

                    //move the piece back
                    theboard = AStarPiece(setPieces, theboard, almostEndColPos, endColPos);
                }

                //finish moving into place
                theboard = AStarPiece(setPieces, theboard, endColPos, endColPos + BoardSize);

                theboard = AStarPiece(setPieces, theboard, almostEndColPos, almostEndColPos);
                theboard = AStarPiece(setPieces, theboard, endColPos, endColPos);






                //do the other bit...same process as before
                int almostEndRowPos = BoardSize * (BoardSize - 2) + (BoardSize - currBoardWidth);
                int endRowPos = BoardSize * (BoardSize - 1) + (BoardSize - currBoardWidth);

                theboard = AStarPiece(setPieces, theboard, almostEndRowPos, endRowPos);

                //if it is in a bad place
                if (theboard.boardRepr[endRowPos] == almostEndRowPos ||
                        (theboard.boardRepr[endRowPos] == almostEndRowPos + 1 && theboard.EmptySpot == almostEndRowPos))
                {
                    //remove the neighboring piece that is in endrowPos so we don't have a locked piece
                    setPieces.remove(endRowPos);

                    //move the piece to the end of the board
                    theboard = AStarPiece(setPieces, theboard, endRowPos, BoardSize * BoardSize - 1);

                    //move the piece back
                    theboard = AStarPiece(setPieces, theboard, almostEndRowPos, endRowPos);
                }

                //move into place
                theboard = AStarPiece(setPieces, theboard, endRowPos, endRowPos + 1);

                theboard = AStarPiece(setPieces, theboard, almostEndRowPos, almostEndRowPos);
                theboard = AStarPiece(setPieces, theboard, endRowPos, endRowPos);


                //consider the "smaller" piece of the board (i.e. 5x5 to 4x4 to 3x3...
                currBoardWidth--;
            }

            //do the 2x2 portion left in the bottom corner

            //this should be the last corner piece to set
            int lastCornerPiece = ((BoardSize - 1) * BoardSize) - 2;

            theboard = AStarPiece(setPieces, theboard, lastCornerPiece, lastCornerPiece);
            theboard = AStarPiece(setPieces, theboard, (lastCornerPiece + 1), (lastCornerPiece + 1));
            theboard = AStarPiece(setPieces, theboard, (lastCornerPiece + BoardSize), (lastCornerPiece + BoardSize));

            if (theboard.moves != null)
                MovesToMake = theboard.moves;

            //start moving the pieces
            executeMove();

        } catch (Exception e)
        {
            //if we have a problem, show the error message
            showErrorMessage(e.getMessage());

            currentAction = ActionType.userAction;
            return false;
        }

        return true;
    }

    //shows an error message with given message
    private void showErrorMessage(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, 0));
        builder.setTitle("Uh Oh!");
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog error = builder.create();
        error.show();
    }


    //this method moves individual pieces into place
    BoardModel AStarPiece(HashSet<Integer> setPieces, BoardModel CurrentBoard, int index, int loc)
    {
        //if loc or index exists in pieces, take it out
        setPieces.remove(loc);
        setPieces.remove(CurrentBoard.boardRepr[index]);



        CurrentBoard.setScore(index, loc);

        //Heap keeps the best bet at the top of the heap
        PriorityQueue<BoardModel> nextMove = new PriorityQueue<BoardModel>();
        nextMove.add(CurrentBoard);


        //trying to avoid an infinite loop
        int failsafe = 500;

        //while piece is not closer to end goal
        while(nextMove.peek().currentScore < -1)
        {
            //Log.i("setPiece", "not at end goal...continuing");

            //get possible moves
            ArrayList<movement> possibleMoves = new ArrayList<movement>(Arrays.asList(movement.Left, movement.Right, movement.Up, movement.Down));

            //calculate next move...
            BoardModel NextGuess = nextMove.poll();

            Log.i("setPiece", "empty spot: " + NextGuess.EmptySpot + "   board total: " + NextGuess.hashCode() + "  current: " + NextGuess.currentScore + "   piece " + index + " at " + NextGuess.boardRepr[index] + " with goal of " + loc);



            //eliminate impossible moves avoid setPieces
            if (NextGuess.EmptySpot % BoardSize == 0 || setPieces.contains(NextGuess.EmptySpot - 1))
                possibleMoves.remove(movement.Left);

            if (NextGuess.EmptySpot % BoardSize == BoardSize - 1 || setPieces.contains(NextGuess.EmptySpot + 1))
                possibleMoves.remove(movement.Right);

            if (NextGuess.EmptySpot / BoardSize == 0 || setPieces.contains(NextGuess.EmptySpot - BoardSize))
                possibleMoves.remove(movement.Up);

            if (NextGuess.EmptySpot / BoardSize == BoardSize - 1 || setPieces.contains(NextGuess.EmptySpot + BoardSize))
                possibleMoves.remove(movement.Down);


            //for each move, add a new guess and calculate its value
            int size = possibleMoves.size();
            for (int i = 0; i < size; i++)
            {
                //copy board and adjust accordingly
                BoardModel newModel = new BoardModel(NextGuess, possibleMoves.get(i));

                // set the score for what we added
                newModel.setScore(index, loc);


                //place in heap
                nextMove.add(newModel);
            }


            failsafe--;

            //if we have a problem...
            if (failsafe < 0)
            {
                String allMoves = "";
                for (int i = 0; i < possibleMoves.size(); i++)
                    allMoves += possibleMoves.get(i).name() + " ";

                String setPoints = "";
                for (int setNumber : setPieces)
                    setPoints += "(" + setNumber % BoardSize + "," + setNumber / BoardSize + "), ";

                Log.i("ERROR IN A STAR", "hit fail safe for current move of piece " + index + " to " + loc);
                Log.i("ERROR IN A STAR", "all moves is " + allMoves + "  set pieces includes: " + setPoints);
                throw new IllegalStateException("Solver has tried too many moves...cancelling.");
            }
            else if (nextMove.size() < 1)
            {
                String allMoves = "";
                for (int i = 0; i < possibleMoves.size(); i++)
                    allMoves += possibleMoves.get(i).name() + " ";

                String setPoints = "";
                for (int setNumber : setPieces)
                    setPoints += "(" + setNumber % BoardSize + "," + setNumber / BoardSize + "), ";

                Log.i("ERROR IN A STAR", "next moves ran out of space for current move of piece " + index + " to " + loc);
                Log.i("ERROR IN A STAR", "all moves is " + allMoves + "  set pieces includes: " + setPoints);


                throw new IllegalStateException("Solver found no where to move a piece.");
            }

        }


        //make sure we set the pieces we need so we don't move them again
        setPieces.add(loc);


        return nextMove.poll();
    }



    //holds moves to be made by solver
    LinkedList<movement> MovesToMake = new LinkedList<movement>();

    //executes the next move in MovesToMake
    void executeMove() {
        if (MovesToMake.isEmpty())
            return;


        Log.i("position switch", "move is " + MovesToMake.peek());

        switch (MovesToMake.poll()) {
            case Left:
                moveBlankLeft();
                break;
            case Right:
                moveBlankRight();
                break;
            case Up:
                moveBlankUp();
                break;
            case Down:
                moveBlankDown();
                break;
        }
    }
}