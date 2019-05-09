package org.dgrf.fractal.core.PSVG;

import java.util.List;


public class VisibilityDegree extends VisibilityGraph{

    public VisibilityDegree(List<?> InputTimeSeries, int PSVGRequiredStart, double PSVGDataPartFromStart, boolean includePSVGInterCept, int maxNodesForCalc, Double rejectCut, double logBase, String psvgResultsTermInstanceSlug) {
        super(InputTimeSeries, PSVGRequiredStart, PSVGDataPartFromStart, includePSVGInterCept, maxNodesForCalc, rejectCut, logBase, psvgResultsTermInstanceSlug);
    }

    
    

    

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

   

     Boolean checkVisibility(int currentNodeIndex, int nodeToCompareIndex) {

        Double currentNodeXVal = Double.valueOf(currentNodeIndex);
        Double currentNodeYVal = (Double)InputTimeSeries.get(currentNodeIndex);

        Double nodeToCompareXVal = (Double)Double.valueOf(nodeToCompareIndex);
        Double nodeToCompareYVal = (Double)InputTimeSeries.get(nodeToCompareIndex);
        List<?> seriesInBetween = InputTimeSeries.subList(currentNodeIndex + 1, nodeToCompareIndex);
        //মাঝখানের গ্যাপ যদি 1 হয় তাহলে এই লিস্টের সাইজ ০ হবে সেক্ষেত্রে এই লুপের মধ্যে না ঢুকেই ট্রু রিটার্ন করবে 
        for (int i = 0; i < seriesInBetween.size(); i++) {
            Double inBetweenNodeXVal = Double.valueOf(currentNodeIndex + i + 1);
            Double inBetweenNodeYVal = (Double)seriesInBetween.get(i);

            Double baseRatio = (inBetweenNodeXVal - currentNodeXVal) / (nodeToCompareXVal - currentNodeXVal);
            Double inBetweenHeight = (baseRatio * (nodeToCompareYVal - currentNodeYVal)) + currentNodeYVal;

            if (inBetweenNodeYVal >= inBetweenHeight) {
                return false;
            }
        }
        return true;
    }

    

    

}
