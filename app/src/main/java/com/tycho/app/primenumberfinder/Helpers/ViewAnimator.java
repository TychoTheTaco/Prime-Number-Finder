/*
package com.tycho.app.primenumberfinder.Helpers;

import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import com.tycho.app.primenumberfinder.Fragments.ScanForPrimesFragment;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;

public class ViewAnimator{
    //Text sizes of each view
    public static float currentNumberTextSize = spValue(42);
    public static float currentNumberLabelTextSize = spValue(24);
    public static float previousPrime0TextSize = spValue(36);
    public static float previousPrime0LabelTextSize = spValue(22);
    public static float previousPrime1TextSize = spValue(30);
    public static float previousPrime1LabelTextSize = spValue(20);
    public static float previousPrime2TextSize = spValue(24);
    public static float previousPrime2LabelTextSize = spValue(18);

    //Maximum width of textViewCurrentNumber until it needs to be resized
    private static float textViewCurrentNumberMaxWidth(){
        return pxValue(PrimeNumberFinder.displayMetrics.widthPixels) - (dpValue(5) + pxValue(ScanForPrimesFragment.textViewCurrentNumberLabelLeft.getWidth()) + dpValue(10) + dpValue(10) + pxValue(ScanForPrimesFragment.textViewCurrentNumberLabelRight.getWidth()) + dpValue(5));
    }

    //Maximum width of textViewPreviousPrime0 until it needs to be resized
    private static float previousPrime0MaxWidth(){
        return pxValue(PrimeNumberFinder.displayMetrics.widthPixels) - (dpValue(5) + pxValue(ScanForPrimesFragment.textViewPreviousPrime0LabelLeft.getWidth()) + dpValue(15) + dpValue(15) + pxValue(ScanForPrimesFragment.textViewPreviousPrime0LabelRight.getWidth()) + dpValue(5));
    }

    //Maximum width of textViewPreviousPrime1 until it needs to be resized
    private static float previousPrime1MaxWidth(){
        return pxValue(PrimeNumberFinder.displayMetrics.widthPixels) - (dpValue(5) + pxValue(ScanForPrimesFragment.textViewPreviousPrime1LabelLeft.getWidth()) + dpValue(20) + dpValue(20) + pxValue(ScanForPrimesFragment.textViewPreviousPrime1LabelRight.getWidth()) + dpValue(5));
    }

    //Maximum width of textViewPreviousPrime2 until it needs to be resized
    private static float previousPrime2MaxWidth(){
        return pxValue(PrimeNumberFinder.displayMetrics.widthPixels) - (dpValue(5) + pxValue(ScanForPrimesFragment.textViewPreviousPrime2LabelLeft.getWidth()) + dpValue(35) + dpValue(35) + pxValue(ScanForPrimesFragment.textViewPreviousPrime2LabelRight.getWidth()) + dpValue(5));
    }

    */
/**
     * Update all of the views
     *//*

    public static void updateViews(){
        updateCurrentNumberView();
        updatePreviousPrime0View();
        updatePreviousPrime1View();
        updatePreviousPrime2View();
    }

    */
/**
     * Reset all views and variables
     *//*

    public static void reset(){
        //Reset the text sizes
        currentNumberTextSize = spValue(42);
        currentNumberLabelTextSize = spValue(24);
        previousPrime0TextSize = spValue(36);
        previousPrime0LabelTextSize = spValue(22);
        previousPrime1TextSize = spValue(30);
        previousPrime1LabelTextSize = spValue(20);
        previousPrime2TextSize = spValue(24);
        previousPrime2LabelTextSize = spValue(18);

        //Apply the new text sizes
        FragmentFindPrimes.textViewCurrentNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentNumberTextSize);
        FragmentFindPrimes.textViewCurrentNumberLabelLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentNumberLabelTextSize);
        FragmentFindPrimes.textViewCurrentNumberLabelRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentNumberLabelTextSize);
        FragmentFindPrimes.textViewPreviousPrime0.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime0TextSize);
        FragmentFindPrimes.textViewPreviousPrime0LabelLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime0LabelTextSize);
        FragmentFindPrimes.textViewPreviousPrime0LabelRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime0LabelTextSize);
        FragmentFindPrimes.textViewPreviousPrime1.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime1TextSize);
        FragmentFindPrimes.textViewPreviousPrime1LabelLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime1LabelTextSize);
        FragmentFindPrimes.textViewPreviousPrime1LabelRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime1LabelTextSize);
        FragmentFindPrimes.textViewPreviousPrime2.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime2TextSize);
        FragmentFindPrimes.textViewPreviousPrime2LabelLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime2LabelTextSize);
        FragmentFindPrimes.textViewPreviousPrime2LabelRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime2LabelTextSize);

        //Reset padding
        FragmentFindPrimes.textViewCurrentNumber.setPaddingRelative(0, 0, 0, 0);
        FragmentFindPrimes.textViewCurrentNumberLabelLeft.setPaddingRelative(0, 0, 0, 0);
        FragmentFindPrimes.textViewCurrentNumberLabelRight.setPaddingRelative(0, 0, 0, 0);
        FragmentFindPrimes.textViewPreviousPrime0.setPaddingRelative(0, 0, 0, 0);
        FragmentFindPrimes.textViewPreviousPrime0LabelLeft.setPaddingRelative(0, 0, 0, 0);
        FragmentFindPrimes.textViewPreviousPrime0LabelRight.setPaddingRelative(0, 0, 0, 0);
        FragmentFindPrimes.textViewPreviousPrime1.setPaddingRelative(0, 0, 0, 0);
        FragmentFindPrimes.textViewPreviousPrime1LabelLeft.setPaddingRelative(0, 0, 0, 0);
        FragmentFindPrimes.textViewPreviousPrime1LabelRight.setPaddingRelative(0, 0, 0, 0);
        FragmentFindPrimes.textViewPreviousPrime2.setPaddingRelative(0, 0, 0, 0);
        FragmentFindPrimes.textViewPreviousPrime2LabelLeft.setPaddingRelative(0, 0, 0, 0);
        FragmentFindPrimes.textViewPreviousPrime2LabelRight.setPaddingRelative(0, 0, 0, 0);
    }

    */
