package com.tycho.app.primenumberfinder.tasks;

import com.tycho.app.primenumberfinder.TreeNode;
import com.tycho.app.primenumberfinder.utils.Task;

/**
 * @author Tycho Bellers
 *         Date Created: 3/3/2017
 */

public class FactorTreeTask extends Task{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FactorTreeTask";

    private final long number;

    private TreeNode factorTree;

    public FactorTreeTask(final long number){
        this.number = number;
    }

    @Override
    public void run(){
        dispatchStarted();

        this.factorTree = generateTree(number);

        dispatchFinished();

    }

    public TreeNode getFactorTree(){
        return factorTree;
    }

    private TreeNode generateTree(long number){

        final TreeNode treeNode = new TreeNode(number);

        final FindFactorsTask findFactorsTask = new FindFactorsTask(number);
        findFactorsTask.run();

        //if (findFactorsTask.getFactors().size() == 2 && findFactorsTask.getFactors().containsAll(Arrays.asList(new long[]{1, number}))) break;

        //Log.e(TAG, "Factors of " + number + " are " + findFactorsTask.getFactors());
        //Log.e(TAG, "Contains " + (findFactorsTask.getFactors().contains(1L) && findFactorsTask.getFactors().contains(number)));

        int size = findFactorsTask.getFactors().size();

        if (size == 2 && (findFactorsTask.getFactors().contains(1L) && findFactorsTask.getFactors().contains(number))){
            //Log.e(TAG, "Finished with " + number);
            //treeNode.addNode(new TreeNode(number));
        }else if (size == 3){
            treeNode.addNode(new TreeNode(findFactorsTask.getFactors().get((size / 2))));
            treeNode.addNode(new TreeNode(findFactorsTask.getFactors().get((size / 2))));
        }else{

            long number1;
            long number2;
            int offset = 0;

            do{
                if (size % 2 == 0){
                    number1 = findFactorsTask.getFactors().get((size / 2) - offset);
                    number2 = findFactorsTask.getFactors().get((size / 2));
                }else{
                    number1 = findFactorsTask.getFactors().get((size / 2));
                    number2 = findFactorsTask.getFactors().get((size / 2) + offset);
                }
                offset++;
            }while (number1 * number2 != number);


            //Log.e(TAG, "Two numbers are: " + number1 + " and " + number2 + " Total: " + (number1 * number2));

            treeNode.addNode(generateTree(number1));
            treeNode.addNode(generateTree(number2));
        }

        return treeNode;


    }
}
