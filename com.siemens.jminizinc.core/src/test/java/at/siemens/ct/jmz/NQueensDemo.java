/**
 * Copyright Siemens AG, 2018
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package at.siemens.ct.jmz;

import static at.siemens.ct.jmz.expressions.bool.RelationalOperator.NEQ;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import at.siemens.ct.jmz.elements.constraints.Constraint;
import at.siemens.ct.jmz.elements.solving.SolvingStrategy;
import at.siemens.ct.jmz.executor.Executor;
import at.siemens.ct.jmz.executor.PipedMiniZincExecutor;
import at.siemens.ct.jmz.expressions.array.IntegerArray;
import at.siemens.ct.jmz.expressions.array.IntegerArrayAccessExpression;
import at.siemens.ct.jmz.expressions.bool.Forall;
import at.siemens.ct.jmz.expressions.bool.RelationalOperation;
import at.siemens.ct.jmz.expressions.comprehension.Generator;
import at.siemens.ct.jmz.expressions.comprehension.IteratorExpression;
import at.siemens.ct.jmz.expressions.integer.ArithmeticOperation;
import at.siemens.ct.jmz.expressions.set.RangeExpression;
import at.siemens.ct.jmz.writer.ModelWriter;

/**
 * Contains a full-blown example to solve the n-queens problem with JMiniZinc.
 */
@RunWith(Parameterized.class)
public class NQueensDemo {

	private int n;

	@Parameterized.Parameters
	public static Collection<Integer> n() {
		return Arrays.asList(4, 8, 10);
	}

	public NQueensDemo(int n) {
		this.n = n;
	}

	@Test
	public void nQueens() throws InterruptedException, IOException {
		RangeExpression range = new RangeExpression(1, n);
		IntegerArray q = IntegerArray.createVariable("q", range, range); // queen in column i is in row q[i]

		IteratorExpression<Integer> i = range.iterate("i");
		IteratorExpression<Integer> j = range.iterate("j");

		Generator<Integer> generatorIJ = new Generator<>(i, j).restrict(new RelationalOperation<>(i, NEQ, j));
		IntegerArrayAccessExpression q_i = q.access(i);
		IntegerArrayAccessExpression q_j = q.access(j);

		// forall(i,j in 1..n where i!=j)(q[i] != q[j])
		Constraint constraintRows = new Constraint(new Forall<>(generatorIJ, new RelationalOperation<>(q_i, NEQ, q_j)));

		// forall(i,j in 1..n where i!=j)(q[i] + i != q[j] + j)
		Constraint constraintDiags = new Constraint(
				new Forall<>(generatorIJ, new RelationalOperation<>(q_i.addTo(i), NEQ, q_j.addTo(j))));

		// forall(i,j in 1..n where i!=j)(q[i] - i != q[j] - j)
		Constraint constraintUpDown = new Constraint(new Forall<>(generatorIJ,
				new RelationalOperation<>(ArithmeticOperation.minus(q_i, i), NEQ, ArithmeticOperation.minus(q_j, j))));

		ModelBuilder model = new ModelBuilder();
		model.add(q, constraintRows, constraintDiags, constraintUpDown);

		ModelWriter writer = new ModelWriter(model);
		writer.setSolvingStrategy(SolvingStrategy.SOLVE_SATISFY);

		Executor executor = new PipedMiniZincExecutor("executor", writer);
		executor.startProcess(1000L);
		executor.waitForSolution();

		Integer[] result = q.parseResults(executor.getLastSolverOutput());
		System.out.println(Arrays.toString(result));

		assertTrue(allDifferent(result));
	}

	private boolean allDifferent(Integer[] integers) {
		Set<Integer> set = new HashSet<>();
		set.addAll(Arrays.asList(integers));
		return set.size() == integers.length;
	}

}
