package org.dgrf.fractal.core.PSVG;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.dgrf.cms.core.client.TermInstanceClient;
import org.dgrf.fractal.constants.FractalConstants;
import org.dgrf.fractal.core.util.DatabaseConnection;
import org.dgrf.fractal.core.util.LogUtil;
import org.dgrf.fractal.db.DAO.VgadjacencyDAO;

public class VisibilityDegree {

    private List<Double> InputTimeSeries;
    //private ArrayList<Integer> visibilityOrder;
    private List<PSVGDetails> PSVGList;
    private int PSVGRequiredStart;
    private double PSVGDataPartFromStart;
    private double PSVGIntercept;
    private double PSVGFractalDimension;
    private double PSVGFractalDimensionSE;
    private double PSVGInterceptSE;
    private double PSVGRSquared;
    private double PSVGChiSquareVal;
    private double rejectCut;
    private SimpleRegression PSVGRegSet;
    private boolean includePSVGInterCept;
    private int maxNodesForCalc;
    private String psvgResultsTermInstanceSlug;

    public VisibilityDegree(List<Double> InputTimeSeries, int PSVGRequiredStart, double PSVGDataPartFromStart,
            boolean includePSVGInterCept, int maxNodesForCalc, Double rejectCut, double logBase, String psvgResultsTermInstanceSlug) {
        this.InputTimeSeries = InputTimeSeries;
        this.PSVGRequiredStart = PSVGRequiredStart;
        this.PSVGDataPartFromStart = PSVGDataPartFromStart;
        this.includePSVGInterCept = includePSVGInterCept;
        this.maxNodesForCalc = maxNodesForCalc;
        this.rejectCut = rejectCut;
        LogUtil.setLogBase(logBase);
        this.psvgResultsTermInstanceSlug = psvgResultsTermInstanceSlug;

    }

    public List<PSVGDetails> getPSVGList() {
        return PSVGList;
    }

    public double getPSVGIntercept() {
        return this.PSVGIntercept;
    }

    public double getPSVGFractalDimension() {
        return this.PSVGFractalDimension;
    }

    public void calculateVisibilityDegree() {

        PSVGGraphStore.createVisibilityGraphFile();

        PSVGGraphStore.psvgresultsslug = psvgResultsTermInstanceSlug;
        initializeDegree(InputTimeSeries);
        iterateAndCalcDegree(InputTimeSeries);
        PSVGGraphStore.closeVisibilityGraphFile();
        PSVGGraphStore.storeVisibilityGraphInDB(DatabaseConnection.EMF);
        PSVGGraphStore.delVisibilityGraphFile();
        calcPSVGList();
        markPSVGoutliers(PSVGRequiredStart, PSVGDataPartFromStart);

        setPSVGFractalDimension();
        if (rejectCut > 0.0) {
            calcPSVGChiSquareVal();
        }
        if (!psvgResultsTermInstanceSlug.contains(FractalConstants.TERM_INSTANCE_SLUG_IPSVG_EXT)) {
            VgadjacencyDAO vgadjacencyDAO = new VgadjacencyDAO(DatabaseConnection.EMF);
            vgadjacencyDAO.deleteVisibilityGraph(psvgResultsTermInstanceSlug);
        }

    }

//    public void printPSVGListToConsole() {
//        PSVGDetails PSVGDet = new PSVGDetails();
//        Logger.getLogger(VisibilityDegree.class.getName()).log(Level.INFO,"k P(k) log(p(k)) log(1/k)" );
//        
//        for (int i = 0; i < PSVGList.size(); i++) {
//            PSVGDet = PSVGList.get(i);
//            Logger.getLogger(VisibilityDegree.class.getName()).log(Level.INFO, "{0} {1} {2} {3}", new Object[]{PSVGDet.getDegValue(), PSVGDet.getProbOfDegVal(), PSVGDet.getlogOfProbOfDegVal(), PSVGDet.getLogOfDegVal()});
//            
//        }
//        Logger.getLogger(VisibilityDegree.class.getName()).log(Level.INFO, "PSVG{0}x + {1}", new Object[]{PSVGFractalDimension, PSVGIntercept});
//        
//    }
    public void setPSVGFractalDimension() {

        PSVGIntercept = PSVGRegSet.getIntercept();
        PSVGFractalDimension = PSVGRegSet.getSlope();
        PSVGRSquared = PSVGRegSet.getRSquare();
        PSVGFractalDimensionSE = PSVGRegSet.getSlopeStdErr();
        PSVGInterceptSE = PSVGRegSet.getInterceptStdErr();
    }

