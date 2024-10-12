package com.bkacad.nnt.save_the_cat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends View {
    Bitmap background, ground, cat;
    //declare to direct object references for background and ground
    Rect rectBackground, rectGround;
    Context context;
    Handler handler;
    final long UPDATE_MILLIS = 30;
    Runnable runnable; //declare a runnable object reference
    //define 2 paint objects for showing points and health
    Paint textPaint = new Paint();
    Paint healthPaint = new Paint();
    float TEXT_SIZE = 120;
    int points = 0;
    int life = 3;
    static int dWidth, dHeight;
    Random random;
    float catX, catY;
    //repositioning the rabbit during a touch
    float oldX;
    float oldCatX;
    ArrayList<Spike> spikes;
    ArrayList<Explosion> explosions;

    public GameView(Context context){
        super(context);
        this.context = context;
        
        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        ground = BitmapFactory.decodeResource(getResources(), R.drawable.ground);
        cat = BitmapFactory.decodeResource(getResources(), R.drawable.cat);
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        dWidth = size.x;
        dHeight = size.y;

        //instantiate the wrecked background and rigged ground objects
        rectBackground = new Rect(0, 0, dWidth, dHeight);
        rectGround = new Rect(0, dHeight - ground.getHeight(), dWidth, dHeight);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {invalidate();}
        };
        //set the color text size text alignment and type phase
        textPaint.setColor(Color.rgb(255,255,255));
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.kenney_blocks));
        healthPaint.setColor(Color.GREEN);

        //instantiate the random object
        random = new Random();
        //initialize cat x and cat y in a way that the cat can be drawn at horizontally center and on top of the ground
        catX = dWidth / 2 - cat.getWidth() / 2;
        catY = dHeight - ground.getHeight() - cat.getHeight();
        spikes = new ArrayList<>();
        explosions = new ArrayList<>();
        for (int i = 0; i < 3; i++){
            Spike spike = new Spike(context);
            spikes.add(spike);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(background, null, rectBackground, null);
        canvas.drawBitmap(ground, null, rectGround, null);
        canvas.drawBitmap(cat, catX, catY, null);
        for (int i = 0; i < spikes.size(); i++){
            canvas.drawBitmap(spikes.get(i).getSpike(spikes.get(i).spikeFrame), spikes.get(i).spikeX, spikes.get(i).spikeY, null);
            // increment spike frame for the current element
            spikes.get(i).spikeFrame++;
            if (spikes.get(i).spikeFrame > 2){
                spikes.get(i).spikeFrame = 0;
            }
            //increment spikeY with spike velocity for a top down movement
            spikes.get(i).spikeY += spikes.get(i).spikeVelocity;
            // check if bottom edge of the spike touches the top edge of the ground
            if (spikes.get(i).spikeY + spikes.get(i).getSpikeHeight() >= dHeight - ground.getHeight()){
                // if true +10 points
                points += 10;
                Explosion explosion = new Explosion(context);
                explosion.explosionX = spikes.get(i).spikeX;
                explosion.explosionY = spikes.get(i).spikeY;

                //add this explosion object to the explosions released
                explosions.add(explosion);
                spikes.get(i).resetPosition();
            }
        }
        
        for (int i = 0; i < spikes.size(); i++){
            if (spikes.get(i).spikeX + spikes.get(i).getSpikeWidth() >= catX
            && spikes.get(i).spikeX <= catX + cat.getWidth()
            && spikes.get(i).spikeY + spikes.get(i).getSpikeWidth() >= catY
            && spikes.get(i).spikeY + spikes.get(i).getSpikeWidth() <= catY + cat.getHeight()){
                life--;
                spikes.get(i).resetPosition();
                if (life == 0){
                    Intent intent = new Intent(context, GameOver.class);
                    intent.putExtra("points", points);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
            }
        }

        for (int i = 0; i < explosions.size(); i++){
            canvas.drawBitmap(explosions.get(i).getExplosion(explosions.get(i).explosionFrame), explosions.get(i).explosionX, explosions.get(i).explosionY, null);
            // increment explosion frame for every element of explosions array list
            explosions.get(i).explosionFrame++;
            // once explosion frame for an element becomes greater than 3 remove the explosion object from array list since done showing the explosion animation
            if (explosions.get(i).explosionFrame > 3){
                explosions.remove(i);
            }
        }

        if (life == 2){
            healthPaint.setColor(Color.YELLOW);
        }else if(life == 1){
            healthPaint.setColor(Color.RED);
        }
        canvas.drawRect(dWidth - 200, 30, dWidth - 200 + 60 * life, 80, healthPaint);
        canvas.drawText("" + points, 20, TEXT_SIZE, textPaint);
        //call post delayed method on handler object which call the run method in runnable after 30 milliseconds
        handler.postDelayed(runnable, UPDATE_MILLIS);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        // consider only when touch is on or below the cat so check for the condition
        if (touchY >= catY) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                oldX = event.getX();
                oldCatX = catX;
            }
            if (action == MotionEvent.ACTION_MOVE) {
                float shift = oldX - touchX;
                float newCatX = oldCatX - shift;
                if (newCatX <= 0) {
                    catX = 0;
                } else if (newCatX >= dWidth - cat.getWidth()) {
                    catX = dWidth - cat.getWidth();
                } else {
                    catX = newCatX;
                }
            }
        }
        return true;
    }
}
