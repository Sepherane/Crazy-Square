package com.example.bob.crazysquare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.media.MediaPlayer;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Animal[] animals;
    private int currentAnimal = 0;
    private ImageView animal;
    MediaPlayer mp;
    Context context = this;
    private ScaleGestureDetector mScaleGestureDetector;
    private int animalWidth = 250;
    private float scaleFactor = 1.0f;
    private float horizontalBias, verticalBias = 0.5f;
    private long touchStart = 0;

    private boolean longpressed = false;
    private boolean pressedDown = false;

    private boolean moving = false;

    private ConstraintLayout layout;

    private int mActivePointerId = -1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: started main activity");

        layout = findViewById(R.id.ConstraintLayout);

        animals = new Animal[]{new Animal(getResources().getIdentifier("@drawable/cat", null, this.getPackageName()), getResources().getIdentifier("cat", "raw", this.getPackageName())),
                new Animal(getResources().getIdentifier("@drawable/dog", null, this.getPackageName()), getResources().getIdentifier("dog", "raw", this.getPackageName())),
                new Animal(getResources().getIdentifier("@drawable/bird", null, this.getPackageName()), getResources().getIdentifier("bird", "raw", this.getPackageName()))};

        animal = (ImageView) findViewById(R.id.animal);


        mp = MediaPlayer.create(context, animals[currentAnimal].getSound());

        animal.setOnTouchListener(new View.OnTouchListener(){
            private float firstTouchX, firstTouchY = 0f;
            private float lastTouchX, lastTouchY = 0f;

            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                // Let the ScaleGestureDetector inspect all events.
                mScaleGestureDetector.onTouchEvent(ev);

                final int action = ev.getActionMasked();

                switch (action) {
                    case MotionEvent.ACTION_DOWN: {
                        if(mActivePointerId == -1) {
                            if(!pressedDown)
                                handler.postDelayed(mLongPressed, ViewConfiguration.getLongPressTimeout());
                            touchStart = Calendar.getInstance().getTimeInMillis();
                            moving = false;
                            firstTouchX = ev.getRawX();
                            lastTouchX = ev.getRawX();
                            firstTouchY = ev.getRawY();
                            lastTouchY = ev.getRawY();

                            // Save the ID of this pointer (for dragging)
                            mActivePointerId = ev.getPointerId(0);
                            pressedDown = true;
                        }
                        if(ev.getPointerCount() > 1)
                            handler.removeCallbacks(mLongPressed);
                        Log.d(TAG, "onTouch: "+ev.getPointerCount());
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        if(Math.abs(firstTouchX - ev.getRawX()) > 10 && Math.abs(firstTouchY - ev.getRawY()) > 10 && ev.getPointerCount() < 2 && !longpressed) {
                            handler.removeCallbacks(mLongPressed);
                            moving = true;
                            // Find the index of the active pointer and fetch its position
                            final int pointerIndex = ev.findPointerIndex(mActivePointerId);

                            final float x = ev.getRawX();
                            final float y = ev.getRawY();

                            // Calculate the distance moved
                            final float dx = x - lastTouchX;
                            final float dy = y - lastTouchY;

                            DisplayMetrics displayMetrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                            int height = displayMetrics.heightPixels;
                            int width = displayMetrics.widthPixels;

                            horizontalBias += dx / width*2;
                            horizontalBias = Math.min(Math.max(horizontalBias, 0f), 1f);

                            verticalBias += dy / height*2;
                            verticalBias = Math.min(Math.max(verticalBias, 0f), 1f);

                            ConstraintLayout.LayoutParams lparams = (ConstraintLayout.LayoutParams) animal.getLayoutParams();
                            lparams.verticalBias = verticalBias;
                            lparams.horizontalBias = horizontalBias;
                            animal.setLayoutParams(lparams);

                            // Remember this touch position for the next move event
                            lastTouchX = x;
                            lastTouchY = y;
                        }

                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        if(!moving)
                            touchEvent(ev);
                        handler.removeCallbacks(mLongPressed);
                        longpressed = false;
                        pressedDown = false;
                        moving = false;
                        mActivePointerId = -1;
                        break;
                    }

                    case MotionEvent.ACTION_CANCEL: {
                        handler.removeCallbacks(mLongPressed);
                        longpressed = false;
                        pressedDown = false;
                        mActivePointerId = -1;
                        break;
                    }

                    case MotionEvent.ACTION_POINTER_UP: {
                        if(!moving)
                            touchEvent(ev);
                        longpressed = false;
                        handler.removeCallbacks(mLongPressed);
                        moving = false;
                        pressedDown = false;
                        mActivePointerId = -1;
                        break;
                    }
                }
                return true;
            }
        });

        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        animal.setImageResource(animals[currentAnimal].getImage());
    }

    private void touchEvent(MotionEvent ev){
        long timedifference = Calendar.getInstance().getTimeInMillis() - touchStart;
        if(!longpressed && timedifference < 500)
            playSound(animals[currentAnimal].getSound());
    }

    private void playSound(int sound){
        try {
            if (mp.isPlaying()) {
                mp.stop();
                mp.release();
            }
            mp = MediaPlayer.create(context, sound);
            mp.start();
        } catch(Exception e) { e.printStackTrace(); }
    }

    final Handler handler = new Handler();
    Runnable mLongPressed = new Runnable() {
        public void run() {
            Log.i("", "Long press!");
            longpressed = true;
            currentAnimal = (currentAnimal + 1) % animals.length;
            animal.setImageResource(animals[currentAnimal].getImage());
        }
    };


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            scaleFactor *= scaleGestureDetector.getScaleFactor();
            scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 2.0f));
            ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) animal.getLayoutParams();
            params.width = Math.round(animalWidth * scaleFactor);
            params.height = params.width;
            animal.setLayoutParams(params);
            return true;
        }
    }
}

