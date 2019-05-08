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
    //private ArrayList<Integer> visibilityOrder;
    private List<VGDegreeDistribution> vgDegreeDistributionList;
    private int PSVGRequiredStart;
    private double PSVGDataPartFromStart;
    private double PSVGIntercept;
    private double PSVGFractalDimension;

    private double PSVGInterceptSE;
    private double PSVGFractalDimensionSE;
    private double PSVGChiSquareVal;
    private double PSVGRSquared;
    private double PSVHChiSquarerejectCut;

    private SimpleRegression PSVGRegSet;
    private boolean includePSVGInterCept;
    private int maxNodesForCalc;

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
        
        initializeDegree(InputTimeSeries);
        iterateAndCalcDegree(InputTimeSeries);
        //createVGEdges();
        PSVGGraphStore.closeVisibilityGraphFile();
        PSVGGraphStore.storeVisibilityGraphInDB(DatabaseConnection.EMF);
        PSVGGraphStore.delVisibilityGraphFile();
        
        calcPSVGList();
        markPSVGoutliers(PSVGRequiredStart, PSVGDataPartFromStart);

        if (PSVHChiSquarerejectCut > 0.0) {
            calcPSVGChiSquareVal();
        }

        setPSVGFractalDimension();

    }
    private void createVGEdges() {
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
                    if (isVisible) {
                        PSVGGraphStore.storeVisibilityGraphInFile(currentNodeIndex, nodeToCompareIndex);
                    }
                }
            }
        }
    }

    private Boolean checkVisibility(int currentNodeIndex, int nodeToCompareIndex) {

        Double currentNodeXVal = InputTimeSeries.get(currentNodeIndex).getxValue();
        Double currentNodeYVal = InputTimeSeries.get(currentNodeIndex).getyValue();

        Double nodeToCompareXVal = InputTimeSeries.get(nodeToCompareIndex).getxValue();
        Double nodeToCompareYVal = InputTimeSeries.get(nodeToCompareIndex).getyValue();
        List<XYData> seriesInBetween  = InputTimeSeries.subList(currentNodeIndex+1,nodeToCompareIndex);
        //System.out.println("compare "+currentNodeIndex+" "+nodeToCompareIndex);
        for (int i=0;i<seriesInBetween.size();i++) {
            Double inBetweenNodeXVal = seriesInBetween.get(i).getxValue();
            Double inBetweenNodeYVal = seriesInBetween.get(i).getyValue();
            
            Double baseRatio = (inBetweenNodeXVal-currentNodeXVal)/(nodeToCompareXVal-currentNodeXVal);
            Double inBetweenHeight = (baseRatio*(nodeToCompareYVal-currentNodeYVal))+currentNodeYVal;
            
            if (inBetweenNodeYVal >= inBetweenHeight) {
                return false;
            }
        }
        return true;
    }
    
    public void printPSVGListToConsole() {
        VGDegreeDistribution PSVGDet;
        
        Logger.getLogger(VisibilityXYDegree.class.getName()).log(Level.INFO,"k P(k) log(p(k)) log(1/k)" );
        for (int i = 0; i < vgDegreeDistributionList.size(); i++) {
            PSVGDet = vgDegreeDistributionList.get(i);
            Logger.getLogger(VisibilityXYDegree.class.getName()).log(Level.INFO, "{0} {1} {2} {3}", new Object[]{PSVGDet.getDegValue(), PSVGDet.getProbOfDegVal(), PSVGDet.getlogOfProbOfDegVal(), PSVGDet.getLogOfDegVal()});
        }
        Logger.getLogger(VisibilityXYDegree.class.getName()).log(Level.INFO, "PSVG{0}x + {1}", new Object[]{PSVGFractalDimension, PSVGIntercept});
    }

    public void setPSVGFractalDimension() {

        PSVGIntercept = PSVGRegSet.getIntercept();
        PSVGFractalDimension = PSVGRegSet.getSlope();
        PSVGRSquared = PSVGRegSet.getRSquare();
        PSVGFractalDimensionSE = PSVGRegSet.getSlopeStdErr();
        PSVGInterceptSE = PSVGRegSet.getInterceptStdErr();
    }

    public void initializeDegree(List<XYData> InputTimeSeries) {
        /* For a particular node the nodes of its two sides are always visible
		 * So the 2 is the default order for each node and that is why the order 
		 * arraylist is initialised with 2. But for the nodes at the two terminals
		 * only one node is visible by default they are set to 1. 
         */
        //visibilityOrder = new ArrayList<>();
        for (int i = 0; i < InputTimeSeries.size(); i++) {
            //visibilityOrder.add(2);
            PSVGGraphStore.storeVisibilityGraphInFile(i, i+1);
        }
        //visibilityOrder.set(0, 1);
        //visibilityOrder.set(InputTimeSeries.size() - 1, 1);
    }

    public void iterateAndCalcDegree(List<XYData> InputTimeSeries) {
        int nodeGap;
        int totalNodes = InputTimeSeries.size();
        if (totalNodes < maxNodesForCalc) {
            maxNodesForCalc = totalNodes;
        }
        int currentNode;
        int nodeToCompare = 0;
        int currNodePlusGap = 0;
        Double comparedNodeValue;
        Double nodeSlope = 0.0;
        Double nodeToNodeDiff = 0.0;
        boolean isVisible;

        for (nodeGap = 2; nodeGap < maxNodesForCalc; nodeGap++) {
            for (currentNode = 0; currentNode < (totalNodes - nodeGap); currentNode++) {
                currNodePlusGap = currentNode + nodeGap;
                isVisible = false;

                for (nodeToCompare = (currentNode + 1); nodeToCompare < currNodePlusGap; nodeToCompare++) {

                    nodeSlope = (InputTimeSeries.get(currNodePlusGap).getxValue() - InputTimeSeries.get(nodeToCompare).getxValue()) / (InputTimeSeries.get(currNodePlusGap).getxValue() - InputTimeSeries.get(currentNode).getxValue());
                    nodeToNodeDiff = InputTimeSeries.get(currentNode).getyValue() - InputTimeSeries.get(currNodePlusGap).getyValue();
                    comparedNodeValue = InputTimeSeries.get(currNodePlusGap).getyValue() + nodeSlope * nodeToNodeDiff;
                    if (InputTimeSeries.get(nodeToCompare).getyValue() < comparedNodeValue) {

                        isVisible = true;
                    } else {
                        isVisible = false;
                        break;
                    }
                }
                if (isVisible) {
                    //visibilityOrder.set(currentNode, (visibilityOrder.get(currentNode) + 1));
                    //visibilityOrder.set(currNodePlusGap, (visibilityOrder.get(currNodePlusGap) + 1));
                    PSVGGraphStore.storeVisibilityGraphInFile(currentNode, currNodePlusGap);
                }
            }

        }

    }
    public void calcPSVGList() {

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
        }).sorted(Comparator.comparing(m->m.getDegValue())).collect(Collectors.toList());
    }
