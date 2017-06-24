package com.tycho.app.primenumberfinder.Handlers;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import com.tycho.app.primenumberfinder.Runnables_old.BackgroundAnimator;

public class BackgroundAnimatorHandler extends Handler{
    public BackgroundAnimatorHandler(Looper looper){
        super(looper);
    }

    @Override
    public void handleMessage(Message msg){
        BackgroundAnimator backgroundAnimator = (BackgroundAnimator) msg.obj;

        if (!backgroundAnimator.stopped){
            View view = backgroundAnimator.view;
            view.setBackgroundColor(Color.argb(255, (int) (backgroundAnimator.r * 255), (int) (backgroundAnimator.g * 255), (int) (backgroundAnimator.b * 255)));

            /*try{
                Settings.background.setBackgroundColor(Color.argb(255,
                        (int) ((MainActivity.backgroundAnimator.r) * 255),
                        (int) ((MainActivity.backgroundAnimator.g) * 255),
                        (int) ((MainActivity.backgroundAnimator.g) * 255)));
            }catch (Exception e){

            }

            try{
                About.background.setBackgroundColor(Color.argb(255,
                        (int) ((MainActivity.backgroundAnimator.r) * 255),
                        (int) ((MainActivity.backgroundAnimator.g) * 255),
                        (int) ((MainActivity.backgroundAnimator.g) * 255)));
            }catch (Exception e){

            }*/

            /*if (view == MainActivity.textViewCurrentNumber.getRootView()){
                MainActivity.startButton.getBackground().setColorFilter(Color.argb(255,
                        (int) ((MainActivity.backgroundAnimator.r + 0.4f) * 255),
                        (int) ((MainActivity.backgroundAnimator.g + 0.4f) * 255),
                        (int) ((MainActivity.backgroundAnimator.g + 0.4f) * 255)),
                        PorterDuff.Mode.MULTIPLY);

                MainActivity.stopButton.getBackground().setColorFilter(Color.argb(255,
                                (int) ((MainActivity.backgroundAnimator.r + 0.4f) * 255),
                                (int) ((MainActivity.backgroundAnimator.g + 0.4f) * 255),
                                (int) ((MainActivity.backgroundAnimator.g + 0.4f) * 255)),
                        PorterDuff.Mode.MULTIPLY);

                MainActivity.resetButton.getBackground().setColorFilter(Color.argb(255,
                                (int) ((MainActivity.backgroundAnimator.r + 0.4f) * 255),
                                (int) ((MainActivity.backgroundAnimator.g + 0.4f) * 255),
                                (int) ((MainActivity.backgroundAnimator.g + 0.4f) * 255)),
                        PorterDuff.Mode.MULTIPLY);

                MainActivity.saveButton.getBackground().setColorFilter(Color.argb(255,
                                (int) ((MainActivity.backgroundAnimator.r + 0.4f) * 255),
                                (int) ((MainActivity.backgroundAnimator.g + 0.4f) * 255),
                                (int) ((MainActivity.backgroundAnimator.g + 0.4f) * 255)),
                        PorterDuff.Mode.MULTIPLY);

                MainActivity.openLatestButton.getBackground().setColorFilter(Color.argb(255,
                                (int) ((MainActivity.backgroundAnimator.r + 0.4f) * 255),
                                (int) ((MainActivity.backgroundAnimator.g + 0.4f) * 255),
                                (int) ((MainActivity.backgroundAnimator.g + 0.4f) * 255)),
                        PorterDuff.Mode.MULTIPLY);

                MainActivity.actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255,
                        (int) ((MainActivity.backgroundAnimator.r + 0.4f) * 255),
                        (int) ((MainActivity.backgroundAnimator.g + 0.4f) * 255),
                        (int) ((MainActivity.backgroundAnimator.g + 0.4f) * 255))));

                try{
                    Settings.actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255,
                            (int) ((MainActivity.backgroundAnimator.r + 0.4f) * 255),
                            (int) ((MainActivity.backgroundAnimator.g + 0.4f) * 255),
                            (int) ((MainActivity.backgroundAnimator.g + 0.4f) * 255))));
                }catch (Exception e){

                }

                try{
                    About.actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255,
                            (int) ((MainActivity.backgroundAnimator.r + 0.4f) * 255),
                            (int) ((MainActivity.backgroundAnimator.g + 0.4f) * 255),
                            (int) ((MainActivity.backgroundAnimator.g + 0.4f) * 255))));
                }catch (Exception e){

                }

                MainActivity.window.setStatusBarColor(Color.argb(255,
                        (int) ((MainActivity.backgroundAnimator.r - 0.2f) * 255),
                        (int) ((MainActivity.backgroundAnimator.g - 0.2f) * 255),
                        (int) ((MainActivity.backgroundAnimator.g - 0.2f) * 255)));

                try{
                    Settings.window.setStatusBarColor(Color.argb(255,
                            (int) ((MainActivity.backgroundAnimator.r - 0.2f) * 255),
                            (int) ((MainActivity.backgroundAnimator.g - 0.2f) * 255),
                            (int) ((MainActivity.backgroundAnimator.g - 0.2f) * 255)));
                }catch (Exception e){

                }

                try{
                    About.window.setStatusBarColor(Color.argb(255,
                            (int) ((MainActivity.backgroundAnimator.r - 0.2f) * 255),
                            (int) ((MainActivity.backgroundAnimator.g - 0.2f) * 255),
                            (int) ((MainActivity.backgroundAnimator.g - 0.2f) * 255)));
                }catch (Exception e){

                }
            }*/
        }
    }
}
