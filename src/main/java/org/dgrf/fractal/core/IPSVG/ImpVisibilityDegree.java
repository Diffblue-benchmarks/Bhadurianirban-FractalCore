/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.core.IPSVG;

import java.util.ArrayList;
import java.util.List;
import org.dgrf.fractal.core.PSVG.VisibilityDegree;
import org.dgrf.fractal.core.util.TimeSeriesUtil;

/**
 *
 * @author dgrfv
 */
public class ImpVisibilityDegree {

    private List<Double> InputTimeSeries;
    private List<IPSVGDetails> ImpPSVGList;
    private int PSVGRequiredStart;
    private Double PSVGDataPartFromStart;
    private int maxNodesForCalc;
    private int maxGap;
    private boolean includePSVGInterCept;
    private Double improvedPSVG;
    private Double logBase;

    public ImpVisibilityDegree(List<Double> InputTimeSeries, int PSVGRequiredStart, Double PSVGDataPartFromStart, boolean includePSVGInterCept, int maxNodesForCalc, Double logBase, int maxGap) {
        this.InputTimeSeries = InputTimeSeries;
        this.PSVGRequiredStart = PSVGRequiredStart;
        this.PSVGDataPartFromStart = PSVGDataPartFromStart;
        this.maxNodesForCalc = maxNodesForCalc;
        this.maxGap = maxGap;
        this.includePSVGInterCept = includePSVGInterCept;
        this.logBase = logBase;
        
    }

    public List<IPSVGDetails> getImpPSVGList() {
        return ImpPSVGList;
    }

    public Double getImprovedPSVG() {
        return improvedPSVG;
    }

    public void calculateVisibilityDegree() {
        ArrayList<Double> ImpPSVGValue = new ArrayList<>();
        ImpPSVGList = new ArrayList<>();
        Double[] PSVGofBrokenTS = new Double[maxGap];
        for (int gapCounter = 0; gapCounter < maxGap; gapCounter++) {
            List<Double> brokenTimeSeries = TimeSeriesUtil.getGappedTimeSeries(InputTimeSeries, gapCounter + 1);
            VisibilityDegree visDegree = new VisibilityDegree(brokenTimeSeries, PSVGRequiredStart, PSVGDataPartFromStart, includePSVGInterCept, maxNodesForCalc, 0.0, logBase);
            visDegree.calculateVisibilityDegree();
            PSVGofBrokenTS[gapCounter] = visDegree.getPSVGFractalDimension();
        }
        for (int gapCounter = 1; gapCounter <= maxGap; gapCounter++) {
            
            ArrayList<Double> PSVGForGaps = new ArrayList<Double>();
            for (int moveInGapCounter = 1; moveInGapCounter <= gapCounter; moveInGapCounter++) {
                int storedPSVGCounter = moveInGapCounter - 1;
                if (Double.isNaN(PSVGofBrokenTS[storedPSVGCounter])) {
                    break;
                }
                PSVGForGaps.add(PSVGofBrokenTS[storedPSVGCounter]);
                

            }

            Double meanPSVGForAllGaps = TimeSeriesUtil.calcMeanOfTimeSeries(PSVGForGaps);
            

            IPSVGDetails ImpPSVGdet = new IPSVGDetails(gapCounter, meanPSVGForAllGaps);
            ImpPSVGValue.add(meanPSVGForAllGaps);
            PSVGForGaps.clear();
            ImpPSVGList.add(ImpPSVGdet);
        }
        improvedPSVG = TimeSeriesUtil.calcMeanOfTimeSeries(ImpPSVGValue);
    }

}
