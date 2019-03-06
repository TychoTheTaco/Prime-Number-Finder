package com.tycho.app.primenumberfinder;

import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest{
    @Test
    public void addition_isCorrect(){
        assertEquals(4, 2 + 2);
    }

    @Test
    public void find_factors_task(){
        final FindFactorsTask findFactorsTask = new FindFactorsTask(new FindFactorsTask.SearchOptions(100));
        findFactorsTask.start();
        assertEquals(findFactorsTask.getFactors(), Arrays.asList(1L, 2L, 4L, 5L, 10L, 20L, 25L, 50L, 100L));
    }
}