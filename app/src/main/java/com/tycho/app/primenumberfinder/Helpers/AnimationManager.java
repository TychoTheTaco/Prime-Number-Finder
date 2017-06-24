package com.tycho.app.primenumberfinder.Helpers;

import android.animation.Animator;
import android.graphics.Rect;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;


public class AnimationManager{
    //Length of animations in milliseconds
    private static final long ANIMATION_DURATION_MILLIS = 350;

    /*public static void animateCardView(final View view, final float viewEndingHeight, final float viewEndingMarginTop){
        //View height
        final float viewStartingHeight = view.getHeight();
        final float viewDeltaHeight = viewEndingHeight - viewStartingHeight;

        //View top margin
        final float viewStartingMarginTop = ((RelativeLayout.LayoutParams) view.getLayoutParams()).topMargin;
        final float viewDeltaMarginTop = viewEndingMarginTop - viewStartingMarginTop;

        //View LayoutParams for changing dimensions
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

        //Animation to be applied to the view
        Animation animation = new Animation(){
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t){
                //Change the height
                layoutParams.height = (int) (viewStartingHeight + (viewDeltaHeight * interpolatedTime));

                //Change the top margin
                layoutParams.topMargin = (int) (viewStartingMarginTop + (viewDeltaMarginTop * interpolatedTime));

                //Apply the layout changes
                view.setLayoutParams(layoutParams);
            }

            @Override
            public boolean willChangeBounds(){
                //This animation will change the boundaries of the view
                return true;
            }
        };

        animation.setAnimationListener(new Animation.AnimationListener(){
                                           @Override
                                           public void onAnimationStart(Animation animation){
                                               //If the view was invisible and is about to expand, make it visible
                                               if (view.getVisibility() == View.GONE && viewEndingHeight > 0 && viewEndingMarginTop > 0){
                                                   view.setVisibility(View.VISIBLE);
                                               }
                                           }

                                           @Override
                                           public void onAnimationEnd(Animation animation){
                                               //Make the view invisible if it has no height
                                               if (viewEndingHeight == 0 && viewEndingMarginTop == 0){
                                                   view.setVisibility(View.GONE);
                                               }

                                               //Change the button text
                                               if (view == SavedFilesFragment.cardViewPrimes){
                                                   //If it is expanded, change the button text to "collapse"
                                                   if (viewEndingHeight == SavedFilesFragment.hiddenView.getHeight()){
                                                       SavedFilesFragment.buttonExpandPrimes.setText(PrimeNumberFinder.getAppContext().getString(R.string.collapse_list));
                                                   }else if (viewEndingHeight == SavedFilesFragment.defaultPrimesCardHeight()){
                                                       SavedFilesFragment.buttonExpandPrimes.setText(PrimeNumberFinder.getAppContext().getString(R.string.expand_list));
                                                   }
                                               }else if (view == SavedFilesFragment.cardViewFactors){
                                                   //If it is expanded, change the button text to "collapse"
                                                   if (viewEndingHeight == SavedFilesFragment.hiddenView.getHeight()){
                                                       SavedFilesFragment.buttonExpandFactors.setText(PrimeNumberFinder.getAppContext().getString(R.string.collapse_list));
                                                   }else if (viewEndingHeight == SavedFilesFragment.defaultFactorsCardHeight()){
                                                       SavedFilesFragment.buttonExpandFactors.setText(PrimeNumberFinder.getAppContext().getString(R.string.expand_list));
                                                   }
                                               }
                                           }

                                           @Override
                                           public void onAnimationRepeat(Animation animation){

                                           }
                                       }

        );

        //Set the duration
        animation.setDuration(ANIMATION_DURATION_MILLIS);

        //Start the animation
        view.startAnimation(animation);
    }*/

