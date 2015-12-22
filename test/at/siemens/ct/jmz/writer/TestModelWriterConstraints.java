package at.siemens.ct.jmz.writer;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.siemens.ct.jmz.IModelBuilder;
import at.siemens.ct.jmz.IntArrayVar;
import at.siemens.ct.jmz.ModelBuilder;
import at.siemens.ct.jmz.elements.Element;
import at.siemens.ct.jmz.elements.IntSet;
import at.siemens.ct.jmz.elements.NullSolvingStrategy;
import at.siemens.ct.jmz.elements.constraints.ArrayElementInSet;

/**
 * Tests {@link ModelWriter} with {@link Element}s from {@link at.siemens.ct.jmz.elements.constraints}.
 * 
 * @author z003ft4a (Richard Taupe)
 *
 */
public class TestModelWriterConstraints {

  private IModelBuilder modelBuilder = new ModelBuilder();
  private IModelWriter modelWriter = new ModelWriter(modelBuilder,
      new NullSolvingStrategy());

  @Before
  public void setUp() {
    modelBuilder.reset();
  }

  /**
   * Creates an {@link ArrayElementInSet} constraint, writes its declaration to a string and checks the result.
   */
  @Test
  public void testCreateArrayElementInSetConstraintToString() {
    String setRangeName = "Range";
    String arrayName = "a";
    IntSet setRange = modelBuilder.createIntSet(setRangeName, 1, 3);
    IntArrayVar arrayVar = modelBuilder.createIntArrayVar(arrayName, setRange, IntSet.ALL_INTEGERS);
    Set<Integer> allowedValues = new HashSet<>();
    allowedValues.add(1);
    allowedValues.add(3);
    modelBuilder.add(new ArrayElementInSet(arrayVar, 1, allowedValues));
    String output = modelWriter.toString();

    StringBuilder expectedOutput = new StringBuilder();
    expectedOutput.append("set of int: Range = 1..3;");
    expectedOutput.append(System.lineSeparator());
    expectedOutput.append(
        "array[" + setRangeName + "] of var int: " + arrayName + ";");
    expectedOutput.append(System.lineSeparator());
    expectedOutput.append("constraint " + arrayName + "[1] in {1, 3};");
    Assert.assertEquals(expectedOutput.toString(), output);
  }

}