    public void initializeDegree(List<Double> InputTimeSeries) {
        /* For a particular node the nodes of its two sides are always visible
		 * So the 2 is the default order for each node and that is why the order 
		 * arraylist is initialised with 2. But for the nodes at the two terminals
		 * only one node is visible by default they are set to 1. 
         */

        for (int i = 0; i < InputTimeSeries.size(); i++) {

            PSVGGraphStore.storeVisibilityGraphInFile(i, i + 1);
        }

    }

    public void createVGEdges() {
        int totalNodes = InputTimeSeries.size();
        if (InputTimeSeries.size() < maxNodesForCalc) {
            maxNodesForCalc = InputTimeSeries.size();
        }

        for (int nodeGap = 1; nodeGap < maxNodesForCalc; nodeGap++) {
            for (int currentNodeIndex = 0; currentNodeIndex < (totalNodes - nodeGap); currentNodeIndex++) {
                int nodeToCompareIndex = currentNodeIndex + nodeGap;

                if (nodeGap == 1) {
                    PSVGGraphStore.storeVisibilityGraphInFile(currentNodeIndex, nodeToCompareIndex);
                } else {
                    Boolean isVisible = checkVisibility(currentNodeIndex, nodeToCompareIndex);
                }
            }
        }
    }

    private Boolean checkVisibility(int currentNodeIndex, int nodeToCompareIndex) {

        Double currentNodeXVal = Double.valueOf(currentNodeIndex);
        Double currentNodeYVal = InputTimeSeries.get(currentNodeIndex);

        Double nodeToCompareXVal = Double.valueOf(nodeToCompareIndex);
        Double nodeToCompareYVal = InputTimeSeries.get(nodeToCompareIndex);
        List<Double> seriesInBetween  = InputTimeSeries.subList(currentNodeIndex+1,nodeToCompareIndex-1);
        for (int i=0;i<seriesInBetween.size();i++) {
            Double inBetweenNodeXVal = Double.valueOf(i);
            Double inBetweenNodeYVal = seriesInBetween.get(i);
            
            Double baseRatio = (inBetweenNodeXVal-currentNodeXVal)/(nodeToCompareXVal-currentNodeXVal);
            Double inBetweenHeight = (baseRatio*(nodeToCompareYVal-currentNodeYVal))+currentNodeYVal;
            if (inBetweenNodeYVal >= inBetweenHeight) {
                return true;
            }
        }
        return false;
    }

    public void iterateAndCalcDegree(List<Double> InputTimeSeries) {
        int nodeGap;
        int totalNodes = InputTimeSeries.size();
        if (InputTimeSeries.size() < maxNodesForCalc) {
            maxNodesForCalc = InputTimeSeries.size();
        }
        int currentNode;
        Double tempCurrentNode = 0.0;
        int nodeToCompare = 0;
        Double tempNodeToCompare = 0.0;
        int currNodePlusGap = 0;
        Double tempCurrNodePlusGap = 0.0;
        //Integer currNodeValue;
        Double comparedNodeValue;
        Double nodeSlope = 0.0;
        Double nodeToNodeDiff = 0.0;
        boolean isVisible = false;

        for (nodeGap = 2; nodeGap < maxNodesForCalc; nodeGap++) {
            for (currentNode = 0; currentNode < (totalNodes - nodeGap); currentNode++) {
                currNodePlusGap = currentNode + nodeGap;
                isVisible = false;

                for (nodeToCompare = (currentNode + 1); nodeToCompare < currNodePlusGap; nodeToCompare++) {
                    tempNodeToCompare = (double) nodeToCompare;
                    tempCurrentNode = (double) currentNode;
                    tempCurrNodePlusGap = (double) currNodePlusGap;

                    nodeSlope = (tempCurrNodePlusGap - tempNodeToCompare) / (tempCurrNodePlusGap - tempCurrentNode);
                    nodeToNodeDiff = InputTimeSeries.get(currentNode) - InputTimeSeries.get(currNodePlusGap);
                    comparedNodeValue = InputTimeSeries.get(currNodePlusGap) + nodeSlope * nodeToNodeDiff;

                    if (InputTimeSeries.get(nodeToCompare) < comparedNodeValue) {

                        isVisible = true;
                    } else {
                        isVisible = false;
                        break;
                    }
                }
                if (isVisible) {

                    PSVGGraphStore.storeVisibilityGraphInFile(currentNode, currNodePlusGap);
                }
            }

        }

    }