/**
     * Update textViewCurrentNumber and all of the views that go with it, such as the labels
     *//*

    private static void updateCurrentNumberView(){
        //Rect that defines the View borders
        Rect bounds = new Rect();

        //Get the borders of the TextView
        FragmentFindPrimes.textViewCurrentNumber.getPaint().getTextBounds(FragmentFindPrimes.textViewCurrentNumber.getText().toString(), 0, FragmentFindPrimes.textViewCurrentNumber.getText().toString().length(), bounds);

        if (pxValue(bounds.width()) > textViewCurrentNumberMaxWidth()){
            //Lower the text size of the labels
            if (currentNumberLabelTextSize > spValue(16)){
                currentNumberLabelTextSize--;
            }

            //Move each label down
            if (pxValue(FragmentFindPrimes.textViewCurrentNumberLabelLeft.getPaddingTop()) < dpValue(40)
                    && pxValue(FragmentFindPrimes.textViewCurrentNumberLabelRight.getPaddingTop()) < dpValue(40)){
                translatePaddingRelative(FragmentFindPrimes.textViewCurrentNumberLabelLeft, 0, 5, 0, 0);
                translatePaddingRelative(FragmentFindPrimes.textViewCurrentNumberLabelRight, 0, 5, 0, 0);
            }

            //Move main TextView up
            //TODO: Change paddingTop to paddingBottom, it makes more sense because it wont be negative. Don't forget to change onSaveInstanceState in the fragment
            if (FragmentFindPrimes.textViewCurrentNumber.getPaddingTop() > -40){
                translatePaddingRelative(FragmentFindPrimes.textViewCurrentNumber, 0, -5, 0, 0);
            }
        }

        //Set the new text sizes
        FragmentFindPrimes.textViewCurrentNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentNumberTextSize);
        FragmentFindPrimes.textViewCurrentNumberLabelLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentNumberLabelTextSize);
        FragmentFindPrimes.textViewCurrentNumberLabelRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentNumberLabelTextSize);
    }

    */
/**
     * Update textViewPreviousPrime0 and all of the views that go with it, such as the labels
     *//*

    private static void updatePreviousPrime0View(){
        //Rect that defines the View borders
        Rect bounds = new Rect();

        //Get the borders of the TextView
        FragmentFindPrimes.textViewPreviousPrime0.getPaint().getTextBounds(FragmentFindPrimes.textViewPreviousPrime0.getText().toString(), 0, FragmentFindPrimes.textViewPreviousPrime0.getText().toString().length(), bounds);

        if (pxValue(bounds.width()) > previousPrime0MaxWidth()){
            //Lower the text size of the labels
            if (previousPrime0LabelTextSize > spValue(15)){
                previousPrime0LabelTextSize--;
            }

            //Move each label down
            if (pxValue(FragmentFindPrimes.textViewPreviousPrime0LabelLeft.getPaddingTop()) < dpValue(30)
                    && pxValue(FragmentFindPrimes.textViewPreviousPrime0LabelRight.getPaddingTop()) < dpValue(30)){
                translatePaddingRelative(FragmentFindPrimes.textViewPreviousPrime0LabelLeft, 0, 5, 0, 0);
                translatePaddingRelative(FragmentFindPrimes.textViewPreviousPrime0LabelRight, 0, 5, 0, 0);
            }

            //Move main TextView up
            if (FragmentFindPrimes.textViewPreviousPrime0.getPaddingTop() > -40){
                translatePaddingRelative(FragmentFindPrimes.textViewPreviousPrime0, 0, -5, 0, 0);
            }
        }

        //Set the new text sizes
        FragmentFindPrimes.textViewPreviousPrime0.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime0TextSize);
        FragmentFindPrimes.textViewPreviousPrime0LabelLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime0LabelTextSize);
        FragmentFindPrimes.textViewPreviousPrime0LabelRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime0LabelTextSize);
    }

    */
