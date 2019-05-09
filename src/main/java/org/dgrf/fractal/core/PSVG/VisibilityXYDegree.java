package org.dgrf.fractal.core.PSVG;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.dgrf.fractal.core.util.DatabaseConnection;
import org.dgrf.fractal.core.util.LogUtil;
import org.dgrf.fractal.db.DAO.VgadjacencyDAO;

public class VisibilityXYDegree {

    private String psvgResultsTermInstanceSlug;
    private List<XYData> InputTimeSeries;
    private int PSVGRequiredStart;
    private double PSVGDataPartFromStart;
    private double PSVHChiSquarerejectCut;
    private boolean includePSVGInterCept;
    private int maxNodesForCalc;

    private List<VGDegreeDistribution> vgDegreeDistributionList;
    private double PSVGIntercept;
    private double PSVGFractalDimension;
    private double PSVGInterceptSE;
    private double PSVGFractalDimensionSE;
    private double PSVGChiSquareVal;
    private double PSVGRSquared;

    

    public VisibilityXYDegree(List<XYData> InputTimeSeries, int PSVGRequiredStart, double PSVGDataPartFromStart,
            boolean includePSVGInterCept, int maxNodesForCalc, double PSVHChiSquarerejectCut, double logBase, String psvgResultsTermInstanceSlug) {
        this.InputTimeSeries = InputTimeSeries;
        this.PSVGRequiredStart = PSVGRequiredStart;
        this.PSVGDataPartFromStart = PSVGDataPartFromStart;
        this.includePSVGInterCept = includePSVGInterCept;
        this.maxNodesForCalc = maxNodesForCalc;
        this.PSVHChiSquarerejectCut = PSVHChiSquarerejectCut;
        this.psvgResultsTermInstanceSlug = psvgResultsTermInstanceSlug;
        LogUtil.setLogBase(logBase);
    }

    public double getPSVGIntercept() {
        return this.PSVGIntercept;
    }

    public double getPSVGFractalDimension() {
        return this.PSVGFractalDimension;
    }

    public void calculateVisibilityDegree() {
        PSVGGraphStore.psvgresultsslug = psvgResultsTermInstanceSlug;
        PSVGGraphStore.createVisibilityGraphFile();

        //initializeDegree(InputTimeSeries);
        //iterateAndCalcDegree(InputTimeSeries);
        createVGEdges();
        PSVGGraphStore.closeVisibilityGraphFile();
        PSVGGraphStore.storeVisibilityGraphInDB(DatabaseConnection.EMF);
        PSVGGraphStore.delVisibilityGraphFile();

        createDegreeDistribution();
        markOutliersOfDegreeDistribution(PSVGRequiredStart, PSVGDataPartFromStart);

        

        fitDegreeDistribution();

    }

    private void createVGEdges() {
        int totalNodes = InputTimeSeries.size();
        if (InputTimeSeries.size() < maxNodesForCalc) {
            maxNodesForCalc = InputTimeSeries.size();
        }

        for (int nodeGap = 1; nodeGap < maxNodesForCalc; nodeGap++) {
            for (int currentNodeIndex = 0; currentNodeIndex < (totalNodes - nodeGap); currentNodeIndex++) {
                int nodeToCompareIndex = currentNodeIndex + nodeGap;

                Boolean isVisible = checkVisibility(currentNodeIndex, nodeToCompareIndex);
                if (isVisible) {
                    PSVGGraphStore.storeVisibilityGraphInFile(currentNodeIndex, nodeToCompareIndex);
                }

            }
        }
    }

    private Boolean checkVisibility(int currentNodeIndex, int nodeToCompareIndex) {

        Double currentNodeXVal = InputTimeSeries.get(currentNodeIndex).getxValue();
        Double currentNodeYVal = InputTimeSeries.get(currentNodeIndex).getyValue();

        Double nodeToCompareXVal = InputTimeSeries.get(nodeToCompareIndex).getxValue();
        Double nodeToCompareYVal = InputTimeSeries.get(nodeToCompareIndex).getyValue();
        List<XYData> seriesInBetween = InputTimeSeries.subList(currentNodeIndex + 1, nodeToCompareIndex);
        //মাঝখানের গ্যাপ যদি 1 হয় তাহলে এই লিস্টের সাইজ ০ হবে সেক্ষেত্রে এই লুপের মধ্যে না ঢুকেই ট্রু রিটার্ন করবে 
        for (int i = 0; i < seriesInBetween.size(); i++) {
            Double inBetweenNodeXVal = seriesInBetween.get(i).getxValue();
            Double inBetweenNodeYVal = seriesInBetween.get(i).getyValue();

            Double baseRatio = (inBetweenNodeXVal - currentNodeXVal) / (nodeToCompareXVal - currentNodeXVal);
            Double inBetweenHeight = (baseRatio * (nodeToCompareYVal - currentNodeYVal)) + currentNodeYVal;

            if (inBetweenNodeYVal >= inBetweenHeight) {
                return false;
            }
        }
        return true;
    }

