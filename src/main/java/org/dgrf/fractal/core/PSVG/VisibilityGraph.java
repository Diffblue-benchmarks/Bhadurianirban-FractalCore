/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.core.PSVG;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.dgrf.fractal.constants.FractalConstants;
import org.dgrf.fractal.core.util.DatabaseConnection;
import org.dgrf.fractal.core.util.LogUtil;
import org.dgrf.fractal.db.DAO.VgadjacencyDAO;
import org.dgrf.fractal.db.entities.Vgadjacency;

/**
 *
 * @author bhaduri
 */
abstract class VisibilityGraph {

    protected final List<?> InputTimeSeries;
    protected final int FIT_DATA_START_INDEX;
    protected final double FIT_DATA_PART_FROM_START;
    protected final double CHI_SQUARE_REJECT_CUT;
    protected final boolean FIT_INCLUDE_INTERCEPT;
    protected final int MAX_NODES_FOR_CALC;
    protected final String PSVG_RESULTS_TERM_INSTANCE_SLUG;

    protected List<VGDegreeDistribution> vgDegreeDistributionList;

    private double PSVGIntercept;
    private double PSVGFractalDimension;
    private double PSVGFractalDimensionSE;
    private double PSVGInterceptSE;
    private double PSVGRSquared;
    private double PSVGChiSquareVal;

    private EntityManager em;
    private int rowCount;

    public VisibilityGraph(List<?> InputTimeSeries, int PSVGRequiredStart, double PSVGDataPartFromStart,
            boolean includePSVGInterCept, int maxNodesForCalc, Double rejectCut, double logBase, String psvgResultsTermInstanceSlug) {
        this.InputTimeSeries = InputTimeSeries;
        this.FIT_DATA_START_INDEX = PSVGRequiredStart;
        this.FIT_DATA_PART_FROM_START = PSVGDataPartFromStart;
        this.FIT_INCLUDE_INTERCEPT = includePSVGInterCept;
        this.MAX_NODES_FOR_CALC = maxNodesForCalc;
        this.CHI_SQUARE_REJECT_CUT = rejectCut;
        LogUtil.setLogBase(logBase);
        this.PSVG_RESULTS_TERM_INSTANCE_SLUG = psvgResultsTermInstanceSlug;

    }
    abstract void createVGEdges();
    abstract Boolean checkVisibility(int currentNodeIndex, int nodeToCompareIndex);
    public void calculateVisibilityDegree() {

        //PSVGGraphStore.psvgresultsslug = PSVG_RESULTS_TERM_INSTANCE_SLUG;
        //PSVGGraphStore.createVisibilityGraphFile();
        
        VgadjacencyDAO vgadjacencyDAO = new VgadjacencyDAO(DatabaseConnection.EMF);
        vgadjacencyDAO.deleteVisibilityGraph(PSVG_RESULTS_TERM_INSTANCE_SLUG);
        em = vgadjacencyDAO.getEntityManager();
        em.getTransaction().begin();
        rowCount=0;
        
        createVGEdges();

        em.flush();
        em.clear();
        em.getTransaction().commit();
        //PSVGGraphStore.closeVisibilityGraphFile();
        //PSVGGraphStore.storeVisibilityGraphInDB(DatabaseConnection.EMF);
        //PSVGGraphStore.delVisibilityGraphFile();

        createDegreeDistribution();
        markOutliersOfDegreeDistribution();
        fitDegreeDistribution();

        if (PSVG_RESULTS_TERM_INSTANCE_SLUG.contains(FractalConstants.TERM_INSTANCE_SLUG_IPSVG_EXT)) {
            
            vgadjacencyDAO.deleteVisibilityGraph(PSVG_RESULTS_TERM_INSTANCE_SLUG);
        }

    }
    protected void insertNewEdge(int node, int adjnode) {
        //VgadjacencyPK vgadjacencyPK = new VgadjacencyPK(PSVG_RESULTS_TERM_INSTANCE_SLUG, node, adjnode);
        Vgadjacency vgadjacency = new Vgadjacency(PSVG_RESULTS_TERM_INSTANCE_SLUG, node, adjnode);
        try {
            em.persist(vgadjacency);
            if (rowCount % 3000 == 0) {
                //System.out.println("Committing " + rowCount);
                em.flush();
                em.clear();

            }
        } catch (Exception ex) {
            Logger.getLogger(VisibilityDegree.class.getName()).log(Level.SEVERE, null, ex);
        }
        rowCount++;
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
    private void calcPSVGChiSquareVal(SimpleRegression PSVGRegSet) {

        int listSize = 0;
        Double expectLogOfProbOfDegVal = 0.0;
        Double actualLogOfProbOfDegVal = 0.0;
        Double diffExpectedActual = 0.0;
        Double absExpectLogOfProbOfDegVal = 0.0;
        Double squaredDiffDivExpected = 0.0;
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
            Logger.getLogger(VisibilityDegree.class.getName()).log(Level.SEVERE, "Chi square could not be calculated");

            PSVGChiSquareVal = 999.0;
        }
        int degFreedom = listSize - 2;//2 is because there are expected and actual is for 2
        PSVGChiSquareVal = sumOfSquaredDiffDivExpected / degFreedom;

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

    public List<VGDegreeDistribution> getVgDegreeDistributionList() {
        return vgDegreeDistributionList;
    }

    public double getPSVGIntercept() {
        return PSVGIntercept;
    }

    public double getPSVGFractalDimension() {
        return PSVGFractalDimension;
    }

    public double getPSVGFractalDimensionSE() {
        return PSVGFractalDimensionSE;
    }

    public double getPSVGInterceptSE() {
        return PSVGInterceptSE;
    }

    public double getPSVGRSquared() {
        return PSVGRSquared;
    }

    public double getPSVGChiSquareVal() {
        return PSVGChiSquareVal;
    }
    
}
