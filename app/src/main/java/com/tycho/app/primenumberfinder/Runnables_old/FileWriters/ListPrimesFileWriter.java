/*
package com.tycho.app.primenumberfinder.Runnables.FileWriters;

import android.os.Bundle;
import android.os.Message;

import com.tycho.app.primenumberfinder.Fragments.FragmentFindPrimes;

import java.util.ArrayList;
import java.util.List;

public class ListPrimesFileWriter implements Runnable{

    //Flow control booleans
    public boolean threadStopped = false;

    private List<Long> primes = new ArrayList<>();

    @Override
    public void run(){
        //Add all prime numbers to a private array so the other one can be reset and reused immediately
        //primes.addAll(FragmentFindPrimes.primeFinderThreadOldObject.primeNumbers);

        while (!threadStopped){
            //Create the file that will be written to
            String fileName = "Primes from " + FragmentFindPrimes.startValue + " to "
                    + ((FragmentFindPrimes.primeFinderThreadOldObject.currentNumberProgress == 100)
                    ? (FragmentFindPrimes.primeFinderThreadOldObject.currentNumber) : (FragmentFindPrimes.primeFinderThreadOldObject.currentNumber - 1)) + ".txt";

            //Ready for reset
            //TODO: resetting the primeFinderThreadOldObject before this may cause issues,
            //TODO: disable the button and re-enable here

            */
/*File dataFile = new File(PrimeNumberFinder.listPrimesDataDirectory.getAbsolutePath()  + File.separator + fileName);
            BufferedWriter bufferedWriter = null;

            //Check if the file already exists
            if (dataFile.exists()){
                sendMessage("fileAlreadyExists");
                break;
            }

            try {
                bufferedWriter = new BufferedWriter(new FileWriter(dataFile, true));

                for (long prime : primes){
                    bufferedWriter.write(String.valueOf(prime));

                    if (primes.indexOf(prime) != primes.size() - 1){
                        bufferedWriter.write("\n");
                    }
                }

                bufferedWriter.close();
            } catch (IOException e) {
                sendMessage("errorIOException");
                break;
            }catch (Exception e){
                sendMessage("errorUnknown");
                break;
            }*//*


            sendMessage("success");
            threadStopped = true;
        }
    }

    */
/**
     * Send a message to the handler
     *//*

    private void sendMessage(String msgData){
        Message message = FragmentFindPrimes.listPrimesFileWriterHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("msgData", msgData);
        message.setData(bundle);
        FragmentFindPrimes.listPrimesFileWriterHandler.sendMessage(message);
    }
}
*/
