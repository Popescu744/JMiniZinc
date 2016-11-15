package at.siemens.ct.jmz.conflictDetection.HSDAG;

import java.util.List;

import at.siemens.ct.jmz.elements.constraints.Constraint;

public interface DiagnoseProgressCallback {
	public void diagnoseFound(List<Constraint> diagnose);
	public void minConflictSet(List<Constraint> minConflictSet, List<Constraint> inputConflictSet);
	public void constraintSelected(Constraint constraint);
	public void displayMessage(String message);
	public void allDiagnoses(DiagnosesCollection diagnoseCollection);
	public void ignoredDiagnose(List<Constraint> diagnose, DiagnoseMetadata diagnoseMetadata);	
}