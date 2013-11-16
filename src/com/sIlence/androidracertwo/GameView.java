/*
 *
 * This file is part of AndroidRacerTwo
 *
 * AndroidRacerTwo is free software: you can redistribute it and/or modify
 * it under the term of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the Licence, or
 * (at your option) any later version.
 * 
 * AndroidRacerTwo is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with AndroidRacerTwo. If not, see <http://www.gnu.org/licenses/>
 *
 * Copyright: 2013 Mytchel Hammond <mytchel.hammond@gmail.com>
 *
*/


package com.sIlence.androidracertwo;

import com.sIlence.androidracertwo.game.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.util.Log;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    public static int INCREASE_KILLS = 1;
    public static int INCREASE_NULL = 0;
    public static int INCREASE_DEATHS = -1;

    private boolean pausing;
    private boolean starting;
    private boolean gameOver;
    private boolean won;
    
    private int countdown;
    private int startcount;

    private GameLoop loop;

    private long endTime;

    private int	boxWidth, boxHeight, boxsX, boxsY;
    private int	top;
    private int	rotation;

    private float x, y, xDiff, yDiff;

    private Rect bounds;
    private String textString;

    private int	fromRight;
    private Paint brush;

    private Game game;

    private MyDialog dialog;
    private boolean closing;

    public GameView(Context context, Game g) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);

        game = g;
        loop = new GameLoop(this);
    }

    protected void newGame() {
        game.init(this);
        
        closing = false;
        starting = true;
        gameOver = false;
        won = false;
        startcount = getTime();
        countdown = 3;

        bounds = new Rect();
        textString = "";

//        setTime(0);
        endTime = System.currentTimeMillis();
    }

    public void update() {
        if (starting || gameOver) return;
         
        incTime(loop.framePeriod());
        
        if (countdown > 0) {
            if ((getTime() - startcount) / 1000 >= 4 - countdown) {
                countdown--;
            }
	    if (countdown == 0) setTime(startcount);
            return;
        }
     
        game.update();
       
        if (pausing) {
            pauseGame();
        }
    }

    public void render(Canvas c) {
        if (c == null) return;

        background(c);
        game.render(c);
        hud(c);
        messages();
    }

    public void hud(Canvas c) {
        brush.setColor(0xffffffff);

	int t = getTime() / 1000;
	if (countdown > 0) {
            brush.setColor(0xffffffff);
            float size = brush.getTextSize();
            brush.setTextSize(getHeight() / 10);
            String message = "" + countdown;
            c.drawText(message, getWidth() / 2 - halfWidth(message), getHeight() / 2, brush);
            brush.setTextSize(size);
	    t = startcount / 1000;
        }

	c.drawText("Time: " + t, 10, brush.getFontSpacing(), brush);

        textString = getKills() + " : " + getDeaths();
        c.drawText(textString, getWidth() - fromRight - halfWidth(textString), brush.getFontSpacing(), brush);

        textString = "Lives: " + local().lives();
        c.drawText(textString, getWidth() / 2 - halfWidth(textString), brush.getFontSpacing(), brush);
    }

    public void messages() {
        if (closing) {
            return;
        }

        if (gameOver) {
            if (won) {
                newGameBox(game.winMessage(), "New Game", "Exit");
            } else {
                newGameBox(game.loseMessage(), "New Game", "Exit");
            }
        } else if (starting) {
            pauseBox("You Are Blue\nSwipe To Turn\nMake Yellow Crash\nTap To Play", "Start", "Exit");
            starting = false;
        } else if (loop.isPaused() || pausing) {
            pauseBox("Paused", "Resume", "Exit");
        }
    }

    public FragmentTransaction cleanupFragments() {
        FragmentTransaction ft = ((Activity) getContext()).getFragmentManager().beginTransaction();

        Fragment f = ((Activity) getContext()).getFragmentManager().findFragmentByTag("dialog");
        if (f != null) ft.remove(f);

        return ft;
    }

    public void newGameBox(String message, String p, String n) {
        pauseGame();

        FragmentTransaction ft = cleanupFragments();

        dialog = new NewGameDialog(this, message, p, n);
        dialog.show(ft, "dialog");
    }

    public void pauseBox(String m, String p, String n) {
        pauseGame();

        FragmentTransaction ft = cleanupFragments();

        dialog = new PauseDialog(this, m, p, n);
        dialog.show(ft, "dialog");
    }

    public void checkScore() {
        game.checkScore(); 
    }

    public void gameOver(boolean w) {
        won = w;

        gameOver = true;
        endTime = System.currentTimeMillis();
    }

    public void changeScore(int scoreType) {
        if (scoreType == INCREASE_DEATHS) {
            incDeaths();
        } else if (scoreType == INCREASE_KILLS) {
            incKills();
        }
    }

    public int halfWidth(String text) {
        brush.getTextBounds(text, 0, text.length(), bounds);
        return bounds.width() / 2;
    }

    public void background(Canvas c) {
        brush.setColor(0xFF000308);
        brush.setStyle(Paint.Style.FILL);
        c.drawRect(0, 0, getWidth(), getHeight(), brush);

        brush.setColor(0x206FC0DF);
        brush.setStyle(Paint.Style.STROKE);

        for (int x = 0; x < getWidth(); x += 70) {
            c.drawLine(x, top, x, getHeight(), brush);
        }
        for (int y = top; y < getHeight(); y += 70) {
            c.drawLine(0, y, getWidth(), y, brush);
        }
    }

    @Override
    public boolean onTouchEvent (MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            x = e.getX();
            y = e.getY();
        } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
            xDiff = e.getX() - x;
            yDiff = e.getY() - y;
            x = e.getX();
            y = e.getY();

            int g = vertorhorz(xDiff, yDiff);
            if (g == 0) {
                if (xDiff > 0 && local().changeDirection(1));
                if (xDiff < 0 && local().changeDirection(3));
            } else if (g == 1) {
                if (yDiff < 0 && local().changeDirection(0));
                if (yDiff > 0 && local().changeDirection(2));
            }
        }
        return true;
    }

    protected int vertorhorz(float x, float y) {
        if (x < 0) x = -x;
        if (y < 0) y = -y;

        if (x > y) return 0;
        if (y > x) return 1;
        return -1;
    }

    public void pause() {
        pausing = true;
    }

    public void pauseGame() {
        loop.pauseGame();
        pausing = false;
        
        local().pause();
        other().pause();
        wall1().pause();
        wall2().pause();
    }

    public synchronized void resumeGame() {
        if (gameOver) return;
        closing = false;
        loop.resumeGame();
    }

    public synchronized void stopGame() {
        closing = true;
        if (loop != null) 
            loop.stopGame();
    }

    public synchronized void start() {
        if (loop != null) {
            loop.stopGame();
        }

        pausing = false;
        closing = false;
        notify();

        loop = new GameLoop(this);
        loop.start();

        pauseGame();
    }

    public boolean isPaused() {
        if (loop == null) return true;
        if (gameOver) return true;
        return loop.isPaused();
    }

    public void surfaceCreated(SurfaceHolder arg0) {

        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        rotation = display.getRotation();

        brush = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Screen dependent stuff

        int size = getWidth();
        if (getHeight() < getWidth()) size = getHeight();

        if (size < 350) { // small

            brush.setTextSize(12);

            boxWidth = 3;
            boxHeight = 3;
            // has to be in boxs or it fucks everything up
            top = boxHeight * 6;
            fromRight = 40;
        } else { // normal

            brush.setTextSize(26);

            boxWidth = 7;
            boxHeight = 7;
            // has to be in boxs or it fucks everything up
            top = boxHeight * 8;
            fromRight = 80;
        }

        boxsX = getWidth() / boxWidth;
        boxsY = (getHeight() - top) / boxHeight;

        newGame();
        start();
    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
        closing = true;

        local().stop();
        other().stop();
        wall1().stop();
        wall2().stop();

        stopGame();
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {}

    public int boxsX() {
        return boxsX;
    }

    public int boxsY() {
        return boxsY;
    }

    protected void setBoxsX(int n) {
        boxsX = n;
    }

    protected void setBoxsY(int n) {
        boxsY = n;
    }

    public int boxWidth() {
        return boxWidth;
    }

    public int boxHeight() {
        return boxHeight;
    }

    protected void setBoxWidth(int n) {
        boxWidth = n;
    }

    protected void setBoxHeight(int n) {
        boxHeight = n;
    }

    public int top() {
        return top;
    }

    public int rotation() {
        return rotation;
    }

    public int gratestLengthInSegments() {
        if (boxsX > boxsY) return boxsX;
        return boxsY;
    }

    public int getKills() {
        return game.getKills();
    }

    public void incKills() {
        game.setKills(game.getKills() + 1); 
    }

    public void setKills(int k) {
        game.setKills(k); 
    }

    public int getDeaths() {
        return game.getDeaths();
    }

    public void incDeaths() {
        game.setDeaths(game.getDeaths() + 1);
    }

    public void setDeaths(int d) {
        game.setDeaths(d);
    }

    public int getTime() {
        return game.getTime();
    }

    public void incTime(int a) {
        game.setTime(game.getTime() + a);
    }

    public void setTime(int t) {
        game.setTime(t);
    }

    public int framePeriod() {
        return loop.framePeriod();
    }

    public LightRacer local() {
        return game.local();
    }

    public AIRacer other() {
        return game.other();
    }

    public WallRacer wall1() {
        return game.wall1();
    }

    public WallRacer wall2() {
        return game.wall2();
    }

    public void killDialog() {
        if (dialog != null) dialog.dismiss();
    }

    public int turnDelay() {
        return 5;
    }
}
