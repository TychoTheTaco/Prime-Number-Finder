package com.tycho.app.primenumberfinder.modules.findprimes;


import easytasks.Task;

/**
 * @author Tycho Bellers
 *         Date Created: 5/12/2017
 */

public class CheckPrimalityTask extends Task{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = CheckPrimalityTask.class.getSimpleName();

    private final long number;

    private boolean isPrime = false;

    private boolean isFinished = false;

    /**
     * The factor that was found causing this number not to be prime.
     */
    private int factor = 0;

    public CheckPrimalityTask(final long number){
        this.number = number;
    }

    private int i;
    private int sqrtMax;

    @Override
    public void run(){

        if (number > 2){

            //Check if the number is divisible by 2
            if (number % 2 != 0){

                /*
                 * Get the square root of the number. We only need to calculate up to the square
                 * root to determine if the number is prime. The square root of a long will
                 * always fit inside the value range of an int.
                 */
                sqrtMax = (int) Math.sqrt(number);

                //Assume the number is prime
                isPrime = true;

                //Check if the number is divisible by every odd number below it's square root.
                for (i = 3; i <= sqrtMax; i += 2){

                    //Check if the number divides perfectly
                    if (number % i == 0){
                        isPrime = false;
                        factor = i;
                        break;
                    }

                    //Check if we should pause
                    tryPause();
                    if (shouldStop()){
                        return;
                    }
                }
            }else{
                factor = 2;
            }
        }else{
            factor = 1;
        }

        if (number == 2) isPrime = true;

        //The task has finished
        isFinished = true;
    }

    @Override
    public float getProgress() {
        if (getState() != State.STOPPED){
            setProgress((float) i / sqrtMax);
        }
        return super.getProgress();
    }

    public boolean isPrime(){
        return isPrime;
    }

    public long getNumber() {
        return number;
    }

    public int getFactor() {
        return factor;
    }
}
