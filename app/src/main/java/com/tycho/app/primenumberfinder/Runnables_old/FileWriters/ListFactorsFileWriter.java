package com.tycho.app.primenumberfinder.Runnables_old.FileWriters;

import java.util.ArrayList;
import java.util.List;

public class ListFactorsFileWriter implements Runnable{

    //Flow control booleans
    public boolean threadStopped = false;

    private List<Long> factors = new ArrayList<>();

    @Override
    public void run(){
        //Add all prime numbers to a private array so the other one can be reset and reused immediately
        //factors.addAll(FindFactorsRunnable.factors);

        while (!threadStopped){
            //Create the file that will be written to
            //String fileName = "Factors of " + FindFactorsRunnable.inputNumber + ".txt";

            //Ready for reset
            //TODO: resetting the FindFactorsRunnable before this may cause issues,
            //TODO: disable the button and re-enable here

            /*File dataFile = new File(PrimeNumberFinder.listFactorsDataDirectory.getAbsolutePath()  + File.separator + fileName);
            BufferedWriter bufferedWriter = null;

            //Check if the file already exists
            if (dataFile.exists()){
                sendMessage("fileAlreadyExists");
                break;
            }

            try {
                bufferedWriter = new BufferedWriter(new FileWriter(dataFile, true));

                for (long factor : factors){
                    bufferedWriter.write(String.valueOf(factor));

                    if (factors.indexOf(factor) != factors.size() - 1){
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
            }*/

            sendMessage("success");
            threadStopped = true;
        }
    }

    /**
     * Send a message to the handler
     */
    private void sendMessage(String msgData){
       /* Message message = FindFactorsFragment.listFactorsFileWriterHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("msgData", msgData);
        message.setData(bundle);
        FindFactorsFragment.listFactorsFileWriterHandler.sendMessage(message);*/
    }
}