    /**
     * Animate the entry of a view
     * @param view The view to animate
     * @param interpolator The interpolator to use
     * @param clicked True if this view was clicked by the user. This will add extra emphasis to the animation
     */
    public static void animateViewEntry(final View view, final Interpolator interpolator, boolean clicked){
        //If the view is already visible, start the animation
        if (view.getVisibility() == View.VISIBLE){

            //If there is already an ongoing animation, override it with a new one
            if (view.getAnimation() != null){
                //Create a new animation to scale the view from 0% to 100%
                ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
                scaleAnimation.setDuration(ANIMATION_DURATION_MILLIS);
                view.startAnimation(scaleAnimation);
            }

            //If this button was clicked, add extra emphasis to the animation
            if (clicked){
                //Create a new animation to scale the view from 90% to 100%, for emphasis
                ScaleAnimation scaleAnimation = new ScaleAnimation(.9f, 1, .9f, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
                scaleAnimation.setInterpolator(new OvershootInterpolator());
                scaleAnimation.setDuration(ANIMATION_DURATION_MILLIS);
                view.startAnimation(scaleAnimation);
            }
        }else{ //Else make the view visible and start a new animation
            view.setVisibility(View.VISIBLE);

            //Create a new animation to scale the view from 0% to 100%
            ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
            scaleAnimation.setDuration(ANIMATION_DURATION_MILLIS);

            //If this button was clicked, add extra emphasis to the animation
            if (clicked){
                scaleAnimation.setInterpolator(new OvershootInterpolator());
            }else{
                scaleAnimation.setInterpolator(interpolator);
            }

            //Start the animation
            view.startAnimation(scaleAnimation);
        }
    }

    /**
     * Animate the exit of a view
     * @param view The view to animate
     * @param interpolator The interpolator to use
     * @param endVisibility The visibility of the view after the animation. Either View.GONE or View.INVISIBLE
     * @param clicked True if this view was clicked by the user. This will add extra emphasis to the animation
     */
    public static void animateViewExit(final View view, final Interpolator interpolator, final int endVisibility, boolean clicked){
        //If the view is already visible, start the animation
        if (view.getVisibility() == View.VISIBLE){
            //Create a new animation to scale the view from 100% to 0%
            final ScaleAnimation exitAnimation = new ScaleAnimation(1, 0, 1, 0, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
            exitAnimation.setInterpolator(interpolator);

            //Create a new animation to scale the view from 100% to 110%, for emphasis
            final ScaleAnimation emphasisAnimation = new ScaleAnimation(1, 1.1f, 1, 1.1f, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
            emphasisAnimation.setDuration(ANIMATION_DURATION_MILLIS / 2);
            emphasisAnimation.setInterpolator(new OvershootInterpolator());
            emphasisAnimation.setAnimationListener(new Animation.AnimationListener(){
                @Override
                public void onAnimationStart(Animation animation){

                }

                @Override
                public void onAnimationEnd(Animation animation){
                    view.startAnimation(exitAnimation);
                }

                @Override
                public void onAnimationRepeat(Animation animation){

                }
            });

            //If this button was clicked, add extra emphasis to the animation
            if (clicked){
                exitAnimation.setDuration(ANIMATION_DURATION_MILLIS / 2);
                view.startAnimation(emphasisAnimation);
            }else{
                exitAnimation.setDuration(ANIMATION_DURATION_MILLIS);
                view.startAnimation(exitAnimation);
            }

            //Hide the view when it is gone
            exitAnimation.setAnimationListener(new Animation.AnimationListener(){
                @Override
                public void onAnimationStart(Animation animation){

                }

                @Override
                public void onAnimationEnd(Animation animation){
                    view.setVisibility(endVisibility);
                }

                @Override
                public void onAnimationRepeat(Animation animation){

                }
            });
        }

    }

    /**
     * Animate a view with a circular "reveal" pattern
     * @param view The view that will become visible
     * @param centerX X coordinate of the center of the circle
     * @param centerY Y coordinate of the center of the circle
     * @param rect A rectangle specifying the boundaries of the clipping circle
     */
    /*public static void reveal(final View view, int centerX, int centerY, Rect rect, boolean inverse){
        //Center of the circle relative to the rectangle bounds
        final float relativeCenterX = centerX - rect.left;
        final float relativeCenterY = centerY - rect.top;

        //Calculate the final radius for the clipping circle
        float finalRadius = 0;
        if (relativeCenterX < rect.centerX()){
            if (relativeCenterY < rect.centerY()){
                finalRadius = (float) Math.hypot(rect.width() - relativeCenterX, rect.height() - relativeCenterY);
            }else{
                finalRadius = (float) Math.hypot(rect.width() - relativeCenterX, rect.height() - (rect.height() - relativeCenterY));
            }
        }else{
            if (relativeCenterY < rect.centerY()){
                finalRadius = (float) Math.hypot(rect.width() - (rect.width() - relativeCenterX), rect.height() - relativeCenterY);
            }else{
                finalRadius = (float) Math.hypot(rect.width() - (rect.width() - relativeCenterX), rect.height() - (rect.height() - relativeCenterY));
            }
        }

        //Create the animator for this view
        Animator anim = null;
        if (inverse){
            anim = ViewAnimationUtils.createCircularReveal(view, centerX, centerY, finalRadius, 0);
            anim.addListener(new Animator.AnimatorListener(){
                @Override
                public void onAnimationStart(Animator animation){
                    SavedFilesFragment.cardViewPrimes.setVisibility(View.VISIBLE);
                    SavedFilesFragment.cardViewFactors.setVisibility(View.VISIBLE);
                    SavedFilesFragment.hiddenView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation){
                    view.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation){

                }

                @Override
                public void onAnimationRepeat(Animator animation){

                }
            });
        }else{
            anim = ViewAnimationUtils.createCircularReveal(view, centerX, centerY, 0, finalRadius);
            anim.addListener(new Animator.AnimatorListener(){
                @Override
                public void onAnimationStart(Animator animation){
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation){
                    SavedFilesFragment.cardViewPrimes.setVisibility(View.GONE);
                    SavedFilesFragment.cardViewFactors.setVisibility(View.GONE);
                    SavedFilesFragment.hiddenView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation){

                }

                @Override
                public void onAnimationRepeat(Animator animation){

                }
            });
        }
        anim.setDuration(ANIMATION_DURATION_MILLIS);

        //Make the view visible and start the animation
        view.setVisibility(View.VISIBLE);

        anim.start();
    }*/

    public static void scale(final View view, int endWidth, int endHeight){
        float endScaleX = endWidth / view.getWidth();
        float endScaleY = endHeight / view.getHeight();

        ScaleAnimation scaleAnimation = new ScaleAnimation(1, endScaleX, 1, endScaleY, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
        scaleAnimation.setInterpolator(new OvershootInterpolator());
        scaleAnimation.setDuration(ANIMATION_DURATION_MILLIS);
        view.startAnimation(scaleAnimation);
    }

    /**
     * Animate a view resizing
     * @param view The view to animate
     * @param viewEndingWidth The final width of the view after the animation
     * @param viewEndingHeight The final height of the view after the animation
     * @param endingMarginTop The final top margin of the view after the animation
     * @param endingMarginLeft The final left margin of the view after the animation
     * @param endingMarginRight The final right margin of the view after the animation
     * @param endingMarginBottom The final bottom margin of the view after the animation
     */
    public static void resize(final View view, int viewEndingWidth, int viewEndingHeight, float endingMarginTop, float endingMarginLeft, float endingMarginRight, float endingMarginBottom){
        //View width
        final float viewStartingWidth = view.getWidth();
        final float viewDeltaWidth = viewEndingWidth - viewStartingWidth;

        //View height
        final float viewStartingHeight = view.getHeight();
        final float viewDeltaHeight = viewEndingHeight - viewStartingHeight;

        //View top margin
        final float viewStartingMarginTop = ((RelativeLayout.LayoutParams) view.getLayoutParams()).topMargin;
        final float viewDeltaMarginTop = endingMarginTop - viewStartingMarginTop;

        //View left margin
        final float viewStartingMarginLeft = ((RelativeLayout.LayoutParams) view.getLayoutParams()).leftMargin;
        final float viewDeltaMarginLeft = endingMarginLeft - viewStartingMarginLeft;

        //View right margin
        final float viewStartingMarginRight = ((RelativeLayout.LayoutParams) view.getLayoutParams()).rightMargin;
        final float viewDeltaMarginRight = endingMarginRight - viewStartingMarginRight;

        //View bottom margin
        final float viewStartingMarginBottom = ((RelativeLayout.LayoutParams) view.getLayoutParams()).bottomMargin;
        final float viewDeltaMarginBottom = endingMarginBottom - viewStartingMarginBottom;

        //View LayoutParams for changing dimensions
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

        //Animation to be applied to the view
        Animation animation = new Animation(){
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t){
                //Change the width and height
                layoutParams.width = (int) (viewStartingWidth + (viewDeltaWidth * interpolatedTime));
                layoutParams.height = (int) (viewStartingHeight + (viewDeltaHeight * interpolatedTime));

                //Change the margins
                layoutParams.topMargin = (int) (viewStartingMarginTop + (viewDeltaMarginTop * interpolatedTime));
                layoutParams.leftMargin = (int) (viewStartingMarginLeft + (viewDeltaMarginLeft * interpolatedTime));
                layoutParams.rightMargin = (int) (viewStartingMarginRight + (viewDeltaMarginRight * interpolatedTime));
                layoutParams.bottomMargin = (int) (viewStartingMarginBottom + (viewDeltaMarginBottom * interpolatedTime));

                //Apply the layout changes
                view.setLayoutParams(layoutParams);
            }

            @Override
            public boolean willChangeBounds(){
                //This animation will change the boundaries of the view
                return true;
            }
        };

        //Set the interpolator
        animation.setInterpolator(new FastOutSlowInInterpolator());

        //Set the duration
        animation.setDuration(ANIMATION_DURATION_MILLIS);

        //Start the animation
        view.startAnimation(animation);
    }
}
