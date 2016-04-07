package at.siemens.ct.jmz.executor;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.siemens.ct.jmz.IModelBuilder;
import at.siemens.ct.jmz.ModelBuilder;
import at.siemens.ct.jmz.elements.IntArrayVar;
import at.siemens.ct.jmz.elements.IntSet;
import at.siemens.ct.jmz.elements.IntVar;
import at.siemens.ct.jmz.elements.solving.SolvingStrategy;
import at.siemens.ct.jmz.writer.IModelWriter;
import at.siemens.ct.jmz.writer.ModelWriter;

/**
 * Tests {@link Executor}
 * 
 * @author z003ft4a (Richard Taupe)
 *
 */
public class TestExecutor {

  private IModelBuilder modelBuilder = new ModelBuilder();
  private IModelWriter modelWriter = new ModelWriter(modelBuilder);
  private IExecutor executor = new PipedMiniZincExecutor("test", modelWriter);

  @Before
  public void setUp() {
    modelBuilder.reset();
    modelWriter.setSolvingStrategy(SolvingStrategy.SOLVE_SATISFY);
  }

  @Test
  public void testSingleVariableGetOutput() throws IOException, InterruptedException {
    IntSet setOneTwoThree = new IntSet("OneTwoThree", 1, 3);
    IntVar i = new IntVar("i", setOneTwoThree);
    modelBuilder.add(setOneTwoThree, i);
    executor.startProcess();
    Assert.assertTrue(Executor.isRunning());
    executor.waitForSolution();
    Assert.assertFalse(Executor.isRunning());
    String lastSolverOutput = executor.getLastSolverOutput();

    StringBuilder expectedOutput = new StringBuilder();
    expectedOutput.append("i = 1;");
    expectedOutput.append(System.lineSeparator());
    expectedOutput.append("----------");

    Assert.assertEquals(expectedOutput.toString(), lastSolverOutput);
  }

  @Test
  public void testSingleVariableGetSolution() throws IOException, InterruptedException {
    IntSet setOneTwoThree = new IntSet("OneTwoThree", 1, 3);
    IntVar i = new IntVar("i", setOneTwoThree);
    modelBuilder.add(setOneTwoThree, i);
    executor.startProcess();
    Assert.assertTrue(Executor.isRunning());
    executor.waitForSolution();
    Assert.assertFalse(Executor.isRunning());
    int solI = i.parseResults(executor.getLastSolverOutput());
    Assert.assertTrue("Unexpected solution: i=" + solI, solI >= 1 && solI <= 3);
  }

  @Test
  public void testArrayGetSolution() throws IOException, InterruptedException {
    IntSet setOneTwoThree = new IntSet("OneTwoThree", 1, 3);
    IntArrayVar a = new IntArrayVar("a", setOneTwoThree, IntSet.ALL_INTEGERS);
    modelBuilder.add(setOneTwoThree, a);
    executor.startProcess();
    Assert.assertTrue(Executor.isRunning());
    executor.waitForSolution();
    Assert.assertFalse(Executor.isRunning());
    int[] solA = a.parseResults(executor.getLastSolverOutput());
    System.out.println(Arrays.toString(solA));
    Assert.assertEquals("Unexpected length of solution array: a=" + Arrays.toString(solA), 3,
        solA.length);
  }

}