    public void calcPSVGList() {

        VgadjacencyDAO vgadjacencyDAO = new VgadjacencyDAO(DatabaseConnection.EMF);
        Map<Integer, Integer> nodesAndDegreeMap = vgadjacencyDAO.getDegreesOfnodes(psvgResultsTermInstanceSlug);
        int totalNodes = nodesAndDegreeMap.size();
        PSVGList = nodesAndDegreeMap.entrySet().stream().filter(nd -> nd.getValue() > 0).map(nd -> {
            PSVGDetails PSVGDet = new PSVGDetails();
            int degreeVal = nd.getKey();
            int degreeValCount = nd.getValue();
            PSVGDet.setDegValue(degreeVal);
            PSVGDet.setNumOfNodesWithDegVal(degreeValCount);
            float probOfDegVal = (float) degreeValCount / totalNodes;
            PSVGDet.setProbOfDegVal(probOfDegVal);
            PSVGDet.setIsRequired(true);
            return PSVGDet;
        }).collect(Collectors.toList());

    }

    public void markPSVGoutliers(int PSVGRequiredStart, double PSVGDataPartFromStart) {
        PSVGRegSet = new SimpleRegression(includePSVGInterCept);

        int PSVGRequiredEnd = (int) ((int) PSVGList.size() * PSVGDataPartFromStart);
        /*
		 * We need at least 4 data points to fit and find the PSVG gradient.
         */
        if (PSVGRequiredEnd < (PSVGRequiredStart + 4)) {
            PSVGRequiredStart = 0;
            PSVGRequiredEnd = PSVGList.size();
        }
        if (PSVGRequiredStart > PSVGList.size()) {
            PSVGRequiredStart = 0;
            PSVGRequiredEnd = PSVGList.size();
        }
        for (int i = 0; i < PSVGList.size(); i++) {
            if (i < PSVGRequiredStart) {
                PSVGList.get(i).setIsRequired(false);
            } else if (i > PSVGRequiredEnd) {
                PSVGList.get(i).setIsRequired(false);
            } else {
                PSVGRegSet.addData(PSVGList.get(i).getLogOfDegVal(), PSVGList.get(i).getlogOfProbOfDegVal());
            }
        }
    }

    /**
     * @return the pSVGFractalDimensionSE
     */
    public double getPSVGFractalDimensionSE() {
        return PSVGFractalDimensionSE;
    }

    /**
     * @return the pSVGInterceptSE
     */
    public double getPSVGInterceptSE() {
        return PSVGInterceptSE;
    }

    public void calcPSVGChiSquareVal() {

        int listSize = 0;
        Double expectLogOfProbOfDegVal = 0.0;
        Double actualLogOfProbOfDegVal = 0.0;
        Double diffExpectedActual = 0.0;
        Double absExpectLogOfProbOfDegVal = 0.0;
        Double squaredDiffDivExpected = 0.0;
        Double sumOfSquaredDiffDivExpected = 0.0;

        for (int i = 0; i < PSVGList.size(); i++) {

            expectLogOfProbOfDegVal = PSVGRegSet.predict(PSVGList.get(i).getLogOfDegVal());
            actualLogOfProbOfDegVal = PSVGList.get(i).getlogOfProbOfDegVal();
            absExpectLogOfProbOfDegVal = Math.abs(expectLogOfProbOfDegVal);
            diffExpectedActual = Math.abs(expectLogOfProbOfDegVal - actualLogOfProbOfDegVal);

            if (diffExpectedActual <= rejectCut) {
                squaredDiffDivExpected = (diffExpectedActual * diffExpectedActual) / absExpectLogOfProbOfDegVal;

                sumOfSquaredDiffDivExpected = sumOfSquaredDiffDivExpected + squaredDiffDivExpected;
                listSize++;
            }
        }

        if (listSize < 3) {
            Logger.getLogger(VisibilityDegree.class.getName()).log(Level.SEVERE, "Chi square could not be calculated");

            PSVGChiSquareVal = 999.0;
        }
        int degFreedom = listSize - 2;//2 is because there are expected and actual is for 2
        PSVGChiSquareVal = sumOfSquaredDiffDivExpected / degFreedom;

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
}
