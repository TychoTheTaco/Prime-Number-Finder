/*
package com.tycho.app.primenumberfinder.Runnables;

import android.os.Bundle;
import android.os.Message;

import com.tycho.app.primenumberfinder.Fragments.FragmentFindPrimes;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;

import java.util.ArrayList;
import java.util.List;

public class PrimeFinderThreadOld implements Runnable{
    //Flow control booleans
    public boolean threadRunning = false;
    public boolean scanningNumbers = false;
    public boolean stoppedScanning = true;
    public boolean threadStopped = true;

    //Current number being scanned
    public long currentNumber = 2;

    //Scan progress of current number
    public double currentNumberProgress = 0;

    //List of all found prime numbers
    public List<Long> primeNumbers = new ArrayList<>();
	
	//Buffer for numbers per second
	public List<Double> numbersPerSecondArray = new ArrayList<>();

    //Times for elapsed time calculation
    public long scanStartTime = 0;
    public long threadStartTime = 0;
    public long pausedTime = 0;
    public long elapsedTime = 0;
    public long lastUpdateTime = 0;
    public long loopStartTime = 0;
    public long loopEndTime = 0;
    public long lastOneSecondUpdateTime;

    //Maximum number to check when scanning for primes
    private double sqrtMax = 0;

    //Numbers scanned per second
    public int numbersPerSecond = 0;
    public int numbersSinceLastSecond = 0;

    //Prime numbers found per second
    public int primesPerSecond = 0;
    public int primesSinceLastSecond = 0;

    @Override
    public void run(){
        while (threadRunning){
            threadStopped = false;
            scanningNumbersLoop: while (scanningNumbers){
                stoppedScanning = false;
                loopStartTime = System.nanoTime();
                currentNumberProgress = 0;

                boolean isPrime = true;
                sqrtMax = Math.round(Math.sqrt(currentNumber));
                for (int i = 2; i <= sqrtMax; i++){
                    doUpdates(i);

                    //Check if number is prime
                    double actualNumber =  (double) currentNumber / i;
                    double roundedNumber = Math.round(actualNumber);

                    //Number is not prime, break out of the loop
                    if (actualNumber == roundedNumber){
                        isPrime = false;
                        break;
                    }

                    if (!threadRunning | !scanningNumbers){
                        break scanningNumbersLoop;
                    }
                }

                //Number is prime, add it to the list
                if (isPrime){
                    primeNumbers.add(currentNumber);
                    primesSinceLastSecond++;
                }

                //Stop thread if end value was reached
                if (currentNumber == FragmentFindPrimes.endValue){
                    scanningNumbers = false;
                    currentNumberProgress = 100;
                    sendUpdateMessage("endValueReached");
                    break;
                }

                //Increase variables
                currentNumber++;
                numbersSinceLastSecond++;
                loopEndTime = System.nanoTime();
            }
            //Thread has stopped scanning for prime numbers
            stoppedScanning = true;
        }
        //Thread has stopped completely
        threadStopped = true;
    }

    */
/**
     * Called every few milliseconds to update elapsed time and other stats
     * @param i Current iteration of the loop
     *//*

    private void doUpdates(int i){
        //Update current number progress
        currentNumberProgress = (i / sqrtMax) * 100;

        //Update the time elapsed
        elapsedTime = System.currentTimeMillis() - scanStartTime;

        //Update every second
        if (System.currentTimeMillis() - lastOneSecondUpdateTime >= 1000){
            numbersPerSecond = numbersSinceLastSecond;
            numbersSinceLastSecond = 0;

            primesPerSecond = primesSinceLastSecond;
            primesSinceLastSecond = 0;

            lastOneSecondUpdateTime = System.currentTimeMillis();
        }

        //Update ui
        if (System.currentTimeMillis() - lastUpdateTime >= PrimeNumberFinder.UPDATE_LIMIT_MS){
            lastUpdateTime = System.currentTimeMillis();
            sendUpdateMessage(null);
        }
    }

    */
/**
     * Send a message to the handler telling the ui to update
     *//*

    private void sendUpdateMessage(String msgData){
        Message message = FragmentFindPrimes.handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("currentNumber", String.valueOf(currentNumber));
        bundle.putString("msgData", msgData);
        message.setData(bundle);
        FragmentFindPrimes.handler.sendMessage(message);
    }
}
*/