/**
     * Update textViewPreviousPrime1 and all of the views that go with it, such as the labels
     *//*

    private static void updatePreviousPrime1View(){
        //Rect that defines the View borders
        Rect bounds = new Rect();

        //Get the borders of the TextView
        FragmentFindPrimes.textViewPreviousPrime1.getPaint().getTextBounds(FragmentFindPrimes.textViewPreviousPrime1.getText().toString(), 0, FragmentFindPrimes.textViewPreviousPrime1.getText().toString().length(), bounds);

        if (pxValue(bounds.width()) > previousPrime1MaxWidth()){
            //Lower the text size of the labels
            if (previousPrime1LabelTextSize > spValue(14)){
                previousPrime1LabelTextSize--;
            }

            //Move each label down
            if (pxValue(FragmentFindPrimes.textViewPreviousPrime1LabelLeft.getPaddingTop()) < dpValue(30)
                    && pxValue(FragmentFindPrimes.textViewPreviousPrime1LabelRight.getPaddingTop()) < dpValue(30)){
                translatePaddingRelative(FragmentFindPrimes.textViewPreviousPrime1LabelLeft, 0, 5, 0, 0);
                translatePaddingRelative(FragmentFindPrimes.textViewPreviousPrime1LabelRight, 0, 5, 0, 0);
            }


            //Move main TextView up
            if (FragmentFindPrimes.textViewPreviousPrime1.getPaddingTop() > -40){
                translatePaddingRelative(FragmentFindPrimes.textViewPreviousPrime1, 0, -5, 0, 0);
            }
        }

        //Set the new text sizes
        FragmentFindPrimes.textViewPreviousPrime1.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime1TextSize);
        FragmentFindPrimes.textViewPreviousPrime1LabelLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime1LabelTextSize);
        FragmentFindPrimes.textViewPreviousPrime1LabelRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime1LabelTextSize);
    }

    */
/**
     * Update textViewPreviousPrime2 and all of the views that go with it, such as the labels
     *//*

    private static void updatePreviousPrime2View(){
        //Rect that defines the View borders
        Rect bounds = new Rect();

        //Get the borders of the TextView
        FragmentFindPrimes.textViewPreviousPrime2.getPaint().getTextBounds(FragmentFindPrimes.textViewPreviousPrime2.getText().toString(), 0, FragmentFindPrimes.textViewPreviousPrime2.getText().toString().length(), bounds);

        if (pxValue(bounds.width()) > previousPrime2MaxWidth()){
            //Lower the text size of the labels
            if (previousPrime2LabelTextSize > spValue(13)){
                previousPrime2LabelTextSize--;
            }

            //Move each label down
            if (pxValue(FragmentFindPrimes.textViewPreviousPrime2LabelLeft.getPaddingTop()) < dpValue(30)
                    && pxValue(FragmentFindPrimes.textViewPreviousPrime2LabelRight.getPaddingTop()) < dpValue(30)){
                translatePaddingRelative(FragmentFindPrimes.textViewPreviousPrime2LabelLeft, 0, 5, 0, 0);
                translatePaddingRelative(FragmentFindPrimes.textViewPreviousPrime2LabelRight, 0, 5, 0, 0);
            }


            //Move main TextView up
            if (FragmentFindPrimes.textViewPreviousPrime2.getPaddingTop() > -40){
                translatePaddingRelative(FragmentFindPrimes.textViewPreviousPrime2, 0, -5, 0, 0);
            }
        }

        //Set the new text sizes
        FragmentFindPrimes.textViewPreviousPrime2.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime2TextSize);
        FragmentFindPrimes.textViewPreviousPrime2LabelLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime2LabelTextSize);
        FragmentFindPrimes.textViewPreviousPrime2LabelRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, previousPrime2LabelTextSize);
    }

    */
/**
     * Converts a value in SP to its equivalent float value
     *
     * @param sp The value to convert (in SP)
     * @return The final floating point value
     *//*

    public static float spValue(float sp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, PrimeNumberFinder.displayMetrics);
    }

    */
/**
     * Converts a value in PX to its equivalent float value
     *
     * @param px The value to convert (in PX)
     * @return The final floating point value
     *//*

    public static float pxValue(float px){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, PrimeNumberFinder.displayMetrics);
    }

    */
/**
     * Converts a value in DP to its equivalent float value
     *
     * @param dp The value to convert (in DP)
     * @return The final floating point value
     *//*

    public static float dpValue(float dp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, PrimeNumberFinder.displayMetrics);
    }

    */
/**
     * Translates a view based on padding in any of the four basic directoins
     *
     * @param view   The view to translate
     * @param start  The amount of padding to translate at the start of the view
     * @param top    The amount of padding to translate at the top of the view
     * @param end    The amount of padding to translate at the end of the view
     * @param bottom The amount of padding to translate at the bottom of the view
     *//*

    private static void translatePaddingRelative(View view, int start, int top, int end, int bottom){
        start = view.getPaddingStart() + start;
        top = view.getPaddingTop() + top;
        end = view.getPaddingEnd() + end;
        bottom = view.getPaddingBottom() + bottom;

        view.setPaddingRelative(start, top, end, bottom);
    }
}
*/
