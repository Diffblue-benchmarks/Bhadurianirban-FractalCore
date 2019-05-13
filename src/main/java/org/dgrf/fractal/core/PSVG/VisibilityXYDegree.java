package org.dgrf.fractal.core.PSVG;

import java.util.List;
import org.dgrf.fractal.db.entities.Vgadjacency;

public class VisibilityXYDegree extends VisibilityGraph {

    public VisibilityXYDegree(List<?> InputTimeSeries, int PSVGRequiredStart, double PSVGDataPartFromStart, boolean includePSVGInterCept, int maxNodesForCalc, Double rejectCut, double logBase, String psvgResultsTermInstanceSlug) {
        super(InputTimeSeries, PSVGRequiredStart, PSVGDataPartFromStart, includePSVGInterCept, maxNodesForCalc, rejectCut, logBase, psvgResultsTermInstanceSlug);
    }

//    @Override
//    void createVGEdges() {
//        int totalNodes = InputTimeSeries.size();
//        int maxNodesForCalc = MAX_NODES_FOR_CALC;
//        if (InputTimeSeries.size() < MAX_NODES_FOR_CALC) {
//            maxNodesForCalc = InputTimeSeries.size();
//        }
//
//        for (int nodeGap = 1; nodeGap < maxNodesForCalc; nodeGap++) {
//            for (int currentNodeIndex = 0; currentNodeIndex < (totalNodes - nodeGap); currentNodeIndex++) {
//                int nodeToCompareIndex = currentNodeIndex + nodeGap;
//
//                Vgadjacency vgadjacency = checkVisibility(currentNodeIndex, nodeToCompareIndex);
//                if (vgadjacency != null) {
//                    insertNewEdge(vgadjacency);
//                    //PSVGGraphStore.storeVisibilityGraphInFile(currentNodeIndex, nodeToCompareIndex);
//                }
//
//            }
//        }
//    }

    @Override
    Vgadjacency checkVisibility(int currentNodeIndex, int nodeToCompareIndex) {

        Double currentNodeXVal = ((XYData) InputTimeSeries.get(currentNodeIndex)).getxValue();
        Double currentNodeYVal = ((XYData) InputTimeSeries.get(currentNodeIndex)).getyValue();

        Double nodeToCompareXVal = ((XYData) InputTimeSeries.get(nodeToCompareIndex)).getxValue();
        Double nodeToCompareYVal = ((XYData) InputTimeSeries.get(nodeToCompareIndex)).getyValue();
        List<?> seriesInBetween = InputTimeSeries.subList(currentNodeIndex + 1, nodeToCompareIndex);
        //মাঝখানের গ্যাপ যদি 1 হয় তাহলে এই লিস্টের সাইজ ০ হবে সেক্ষেত্রে এই লুপের মধ্যে না ঢুকেই ট্রু রিটার্ন করবে 
        for (int i = 0; i < seriesInBetween.size(); i++) {
            Double inBetweenNodeXVal = ((XYData) seriesInBetween.get(i)).getxValue();
            Double inBetweenNodeYVal = ((XYData) seriesInBetween.get(i)).getyValue();

            Double baseRatio = (inBetweenNodeXVal - currentNodeXVal) / (nodeToCompareXVal - currentNodeXVal);
            Double inBetweenHeight = (baseRatio * (nodeToCompareYVal - currentNodeYVal)) + currentNodeYVal;

            if (inBetweenNodeYVal >= inBetweenHeight) {
                return null;
            }
        }
        Vgadjacency vgadjacency = gatherEdgeDetails(currentNodeIndex, nodeToCompareIndex, currentNodeXVal, currentNodeYVal, nodeToCompareXVal, nodeToCompareYVal);
        return vgadjacency;
    }

}
