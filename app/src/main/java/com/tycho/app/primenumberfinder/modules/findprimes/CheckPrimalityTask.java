package com.tycho.app.primenumberfinder.modules.findprimes;


import easytasks.Task;

/**
 * @author Tycho Bellers
 *         Date Created: 5/12/2017
 */

public class CheckPrimalityTask extends Task {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "CheckPrimalityTask";

    private final long number;

    private boolean isPrime = false;

    public boolean finishedSearch = false;

    public CheckPrimalityTask(final long number){
        this.number = number;
    }

    @Override
    public void run(){

        boolean isFinished = true;

        if (number > 2){

            //Check if the number is divisible by 2
            if (number % 2 != 0){

                /*
                 * Get the square root of the number. We only need to calculate up to the square
                 * root to determine if the number is prime. The square root of a long will
                 * always fit inside the value range of an int.
                 */
                final int sqrtMax = (int) Math.sqrt(number);

                //Assume the number is prime
                isPrime = true;

                /*
                 * Check if the number is divisible by every odd number below it's square root.
                 */
                for (int i = 3; i <= sqrtMax; i += 2){

                    //Check if the number divides perfectly
                    if (number % i == 0){
                        isPrime = false;
                        break;
                    }

                    setProgress((float) i / sqrtMax);

                    //Check if we should pause
                    tryPause();
                    if (shouldStop()){
                        //running = false;
                        isFinished = false;
                        break;
                    }
                }
            }
        }

        if (number == 2) isPrime = true;

        //The task has finished
        if (isFinished){
            finishedSearch = true;
        }

        setProgress(1);
    }

    public boolean isPrime(){
        return isPrime;
    }

    public long getNumber() {
        return number;
    }
}
