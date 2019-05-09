package org.dgrf.fractal.core.PSVG;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.dgrf.fractal.core.util.DatabaseConnection;
import org.dgrf.fractal.core.util.LogUtil;
import org.dgrf.fractal.db.DAO.VgadjacencyDAO;
import org.dgrf.fractal.db.entities.Vgadjacency;

public class VisibilityXYDegree {

    private final String PSVG_RESULTS_TERM_INSTANCE_SLUG;
    private final List<XYData> InputTimeSeries;
    private final int FIT_DATA_START_INDEX;
    private final double FIT_DATA_PART_FROM_START;
    private final double CHI_SQUARE_REJECT_CUT;
    private final boolean FIT_INCLUDE_INTERCEPT;
    private final int MAX_NODES_FOR_CALC;

    private List<VGDegreeDistribution> vgDegreeDistributionList;
    private double PSVGIntercept;
    private double PSVGFractalDimension;
    private double PSVGInterceptSE;
    private double PSVGFractalDimensionSE;
    private double PSVGChiSquareVal;
    private double PSVGRSquared;

    private EntityManager em;
    private int rowCount;

    public VisibilityXYDegree(List<XYData> InputTimeSeries, int PSVGRequiredStart, double PSVGDataPartFromStart,
            boolean includePSVGInterCept, int maxNodesForCalc, double PSVHChiSquarerejectCut, double logBase, String psvgResultsTermInstanceSlug) {
        this.InputTimeSeries = InputTimeSeries;
        this.FIT_DATA_START_INDEX = PSVGRequiredStart;
        this.FIT_DATA_PART_FROM_START = PSVGDataPartFromStart;
        this.FIT_INCLUDE_INTERCEPT = includePSVGInterCept;
        this.MAX_NODES_FOR_CALC = maxNodesForCalc;
        this.CHI_SQUARE_REJECT_CUT = PSVHChiSquarerejectCut;
        this.PSVG_RESULTS_TERM_INSTANCE_SLUG = psvgResultsTermInstanceSlug;
        LogUtil.setLogBase(logBase);
    }

    public double getPSVGIntercept() {
        return this.PSVGIntercept;
    }

    public double getPSVGFractalDimension() {
        return this.PSVGFractalDimension;
    }

    public void calculateVisibilityDegree() {
        //PSVGGraphStore.psvgresultsslug = PSVG_RESULTS_TERM_INSTANCE_SLUG;
        //PSVGGraphStore.createVisibilityGraphFile();

        //initializeDegree(InputTimeSeries);
        //iterateAndCalcDegree(InputTimeSeries);
        VgadjacencyDAO vgadjacencyDAO = new VgadjacencyDAO(DatabaseConnection.EMF);
        vgadjacencyDAO.deleteVisibilityGraph(PSVG_RESULTS_TERM_INSTANCE_SLUG);
        em = vgadjacencyDAO.getEntityManager();
        em.getTransaction().begin();
        rowCount=0;
        
        createVGEdges();
        
        em.flush();
        em.clear();
        em.getTransaction().commit();
//        PSVGGraphStore.closeVisibilityGraphFile();
//        PSVGGraphStore.storeVisibilityGraphInDB(DatabaseConnection.EMF);
//        PSVGGraphStore.delVisibilityGraphFile();

        createDegreeDistribution();
        markOutliersOfDegreeDistribution();

        

        fitDegreeDistribution();

    }

    private void createVGEdges() {
        int totalNodes = InputTimeSeries.size();
        int maxNodesForCalc= MAX_NODES_FOR_CALC;
        if (InputTimeSeries.size() < MAX_NODES_FOR_CALC) {
            maxNodesForCalc = InputTimeSeries.size();
        }

        for (int nodeGap = 1; nodeGap < maxNodesForCalc; nodeGap++) {
            for (int currentNodeIndex = 0; currentNodeIndex < (totalNodes - nodeGap); currentNodeIndex++) {
                int nodeToCompareIndex = currentNodeIndex + nodeGap;

                Boolean isVisible = checkVisibility(currentNodeIndex, nodeToCompareIndex);
                if (isVisible) {
                    insertNewEdge(currentNodeIndex, nodeToCompareIndex);
                    //PSVGGraphStore.storeVisibilityGraphInFile(currentNodeIndex, nodeToCompareIndex);
                }

            }
        }
    }
    
    private void insertNewEdge(int node, int adjnode) {
        //VgadjacencyPK vgadjacencyPK = new VgadjacencyPK(PSVG_RESULTS_TERM_INSTANCE_SLUG, node, adjnode);
        Vgadjacency vgadjacency = new Vgadjacency(PSVG_RESULTS_TERM_INSTANCE_SLUG, node, adjnode);
        try {
            em.persist(vgadjacency);
            if (rowCount % 3000 == 0) {
                System.out.println("Committing " + rowCount);
                em.flush();
                em.clear();

            }
        } catch (Exception ex) {
            Logger.getLogger(VisibilityDegree.class.getName()).log(Level.SEVERE, null, ex);
        }
        rowCount++;
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
        SimpleRegression PSVGRegSet = new SimpleRegression(FIT_INCLUDE_INTERCEPT);
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
        if (CHI_SQUARE_REJECT_CUT > 0.0) {
            calcPSVGChiSquareVal(PSVGRegSet);
        }
    }

    public void createDegreeDistribution() {

        VgadjacencyDAO vgadjacencyDAO = new VgadjacencyDAO(DatabaseConnection.EMF);
        Map<Integer, Integer> nodesAndDegreeMap = vgadjacencyDAO.getNodeCountsforDegree(PSVG_RESULTS_TERM_INSTANCE_SLUG);
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

    public void markOutliersOfDegreeDistribution() {
        

        int PSVGRequiredEnd = (int) ((int) vgDegreeDistributionList.size() * FIT_DATA_PART_FROM_START);
        /*
		 * We need at least 4 data points to fit and find the PSVG gradient.
         */
        int fitDataStartIndex = FIT_DATA_START_INDEX;
        if (PSVGRequiredEnd < (FIT_DATA_START_INDEX + 4)) {
            fitDataStartIndex = 0;
            PSVGRequiredEnd = vgDegreeDistributionList.size();
        }
        if (FIT_DATA_START_INDEX > vgDegreeDistributionList.size()) {
            fitDataStartIndex = 0;
            PSVGRequiredEnd = vgDegreeDistributionList.size();
        }
        for (int i = 0; i < vgDegreeDistributionList.size(); i++) {
            if (i < fitDataStartIndex) {
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
            if (diffExpectedActual <= CHI_SQUARE_REJECT_CUT) {
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
