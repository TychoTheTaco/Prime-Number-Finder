package com.tycho.app.primenumberfinder.utils;

import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;

import java.math.BigInteger;
import java.util.List;

public class Validator {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = Validator.class.getSimpleName();

    public static boolean isPrimalityInputValid(final BigInteger input) {

        //Number must be less than or equal to max long value
        if (input.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) >= 0) {
            return false;
        }

        //Number must be greater than 0
        if (input.compareTo(BigInteger.ONE) < 0) {
            return false;
        }

        return true;
    }

    public static boolean isFindPrimesRangeValid(final BigInteger start, final BigInteger end, final FindPrimesTask.SearchMethod searchMethod) {

        //Number must be less than or equal to max long value
        if (start.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) >= 0 || end.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) >= 0) {
            return false;
        }

        //The start value must be at least 0
        if (start.compareTo(BigInteger.ZERO) < 0) {
            return false;
        }

        //Check if end value is equal to infinity
        if (end.compareTo(BigInteger.valueOf(FindPrimesTask.INFINITY)) != 0) {
            //End value must be greater than start value
            if (start.compareTo(end) >= 0) {
                return false;
            }
        }

        //Depends on search method
        switch (searchMethod) {
            case SIEVE_OF_ERATOSTHENES:

                /*
                The start value must be less than the max int value. The test for start < max has
                already been done.
                 */
                if (start.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) >= 0) {
                    return false;
                }

                //The end value cannot be infinity
                if (end.compareTo(BigInteger.valueOf(FindPrimesTask.INFINITY)) == 0) {
                    return false;
                }

                //The end value must be less than the max int value
                if (end.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) >= 0) {
                    return false;
                }

                break;

            case BRUTE_FORCE:
                break;
        }

        return true;
    }

    /**
     * Check if this is a valid number to factor
     *
     * @param input
     * @return
     */
    public static boolean isValidFactorInput(final BigInteger input) {

        //Number must be less than or equal to max long value
        if (input.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) >= 0) {
            return false;
        }

        //Number must be greater than 0
        if (input.compareTo(BigInteger.ONE) < 0) {
            return false;
        }

        return true;
    }

    public static boolean isValidLCMInput(final List<BigInteger> numbers) {
        //Must be at least 2 numbers
        if (numbers.size() < 2) return false;

        //Validate individual numbers
        for (BigInteger number : numbers) {
            if (!isValidLCMInput(number)) return false;
        }

        return true;
    }

    public static boolean isValidLCMInput(final BigInteger number) {
        //Number must be less than or equal to max long value
        if (number.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) >= 0) {
            return false;
        }

        //Number cannot be 0, 1, or -1
        if (number.compareTo(BigInteger.ZERO) == 0 || number.compareTo(BigInteger.ONE) == 0 || number.compareTo(BigInteger.valueOf(-1)) == 0){
            return false;
        }

        return true;
    }
}
