/**
 * Copyright Siemens AG, 2016-2017
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package at.siemens.ct.jmz.diag.hsdag;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import at.siemens.ct.jmz.diag.AbstractConflictDetection;
import at.siemens.ct.jmz.diag.ConsistencyChecker;
import at.siemens.ct.jmz.diag.DiagnoseProgressCallback;
import at.siemens.ct.jmz.diag.DiagnosisException;
import at.siemens.ct.jmz.diag.DiagnosisMetadata;
import at.siemens.ct.jmz.diag.QuickXPlain;
import at.siemens.ct.jmz.diag.SimpleConflictDetection;
import at.siemens.ct.jmz.elements.Element;
import at.siemens.ct.jmz.elements.constraints.Constraint;

public class ConflictDetectionHSDAG extends HSDAG {

	private DiagnosesCollection conflictSets;
	private AbstractConflictDetection conflictDetection;

  public ConflictDetectionHSDAG(String mznFullFileName, Set<Constraint> userConstraints,
      DiagnoseProgressCallback progressCallback, ConflictDetectionAlgorithm conflictDetectionAlgorithm)
          throws FileNotFoundException {
    this(mznFullFileName, Collections.emptySet(), userConstraints, progressCallback, conflictDetectionAlgorithm);
  }

  public ConflictDetectionHSDAG(Collection<Element> fixedModel, Set<Constraint> userConstraints,
      DiagnoseProgressCallback progressCallback, ConflictDetectionAlgorithm conflictDetectionAlgorithm) {
    super(fixedModel, userConstraints, progressCallback, conflictDetectionAlgorithm);
    init(null, fixedModel, conflictDetectionAlgorithm);
  }

  public ConflictDetectionHSDAG(String mznFullFileName, Collection<? extends Element> fixedModel,
      Set<Constraint> userConstraints, DiagnoseProgressCallback progressCallback,
      ConflictDetectionAlgorithm conflictDetectionAlgorithm) throws FileNotFoundException {
    super(mznFullFileName, userConstraints, progressCallback, conflictDetectionAlgorithm);
    init(mznFullFileName, fixedModel, conflictDetectionAlgorithm);
  }

  private void init(String mznFullFileName, Collection<? extends Element> fixedModel,
      ConflictDetectionAlgorithm conflictDetectionAlgorithm) {
    try {
      conflictSets = new DiagnosesCollection();
      switch (conflictDetectionAlgorithm) {
      case SimpleConflictDetection:
        this.conflictDetection = new SimpleConflictDetection(mznFullFileName, fixedModel);
        break;
      case QuickXPlain:
        this.conflictDetection = new QuickXPlain(mznFullFileName, fixedModel);
        break;
      default:
        throw new IllegalArgumentException(
            String.format("No such conflict detection algoritm: %s", conflictDetectionAlgorithm));
      }
    } catch (FileNotFoundException fnfe) {
      fnfe.printStackTrace();
    }
  }

	@Override
  protected void buildDiagnosesTree(TreeNode root, DiagnosesCollection diagnosesCollection) throws DiagnosisException {
		Set<Constraint> minCS;
		Set<Constraint> difference;
		TreeNode treeNode;

		Set<Constraint> rootData = root.getData();
		Set<Constraint> rootInitialConstraintsSet = root.getInitialConstraintsSet();

		for (Constraint constraint : rootData) {
			difference = removeConstraintFromList(rootInitialConstraintsSet, constraint);
			if (progressCallback != null) {
				progressCallback.displayMessage("");
				displayNodeConstraint(root, constraint);
			}

			minCS = conflictDetection.getMinConflictSet(difference);

			if (progressCallback != null) {
				progressCallback.minConflictSet(minCS, difference, "");
			}

      if (minCS == null || minCS.isEmpty()) {
				treeNode = new TreeNode(null, null, null);
				root.addChild(constraint, treeNode);

				Set<Constraint> diagnose = getDiagnose(treeNode);
				Collections.reverse(new ArrayList<>(diagnose));

				DiagnosisMetadata diagnoseMetadata = diagnosesCollection.Contains(diagnose);
				if (diagnoseMetadata == DiagnosisMetadata.Min) {
					diagnosesCollection.add(diagnose);
					if (progressCallback != null)
						progressCallback.diagnosisFound(diagnose);
				} else {
					if (progressCallback != null)
						progressCallback.ignoredDiagnosis(diagnose, diagnoseMetadata);
				}
			} else {
				if (!conflictSets.contains(minCS)) {
					conflictSets.add(minCS);
				}
				treeNode = new TreeNode(minCS, difference, String.valueOf(indexOfConstraint));
				root.addChild(constraint, treeNode);
				queue.add(treeNode);
			}
		}

		while (!queue.isEmpty()) {
			TreeNode node = queue.getFirst();
			queue.removeFirst();
			buildDiagnosesTree(node, diagnosesCollection);
		}
	}

	private Set<Constraint> getDiagnose(TreeNode node) {
		Set<Constraint> diagnoses = new LinkedHashSet<Constraint>();

		if (node.getConstraint() != null) {
			diagnoses.add(node.getConstraint());
		}
		if (node.getParentNode() != null) {
			diagnoses.addAll(getDiagnose(node.getParentNode()));
		}
		return diagnoses;
	}

	@Override
  public DiagnosesCollection diagnose() throws DiagnosisException {

		ConsistencyChecker consistencyChecker = new ConsistencyChecker();
    if (!fixedModel.isEmpty() && !consistencyChecker.isConsistent(fixedModel)) {
      if (progressCallback != null)
        progressCallback.displayMessage("The fixed model is not consistent.");
      return new DiagnosesCollection();
    }

    Set<Constraint> minCS = conflictDetection.getMinConflictSet(userConstraints);

    if (minCS == null || minCS.isEmpty()) {
			if (progressCallback != null)
				progressCallback.displayMessage("A minimal conflict set does not exist for the user-set constraints.");

			return new DiagnosesCollection();
		}

		if (!conflictSets.contains(minCS)) {
			conflictSets.add(minCS);
		}
		TreeNode rootNode = new TreeNode(minCS, userConstraints, null);
		DiagnosesCollection diagnosesCollection = new DiagnosesCollection(); // Here
																				// are
																				// stored
																				// diagnoses
		if (progressCallback != null) {
			progressCallback.minConflictSet(minCS, userConstraints, "0) ");
		}

		buildDiagnosesTree(rootNode, diagnosesCollection);

		if (progressCallback != null) {
			progressCallback.displayMessage("All minimal conflict sets :"
					+ System.lineSeparator() + conflictSets.toString());
			progressCallback.displayMessage(System.lineSeparator() + "All minimal diagnoses :" + System.lineSeparator()
					+ diagnosesCollection.toString());
		}
		return diagnosesCollection;
	}

  /**
   * @return the collection of conflict sets encountered in the diagnosis process
   */
  public DiagnosesCollection getConflictSets() {
    return conflictSets;
  }
}
