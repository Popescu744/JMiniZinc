/**
 * Copyright Siemens AG, 2016-2017
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package at.siemens.ct.jmz.expressions.bool;

/**
 * @author Copyright Siemens AG, 2016-2017
 */
public class Disjunction extends BinaryLogicalOperation {

  public Disjunction(BooleanExpression disjunct1, BooleanExpression disjunct2) {
    super(disjunct1, BinaryLogicalOperator.Or, disjunct2);
  }

  @Override
  public Disjunction substitute(String name, Object value) {
    return new Disjunction(operand1.substitute(name, value), operand2.substitute(name, value));
  }

}
