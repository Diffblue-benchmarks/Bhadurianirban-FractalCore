package org.dgrf.fractal.core.PSVG;

import java.util.List;
import org.dgrf.fractal.db.entities.Vgadjacency;

public class VisibilityGraphXYTS extends VisibilityGraph {

    public VisibilityGraphXYTS(List<?> InputTimeSeries, int PSVGRequiredStart, double PSVGDataPartFromStart, boolean includePSVGInterCept, int maxNodesForCalc, Double rejectCut, double logBase, String psvgResultsTermInstanceSlug) {
        super(InputTimeSeries, PSVGRequiredStart, PSVGDataPartFromStart, includePSVGInterCept, maxNodesForCalc, rejectCut, logBase, psvgResultsTermInstanceSlug);
    }


    @Override
    Vgadjacency checkVisibility(int currentNodeIndex, int nodeToCompareIndex) {

        Double currentNodeXVal = ((XYData) InputTimeSeries.get(currentNodeIndex)).getxValue();
        Double currentNodeYVal = ((XYData) InputTimeSeries.get(currentNodeIndex)).getyValue();

        Double nodeToCompareXVal = ((XYData) InputTimeSeries.get(nodeToCompareIndex)).getxValue();
        Double nodeToCompareYVal = ((XYData) InputTimeSeries.get(nodeToCompareIndex)).getyValue();
        int inBetweenStartIndex = currentNodeIndex + 1;
        int inBetweenEndIndex = nodeToCompareIndex;
        //List<?> seriesInBetween = InputTimeSeries.subList(currentNodeIndex + 1, nodeToCompareIndex);
        //মাঝখানের গ্যাপ যদি 1 হয় তাহলে এই লিস্টের সাইজ ০ হবে সেক্ষেত্রে এই লুপের মধ্যে না ঢুকেই ট্রু রিটার্ন করবে 
        for (int i = inBetweenStartIndex; i < inBetweenEndIndex; i++) {
            Double inBetweenNodeXVal = ((XYData) InputTimeSeries.get(i)).getxValue();
            Double inBetweenNodeYVal = ((XYData) InputTimeSeries.get(i)).getyValue();

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
