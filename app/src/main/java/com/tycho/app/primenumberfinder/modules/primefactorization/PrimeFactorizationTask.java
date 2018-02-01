package com.tycho.app.primenumberfinder.modules.primefactorization;

import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import easytasks.Task;
import simpletrees.Tree;

/**
 * @author Tycho Bellers
 *         Date Created: 3/3/2017
 */

public class PrimeFactorizationTask extends Task {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "PrimeFactorizationTask";

    private final long number;

    private Tree<Long> factorTree;

    private Map<Long, Long> primeFactors = new TreeMap<>();

    private int total;

    public PrimeFactorizationTask(final long number) {
        this.number = number;
    }

    @Override
    public void run() {
        final FindFactorsTask findFactorsTask = new FindFactorsTask(new FindFactorsTask.SearchOptions(number, FindFactorsTask.SearchOptions.MonitorType.NONE));
        findFactorsTask.start();
        final List<Long> factors = findFactorsTask.getFactors();

        for (long n : factors){
            final CheckPrimalityTask checkPrimalityTask = new CheckPrimalityTask(n);
            checkPrimalityTask.start();
            if (checkPrimalityTask.isPrime()){
                total++;
            }
        }
        this.factorTree = generateTree(number);
    }

    private Tree<Long> generateTree(long number) {

        final Tree<Long> tree = new Tree<>(number);

        final FindFactorsTask findFactorsTask = new FindFactorsTask(new FindFactorsTask.SearchOptions(number, FindFactorsTask.SearchOptions.MonitorType.NONE));
        findFactorsTask.start();

        final int size = findFactorsTask.getFactors().size();

        if (size == 1){
            primeFactors.put(1L, 1L);
        }else if (size == 2) {
            //This number is prime, end of branch
            if (!primeFactors.containsKey(findFactorsTask.getNumber())){
                primeFactors.put(findFactorsTask.getNumber(), 1L);
            }else{
                primeFactors.put(findFactorsTask.getNumber(), primeFactors.get(findFactorsTask.getNumber()) + 1);
            }
        } else if (size == 3) {
            //This is a perfect square
            tree.addNode(findFactorsTask.getFactors().get(1));
            tree.addNode(findFactorsTask.getFactors().get(1));

            if (!primeFactors.containsKey(findFactorsTask.getFactors().get(1))){
                primeFactors.put(findFactorsTask.getFactors().get(1), 2L);
            }else{
                primeFactors.put(findFactorsTask.getFactors().get(1), primeFactors.get(findFactorsTask.getFactors().get(1)) + 2);
            }
        } else {

            long number1;
            long number2;

            if (size % 2 == 0) {
                number1 = findFactorsTask.getFactors().get((size / 2) - 1);
                number2 = findFactorsTask.getFactors().get((size / 2));
            } else {
                number1 = findFactorsTask.getFactors().get((size / 2));
                number2 = findFactorsTask.getFactors().get((size / 2));
            }

            setProgress(primeFactors.size() / total);

            tree.addNode(generateTree(number1));
            tree.addNode(generateTree(number2));
        }

        return tree;
    }

    public Tree<Long> getFactorTree() {
        return factorTree;
    }

    public Map<Long, Long> getPrimeFactors() {
        return primeFactors;
    }

    public long getNumber() {
        return number;
    }
}
