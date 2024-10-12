package com.bkacad.nnt.save_the_cat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Random;

public class Spike {
    // define a bitmap array for holding all the spike images
    Bitmap spike[] =new Bitmap[3];
    int spikeFrame = 0;
    int spikeX, spikeY, spikeVelocity;
    Random random;

    //define the constructor for this spike class
    public Spike(Context context){
        spike[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.spike);
        spike[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.spike_1);
        spike[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.spike_2);
        random = new Random();
        resetPosition();
    }

    public Bitmap getSpike(int spikeFrame){
        return spike[spikeFrame];
    }

    public int getSpikeWidth(){
        return spike[0].getWidth();
    }

    public int getSpikeHeight(){
        return spike[0].getHeight();
    }
    void resetPosition() {
        spikeX = random.nextInt(GameView.dWidth - getSpikeWidth());
        spikeY = -200 + random.nextInt(600) * -1;
        spikeVelocity = 35 + random.nextInt(16);
    }
}


