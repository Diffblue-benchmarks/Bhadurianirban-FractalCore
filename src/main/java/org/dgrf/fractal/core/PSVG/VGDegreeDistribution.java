package org.dgrf.fractal.core.PSVG;

import org.dgrf.fractal.core.util.LogUtil;



public class VGDegreeDistribution {
	private int degValue;
	private int numOfNodesWithDegVal;
	private double probOfDegVal;
	
	private double logOfDegVal;
	private double logOfProbOfDegVal;
	private boolean isRequired;
	
	
	public void setDegValue (int degValue) {
		this.degValue = degValue;
	}
	public void setNumOfNodesWithDegVal (int numOfNodesWithDegVal) {
		this.numOfNodesWithDegVal = numOfNodesWithDegVal;
	}
	public void setProbOfDegVal (float probOfDegVal) {
		this.probOfDegVal = probOfDegVal;
		setLogOfNumOfNodesWithDegVal();
		setLogOfProbOfDegVal();
	}
	private void setLogOfNumOfNodesWithDegVal() {
		
		float degValInverse =  (float) 1/degValue;
		
		this.logOfDegVal = LogUtil.logBaseK(degValInverse);
	}
	private void setLogOfProbOfDegVal() {
		this.logOfProbOfDegVal = LogUtil.logBaseK(probOfDegVal);
	}
	public int getDegValue () {
		return this.degValue;
	}
	public int getNumOfNodesWithDegVal () {
		return this.numOfNodesWithDegVal;
	}
	public double getProbOfDegVal () {
		return this.probOfDegVal;
	}
	public double getLogOfDegVal() {
		return this.logOfDegVal;
	}
	public double getlogOfProbOfDegVal() {
		return this.logOfProbOfDegVal;
	}
	public void setIsRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}
	public boolean getIsRequired() {
		return this.isRequired;
	}

}