    private void fitDegreeDistribution() {
        SimpleRegression PSVGRegSet = new SimpleRegression(includePSVGInterCept);
        vgDegreeDistributionList.stream().forEach(vgd -> {
            if (vgd.getIsRequired()) {
                PSVGRegSet.addData(vgd.getLogOfDegVal(), vgd.getlogOfProbOfDegVal());
            }
        });

        PSVGIntercept = PSVGRegSet.getIntercept();
        PSVGFractalDimension = PSVGRegSet.getSlope();
        PSVGRSquared = PSVGRegSet.getRSquare();
        PSVGFractalDimensionSE = PSVGRegSet.getSlopeStdErr();
        PSVGInterceptSE = PSVGRegSet.getInterceptStdErr();
        if (PSVHChiSquarerejectCut > 0.0) {
            calcPSVGChiSquareVal(PSVGRegSet);
        }
    }

    public void createDegreeDistribution() {

        VgadjacencyDAO vgadjacencyDAO = new VgadjacencyDAO(DatabaseConnection.EMF);
        Map<Integer, Integer> nodesAndDegreeMap = vgadjacencyDAO.getNodeCountsforDegree(psvgResultsTermInstanceSlug);
        int totalNodes = nodesAndDegreeMap.size();
        vgDegreeDistributionList = nodesAndDegreeMap.entrySet().stream().filter(nd -> nd.getValue() > 0).map(nd -> {
            VGDegreeDistribution PSVGDet = new VGDegreeDistribution();
            int degreeVal = nd.getKey();
            int degreeValCount = nd.getValue();
            PSVGDet.setDegValue(degreeVal);
            PSVGDet.setNumOfNodesWithDegVal(degreeValCount);
            float probOfDegVal = (float) degreeValCount / totalNodes;
            PSVGDet.setProbOfDegVal(probOfDegVal);
            PSVGDet.setIsRequired(true);
            return PSVGDet;
        }).sorted(Comparator.comparing(m -> m.getDegValue())).collect(Collectors.toList());
    }

    public void markOutliersOfDegreeDistribution(int PSVGRequiredStart, double PSVGDataPartFromStart) {
        

        int PSVGRequiredEnd = (int) ((int) vgDegreeDistributionList.size() * PSVGDataPartFromStart);
        /*
		 * We need at least 4 data points to fit and find the PSVG gradient.
         */
        if (PSVGRequiredEnd < (PSVGRequiredStart + 4)) {
            PSVGRequiredStart = 0;
            PSVGRequiredEnd = vgDegreeDistributionList.size();
        }
        if (PSVGRequiredStart > vgDegreeDistributionList.size()) {
            PSVGRequiredStart = 0;
            PSVGRequiredEnd = vgDegreeDistributionList.size();
        }
        for (int i = 0; i < vgDegreeDistributionList.size(); i++) {
            if (i < PSVGRequiredStart) {
                vgDegreeDistributionList.get(i).setIsRequired(false);
            } else if (i > PSVGRequiredEnd) {
                vgDegreeDistributionList.get(i).setIsRequired(false);
            }
        }
    }

    public void calcPSVGChiSquareVal(SimpleRegression PSVGRegSet) {
        int listSize = 0;
        Double expectLogOfProbOfDegVal;
        Double actualLogOfProbOfDegVal;
        Double diffExpectedActual;
        Double absExpectLogOfProbOfDegVal;
        Double squaredDiffDivExpected;
        Double sumOfSquaredDiffDivExpected = 0.0;
        for (int i = 0; i < vgDegreeDistributionList.size(); i++) {

            expectLogOfProbOfDegVal = PSVGRegSet.predict(vgDegreeDistributionList.get(i).getLogOfDegVal());
            actualLogOfProbOfDegVal = vgDegreeDistributionList.get(i).getlogOfProbOfDegVal();
            absExpectLogOfProbOfDegVal = Math.abs(expectLogOfProbOfDegVal);
            diffExpectedActual = Math.abs(expectLogOfProbOfDegVal - actualLogOfProbOfDegVal);
            if (diffExpectedActual <= PSVHChiSquarerejectCut) {
                squaredDiffDivExpected = (diffExpectedActual * diffExpectedActual) / absExpectLogOfProbOfDegVal;
                sumOfSquaredDiffDivExpected = sumOfSquaredDiffDivExpected + squaredDiffDivExpected;
                listSize++;
            }
        }

        if (listSize < 3) {
            Logger.getLogger(VisibilityXYDegree.class.getName()).log(Level.SEVERE, "Chi square could not be calculated");
            PSVGChiSquareVal = 999.0;
        }
        int degFreedom = listSize - 2;//2 is because there are expected and actual is for 2
        PSVGChiSquareVal = sumOfSquaredDiffDivExpected / degFreedom;

    }

    /**
     * @return the pSVGInterceptSE
     */
    public double getPSVGInterceptSE() {
        return PSVGInterceptSE;
    }

    /**
     * @return the pSVGFractalDimensionSE
     */
    public double getPSVGFractalDimensionSE() {
        return PSVGFractalDimensionSE;
    }

    /**
     * @return the pSVGChiSquareVal
     */
    public double getPSVGChiSquareVal() {
        return PSVGChiSquareVal;
    }

    /**
     * @return the pSVGRSquared
     */
    public double getPSVGRSquared() {
        return PSVGRSquared;
    }

    public List<VGDegreeDistribution> getVgDegreeDistributionList() {
        return vgDegreeDistributionList;
    }

}
