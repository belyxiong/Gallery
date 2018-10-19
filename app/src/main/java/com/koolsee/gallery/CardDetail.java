package com.koolsee.gallery;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

public class CardDetail extends Activity{

    private ImageView image1,image2,image3,image4,image5;

    private ScaleAnimation sato0 = new ScaleAnimation(1,0,1,1,
            Animation.RELATIVE_TO_PARENT,0.5f,Animation.RELATIVE_TO_PARENT,0.5f);

    private ScaleAnimation sato1 = new ScaleAnimation(0,1,1,1,
            Animation.RELATIVE_TO_PARENT,0.5f,Animation.RELATIVE_TO_PARENT,0.5f);


    float mPreviousX;
    float mPreviousY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carddetail);

        initView();

        findViewById(R.id.carddetail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });
        findViewById(R.id.carddetail).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mPreviousX  = motionEvent.getX();
                    mPreviousY  = motionEvent.getY();
                    return false;
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                    if ((motionEvent.getX() - mPreviousX) > 50 || (motionEvent.getY() - mPreviousY) > 50) {
                        //swipe
                        if (image1.getVisibility() == View.VISIBLE){
                            image1.startAnimation(sato0);
                        }else{
                            image2.startAnimation(sato0);
                        }
                        return true;
                    }
                }
                return false;
            }
        });
        findViewById(R.id.carddetail).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder ab = new AlertDialog.Builder(CardDetail.this);
                ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                ab.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                ab.setMessage(R.string.confirmdelete);
                ab.setTitle(R.string.hint);
                ab.show();
                return true;
            }
        });
    }

    private void showImage1(){

        image1.setVisibility(View.VISIBLE);
        image2.setVisibility(View.GONE);
    }
    private void showImage2(){

        image1.setVisibility(View.GONE);
        image2.setVisibility(View.VISIBLE);
    }

    private void initView(){
        image1 = (ImageView) findViewById(R.id.iamge1);
        image2 = (ImageView) findViewById(R.id.iamge2);
        showImage1();
        sato0.setDuration(500);
        sato1.setDuration(500);

        sato0.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (image1.getVisibility() == View.VISIBLE){
                    image1.setAnimation(null);
                    showImage2();
                    image2.startAnimation(sato1);
                }else{
                    image2.setAnimation(null);
                    showImage1();
                    image1.startAnimation(sato1);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

}