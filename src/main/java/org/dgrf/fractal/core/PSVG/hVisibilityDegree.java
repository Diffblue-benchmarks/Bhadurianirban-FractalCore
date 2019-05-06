package org.dgrf.fractal.core.PSVG;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.dgrf.fractal.core.util.LogUtil;



public class hVisibilityDegree {

	private List<Double> InputTimeSeries;
	private ArrayList<Integer> visibilityOrder;
	private ArrayList<VGDegreeDistribution> PSVGList;
	private int PSVGRequiredStart;
	private double PSVGDataPartFromStart;
	private double PSVGIntercept;
	private double PSVGFractalDimension;
	private SimpleRegression PSVGRegSet;
	private boolean includePSVGInterCept;
	private int maxNodesForCalc;
	public hVisibilityDegree (List<Double> InputTimeSeries,int PSVGRequiredStart, double PSVGDataPartFromStart,
			boolean includePSVGInterCept,int maxNodesForCalc,double logBase) {
		this.InputTimeSeries = InputTimeSeries;
		this.PSVGRequiredStart = PSVGRequiredStart;
		this.PSVGDataPartFromStart = PSVGDataPartFromStart;
		this.includePSVGInterCept = includePSVGInterCept;
		this.maxNodesForCalc = maxNodesForCalc;
		LogUtil.setLogBase(logBase);
	}

	
	public ArrayList<VGDegreeDistribution> getPSVGList () {
		return PSVGList;
	}
	public double getPSVGIntercept () {
		return this.PSVGIntercept;
	}
	public double getPSVGFractalDimension () {
		return this.PSVGFractalDimension;
	}
	public void calculateVisibilityDegree () {
		initializeDegree(InputTimeSeries);
		iterateAndCalcDegree(InputTimeSeries);

		calcPSVGList();
		markPSVGoutliers(PSVGRequiredStart,PSVGDataPartFromStart);
		
		

		setPSVGFractalDimension();
		
		
	}
	public void printPSVGListToConsole() {
	VGDegreeDistribution PSVGDet = new VGDegreeDistribution();
	
        Logger.getLogger(hVisibilityDegree.class.getName()).log(Level.INFO,"k P(k) log(p(k)) log(1/k)" );
	for (int i=0;i< PSVGList.size();i++) {
		PSVGDet = PSVGList.get(i);
                Logger.getLogger(hVisibilityDegree.class.getName()).log(Level.INFO, "{0} {1} {2} {3}", new Object[]{PSVGDet.getDegValue(), PSVGDet.getProbOfDegVal(), PSVGDet.getlogOfProbOfDegVal(), PSVGDet.getLogOfDegVal()});
		
	    }
	
        Logger.getLogger(hVisibilityDegree.class.getName()).log(Level.INFO, "PSVG{0}x + {1}", new Object[]{PSVGFractalDimension, PSVGIntercept});
	}
	public void setPSVGFractalDimension () {
		
		PSVGIntercept = PSVGRegSet.getIntercept();
		PSVGFractalDimension = PSVGRegSet.getSlope();
	}
	public void initializeDegree(List<Double> InputTimeSeries) {
		/* For a particular node the nodes of its two sides are always visible
		 * So the 2 is the default order for each node and that is why the order 
		 * arraylist is initialised with 2. But for the nodes at the two terminals
		 * only one node is visible by default they are set to 1. 
		 */
		visibilityOrder = new ArrayList<Integer>();
		for (int i =0; i<InputTimeSeries.size();i++) {
			visibilityOrder.add(2);
		}
		visibilityOrder.set(0, 1);
		visibilityOrder.set(InputTimeSeries.size()-1, 1);
	}
	public void iterateAndCalcDegree(List<Double> InputTimeSeries) {
		int nodeGap;
		int totalNodes = visibilityOrder.size();
		if (visibilityOrder.size()<maxNodesForCalc) {
			maxNodesForCalc = visibilityOrder.size();
		}
		int currentNode;
		//Double tempCurrentNode = 0.0;
		int nodeToCompare=0;
		//Double tempNodeToCompare=0.0;
		int currNodePlusGap=0;
		//Double tempCurrNodePlusGap=0.0;
		//Integer currNodeValue;
		//Double comparedNodeValue;
		//Double nodeSlope=0.0;
		//Double nodeToNodeDiff = 0.0;
		boolean isVisible=false;
		
		for (nodeGap = 2;nodeGap<maxNodesForCalc;nodeGap++) {
			for (currentNode = 0;currentNode<(totalNodes-nodeGap);currentNode++) {
				currNodePlusGap = currentNode + nodeGap;
				isVisible = false;
				
				
				for (nodeToCompare=(currentNode+1);nodeToCompare<currNodePlusGap;nodeToCompare++) {
					/*tempNodeToCompare = (double) nodeToCompare;
					tempCurrentNode = (double) currentNode;
					tempCurrNodePlusGap = (double)  currNodePlusGap;

					nodeSlope = (tempCurrNodePlusGap - tempNodeToCompare)/(tempCurrNodePlusGap - tempCurrentNode);
					nodeToNodeDiff = InputTimeSeries.get(currentNode) - InputTimeSeries.get(currNodePlusGap);
					comparedNodeValue = InputTimeSeries.get(currNodePlusGap) + nodeSlope * nodeToNodeDiff;*/
							

					if ((InputTimeSeries.get(nodeToCompare)< InputTimeSeries.get(currentNode)) 
							&& (InputTimeSeries.get(nodeToCompare)< InputTimeSeries.get(currNodePlusGap))) {
						isVisible = true;
					}
					else {
						isVisible = false;
						break;
					}
				}
				if (isVisible) {
					visibilityOrder.set(currentNode, (visibilityOrder.get(currentNode)+1));
					visibilityOrder.set(currNodePlusGap, (visibilityOrder.get(currNodePlusGap)+1));
                                        PSVGGraphStore.storeVisibilityGraphInFile(currentNode, currNodePlusGap);
				}
			}

		}
		
	}
	public void calcPSVGList() {
		PSVGList = new ArrayList<VGDegreeDistribution>();
		int degreeVal;
		int degreeValCount;
		int visibilityOrderIterator;
		int totalNodes = visibilityOrder.size();
		if (visibilityOrder.size()<maxNodesForCalc) {
			maxNodesForCalc = visibilityOrder.size();
		}
		int maxNodesOnBothSides = maxNodesForCalc*2;
		float probOfDegVal;
		VGDegreeDistribution PSVGDet;
		for (degreeVal=0;degreeVal<maxNodesOnBothSides;degreeVal++) {
			degreeValCount = 0;
			for (visibilityOrderIterator=0;visibilityOrderIterator<totalNodes;visibilityOrderIterator++) {
				if (degreeVal == visibilityOrder.get(visibilityOrderIterator)) {
					degreeValCount++;
				}
			}
			if (degreeValCount>0) {
				PSVGDet = new VGDegreeDistribution();
				PSVGDet.setDegValue(degreeVal);
				PSVGDet.setNumOfNodesWithDegVal(degreeValCount);
				probOfDegVal = (float) degreeValCount/totalNodes;
				PSVGDet.setProbOfDegVal(probOfDegVal);
				PSVGDet.setIsRequired(true);
				PSVGList.add(PSVGDet);
			}
		}
	}

	public void markPSVGoutliers(int PSVGRequiredStart,double PSVGDataPartFromStart) {
		PSVGRegSet = new SimpleRegression(includePSVGInterCept);
		
		int PSVGRequiredEnd = (int) ((int) PSVGList.size()*PSVGDataPartFromStart);
		/*
		 * We need at least 4 data points to fit and find the PSVG gradient.
		 */
		if (PSVGRequiredEnd < (PSVGRequiredStart+4)) {
			PSVGRequiredStart = 0;
			PSVGRequiredEnd = PSVGList.size();
		}
		if (PSVGRequiredStart>PSVGList.size()) {
			PSVGRequiredStart = 0;
			PSVGRequiredEnd = PSVGList.size();
		}
		for (int i = 0;i<PSVGList.size();i++) {
			if (i<PSVGRequiredStart) {
				PSVGList.get(i).setIsRequired(false);
			}
			else if (i>PSVGRequiredEnd) {
				PSVGList.get(i).setIsRequired(false);
			}
			else {
				PSVGRegSet.addData(PSVGList.get(i).getLogOfDegVal(),PSVGList.get(i).getlogOfProbOfDegVal());
			}
		}
	}
}