//    
//    public void calcPSVGList() {
//        vgDegreeDistributionList = new ArrayList<>();
//        int degreeVal;
//        int degreeValCount;
//        int visibilityOrderIterator;
//        int totalNodes = visibilityOrder.size();
//        if (visibilityOrder.size() < maxNodesForCalc) {
//            maxNodesForCalc = visibilityOrder.size();
//        }
//        int maxNodesOnBothSides = maxNodesForCalc * 2;
//        float probOfDegVal;
//        VGDegreeDistribution PSVGDet;
//        for (degreeVal = 0; degreeVal < maxNodesOnBothSides; degreeVal++) {
//            degreeValCount = 0;
//            for (visibilityOrderIterator = 0; visibilityOrderIterator < totalNodes; visibilityOrderIterator++) {
//                if (degreeVal == visibilityOrder.get(visibilityOrderIterator)) {
//                    degreeValCount++;
//                }
//            }
//            if (degreeValCount > 0) {
//                PSVGDet = new VGDegreeDistribution();
//                PSVGDet.setDegValue(degreeVal);
//                PSVGDet.setNumOfNodesWithDegVal(degreeValCount);
//                probOfDegVal = (float) degreeValCount / totalNodes;
//                PSVGDet.setProbOfDegVal(probOfDegVal);
//                PSVGDet.setIsRequired(true);
//                vgDegreeDistributionList.add(PSVGDet);
//            }
//        }
//    }

    public void markPSVGoutliers(int PSVGRequiredStart, double PSVGDataPartFromStart) {
        PSVGRegSet = new SimpleRegression(includePSVGInterCept);

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
            } else {
                PSVGRegSet.addData(vgDegreeDistributionList.get(i).getLogOfDegVal(), vgDegreeDistributionList.get(i).getlogOfProbOfDegVal());
            }
        }
    }

    public void calcPSVGChiSquareVal() {
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
            Logger.getLogger(VisibilityXYDegree.class.getName()).log(Level.SEVERE,"Chi square could not be calculated" );
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
