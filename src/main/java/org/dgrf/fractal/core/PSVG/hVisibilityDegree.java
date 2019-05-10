package org.dgrf.fractal.core.PSVG;

import java.util.List;



public class hVisibilityDegree extends VisibilityGraph{

    public hVisibilityDegree(List<?> InputTimeSeries, int PSVGRequiredStart, double PSVGDataPartFromStart, boolean includePSVGInterCept, int maxNodesForCalc, Double rejectCut, double logBase, String psvgResultsTermInstanceSlug) {
        super(InputTimeSeries, PSVGRequiredStart, PSVGDataPartFromStart, includePSVGInterCept, maxNodesForCalc, rejectCut, logBase, psvgResultsTermInstanceSlug);
    }

    @Override
    void createVGEdges() {
        int totalNodes = InputTimeSeries.size();
        int maxNodesForCalc = MAX_NODES_FOR_CALC;
        if (InputTimeSeries.size() < MAX_NODES_FOR_CALC) {
            maxNodesForCalc = InputTimeSeries.size();
        }

        for (int nodeGap = 1; nodeGap < maxNodesForCalc; nodeGap++) {
            for (int currentNodeIndex = 0; currentNodeIndex < (totalNodes - nodeGap); currentNodeIndex++) {
                int nodeToCompareIndex = currentNodeIndex + nodeGap;

                Boolean isVisible = checkVisibility(currentNodeIndex, nodeToCompareIndex);
                if (isVisible) {
                    //PSVGGraphStore.storeVisibilityGraphInFile(currentNodeIndex, nodeToCompareIndex);
                    insertNewEdge(currentNodeIndex, nodeToCompareIndex);
                }

            }
        }
    }

    @Override
    Boolean checkVisibility(int currentNodeIndex, int nodeToCompareIndex) {
        //Double currentNodeXVal = Double.valueOf(currentNodeIndex);
        Double currentNodeYVal = (Double)InputTimeSeries.get(currentNodeIndex);

        //Double nodeToCompareXVal = (Double)Double.valueOf(nodeToCompareIndex);
        Double nodeToCompareYVal = (Double)InputTimeSeries.get(nodeToCompareIndex);
        List<?> seriesInBetween = InputTimeSeries.subList(currentNodeIndex + 1, nodeToCompareIndex);
        //মাঝখানের গ্যাপ যদি 1 হয় তাহলে এই লিস্টের সাইজ ০ হবে সেক্ষেত্রে এই লুপের মধ্যে না ঢুকেই ট্রু রিটার্ন করবে 
        
        for (int i = 0; i < seriesInBetween.size(); i++) {
            //Double inBetweenNodeXVal = Double.valueOf(currentNodeIndex + i + 1);
            Double inBetweenNodeYVal = (Double)seriesInBetween.get(i);

            //Double baseRatio = (inBetweenNodeXVal - currentNodeXVal) / (nodeToCompareXVal - currentNodeXVal);
            //Double inBetweenHeight = (baseRatio * (nodeToCompareYVal - currentNodeYVal)) + currentNodeYVal;

            if ((inBetweenNodeYVal < currentNodeYVal)&& (inBetweenNodeYVal < nodeToCompareYVal)) {
               
            } else {
                return false;
            }
        }
        return true;
    }

	
//	public void initializeDegree(List<Double> InputTimeSeries) {
//		/* For a particular node the nodes of its two sides are always visible
//		 * So the 2 is the default order for each node and that is why the order 
//		 * arraylist is initialised with 2. But for the nodes at the two terminals
//		 * only one node is visible by default they are set to 1. 
//		 */
//		visibilityOrder = new ArrayList<Integer>();
//		for (int i =0; i<InputTimeSeries.size();i++) {
//			visibilityOrder.add(2);
//                        PSVGGraphStore.storeVisibilityGraphInFile(i, i+1);
//		}
//		visibilityOrder.set(0, 1);
//		visibilityOrder.set(InputTimeSeries.size()-1, 1);
//	}
//	public void iterateAndCalcDegree(List<Double> InputTimeSeries) {
//		int nodeGap;
//		int totalNodes = visibilityOrder.size();
//		if (visibilityOrder.size()<maxNodesForCalc) {
//			maxNodesForCalc = visibilityOrder.size();
//		}
//		int currentNode;
//		//Double tempCurrentNode = 0.0;
//		int nodeToCompare=0;
//		//Double tempNodeToCompare=0.0;
//		int currNodePlusGap=0;
//		//Double tempCurrNodePlusGap=0.0;
//		//Integer currNodeValue;
//		//Double comparedNodeValue;
//		//Double nodeSlope=0.0;
//		//Double nodeToNodeDiff = 0.0;
//		boolean isVisible=false;
//		
//		for (nodeGap = 2;nodeGap<maxNodesForCalc;nodeGap++) {
//			for (currentNode = 0;currentNode<(totalNodes-nodeGap);currentNode++) {
//				currNodePlusGap = currentNode + nodeGap;
//				isVisible = false;
//				
//				
//				for (nodeToCompare=(currentNode+1);nodeToCompare<currNodePlusGap;nodeToCompare++) {
//					/*tempNodeToCompare = (double) nodeToCompare;
//					tempCurrentNode = (double) currentNode;
//					tempCurrNodePlusGap = (double)  currNodePlusGap;
//
//					nodeSlope = (tempCurrNodePlusGap - tempNodeToCompare)/(tempCurrNodePlusGap - tempCurrentNode);
//					nodeToNodeDiff = InputTimeSeries.get(currentNode) - InputTimeSeries.get(currNodePlusGap);
//					comparedNodeValue = InputTimeSeries.get(currNodePlusGap) + nodeSlope * nodeToNodeDiff;*/
//							
//
//					if ((InputTimeSeries.get(nodeToCompare)< InputTimeSeries.get(currentNode)) 
//							&& (InputTimeSeries.get(nodeToCompare)< InputTimeSeries.get(currNodePlusGap))) {
//						isVisible = true;
//					}
//					else {
//						isVisible = false;
//						break;
//					}
//				}
//				if (isVisible) {
//					visibilityOrder.set(currentNode, (visibilityOrder.get(currentNode)+1));
//					visibilityOrder.set(currNodePlusGap, (visibilityOrder.get(currNodePlusGap)+1));
//                                        PSVGGraphStore.storeVisibilityGraphInFile(currentNode, currNodePlusGap);
//				}
//			}
//
//		}
//		
//	}

}
