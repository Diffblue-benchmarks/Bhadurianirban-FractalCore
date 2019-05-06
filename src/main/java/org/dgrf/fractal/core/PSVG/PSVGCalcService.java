/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.core.PSVG;

import org.dgrf.fractal.termmeta.PSVGParamMeta;
import org.dgrf.fractal.termmeta.PSVGResultsMeta;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.dgrf.fractal.response.FractalResponseCode;
import org.dgrf.cms.core.driver.CMSClientService;
import org.dgrf.cms.constants.CMSConstants;
import org.dgrf.cms.dto.TermInstanceDTO;

import org.dgrf.fractal.db.DAO.DataSeriesDAO;
import org.dgrf.fractal.db.DAO.PsvgresultsDAO;
import org.dgrf.fractal.db.DAO.VgadjacencyDAO;
import org.dgrf.fractal.db.JPA.exceptions.PreexistingEntityException;
import org.dgrf.fractal.db.entities.Psvgresults;
import org.dgrf.fractal.termmeta.DataSeriesMeta;
import org.dgrf.fractal.core.dto.FractalDTO;
import org.dgrf.fractal.core.util.DatabaseConnection;
import org.dgrf.fractal.constants.FractalConstants;
import org.dgrf.fractal.core.dto.PSVGResultDTO;

/**
 *
 * @author bhaduri
 */
public class PSVGCalcService {

   

    

    public FractalDTO calculatePSVG(FractalDTO fractalDTO) {
        CMSClientService cmscs = new CMSClientService();
        
        String paramSlug = fractalDTO.getParamSlug();
        String dataSeriesSlug = fractalDTO.getDataSeriesSlug();
        String calcType = fractalDTO.getCalcType();
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug(FractalConstants.TERM_SLUG_DATASERIES);
        termInstanceDTO.setTermInstanceSlug(dataSeriesSlug);
        termInstanceDTO = cmscs.getTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        Map<String, Object> selectedDataSeries = termInstanceDTO.getTermInstance();
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug(FractalConstants.TERM_SLUG_PSVG_PARAM);
        termInstanceDTO.setTermInstanceSlug(paramSlug);
        termInstanceDTO = cmscs.getTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        Map<String, Object> selectedPsvgParamData = termInstanceDTO.getTermInstance();
        
        
        Map<String, Object> psvgCalcInstance;
        switch (calcType) {
            case FractalConstants.PSVG_CALC_TYPE_XY:
                psvgCalcInstance = calculatePSVGXY(selectedPsvgParamData, selectedDataSeries);
                break;
            case FractalConstants.PSVG_CALC_TYPE_N:
                psvgCalcInstance = calculatePSVGNormal(selectedPsvgParamData, selectedDataSeries);
                break;
            case FractalConstants.PSVG_CALC_TYPE_H:
                psvgCalcInstance = calculatePSVGHorizontal(selectedPsvgParamData, selectedDataSeries);
                break;
            default:
                psvgCalcInstance = null;
                break;
        }


        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug((String) psvgCalcInstance.get(CMSConstants.TERM_SLUG));
        termInstanceDTO.setTermInstanceSlug((String) psvgCalcInstance.get(CMSConstants.TERM_INSTANCE_SLUG));
        termInstanceDTO.setTermInstance(psvgCalcInstance);

        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        fractalDTO.setFractalTermInstance(psvgCalcInstance);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }

    private Map<String, Object> calculatePSVGNormal(Map<String, Object> selectedPsvgParamData, Map<String, Object> selectedDataSeries) {
        Map<String, Object> psvgCalcResults = new HashMap<>();
        int dataseriesId = Integer.parseInt((String) selectedDataSeries.get(DataSeriesMeta.DATA_SERIES_ID));
        DataSeriesDAO dataSeriesDao = new DataSeriesDAO(DatabaseConnection.EMF);

        int PSVGRequiredStart = Integer.parseInt((String) selectedPsvgParamData.get(PSVGParamMeta.REQUIRED_START));
        double PSVGDataPartFromStart = Double.parseDouble((String) selectedPsvgParamData.get(PSVGParamMeta.DATA_PART_FROM_START));
        String strIncludePSVGInterCept = (String) selectedPsvgParamData.get(PSVGParamMeta.INCLUDE_INTERCEPT);
        boolean includePSVGInterCept = strIncludePSVGInterCept.equals("Yes");
        int maxNodesForCalc = Integer.parseInt((String) selectedPsvgParamData.get(PSVGParamMeta.MAX_NODES_FOR_CALC));
        double rejectCut = Double.parseDouble((String) selectedPsvgParamData.get(PSVGParamMeta.REJECT_CUT));
        double logBase = Double.parseDouble((String) selectedPsvgParamData.get(PSVGParamMeta.LOG_BASE));

        String psvgResultsTermInstanceSlug = selectedDataSeries.get(CMSConstants.TERM_INSTANCE_SLUG) + FractalConstants.TERM_INSTANCE_SLUG_PSVG_EXT + FractalConstants.PSVG_CALC_TYPE_N;

        List<PSVGDetails> PSVGDetailsList;
        Double PSVGIntercept, PSVGFractalDimension, PSVGInterceptSE, PSVGFractalDimensionSE, PSVGChiSquareVal, PSVGRSquared;

        List<Double> InputTimeSeries = dataSeriesDao.getDataSeriesYvalPosById(dataseriesId);
        //write VG in file.
        //open temp VG in file.
       
        
        //initialise VG Calculation
        VisibilityDegree visDegree = new VisibilityDegree(InputTimeSeries, PSVGRequiredStart, PSVGDataPartFromStart, includePSVGInterCept, maxNodesForCalc, rejectCut, logBase,psvgResultsTermInstanceSlug);
        visDegree.calculateVisibilityDegree();
        //close  temp VG in file.
        
        //store the Graph in table as adjacency list
        
        //delete temp VG in file.
        
        PSVGDetailsList = visDegree.getPSVGList();
        PSVGIntercept = visDegree.getPSVGIntercept();
        PSVGFractalDimension = visDegree.getPSVGFractalDimension();
        PSVGInterceptSE = visDegree.getPSVGInterceptSE();
        PSVGFractalDimensionSE = visDegree.getPSVGFractalDimensionSE();
        PSVGChiSquareVal = visDegree.getPSVGChiSquareVal();
        PSVGRSquared = visDegree.getPSVGRSquared();

        DecimalFormat df = new DecimalFormat("####0.00");
        psvgCalcResults.put(PSVGResultsMeta.PSVG_PARAM, selectedPsvgParamData.get(CMSConstants.TERM_INSTANCE_SLUG));
        psvgCalcResults.put(PSVGResultsMeta.DATASERIES, selectedDataSeries.get(CMSConstants.TERM_INSTANCE_SLUG));
        psvgCalcResults.put(PSVGResultsMeta.PSVG_CALC_TYPE, FractalConstants.PSVG_CALC_TYPE_N);
        psvgCalcResults.put(PSVGResultsMeta.FRACTAL_DIMENSION, df.format(PSVGFractalDimension));
        psvgCalcResults.put(PSVGResultsMeta.INTERCEPT, df.format(PSVGIntercept));
        psvgCalcResults.put(PSVGResultsMeta.INTERCEPT_SE, df.format(PSVGInterceptSE));
        psvgCalcResults.put(PSVGResultsMeta.FRACTAL_DIMENSION_SE, df.format(PSVGFractalDimensionSE));
        psvgCalcResults.put(PSVGResultsMeta.CHI_SQUARED_VAL, df.format(PSVGChiSquareVal));
        psvgCalcResults.put(PSVGResultsMeta.R_SQUARED_VAL, df.format(PSVGRSquared));
        psvgCalcResults.put(PSVGResultsMeta.QUEUED, "No");
        psvgCalcResults.put(CMSConstants.TERM_SLUG, FractalConstants.TERM_SLUG_PSVG_CALC);
        psvgCalcResults.put(CMSConstants.TERM_INSTANCE_SLUG, psvgResultsTermInstanceSlug);
        PSVGGraphStore.psvgresultsslug = psvgResultsTermInstanceSlug;
        int response = storePSVGResults(psvgResultsTermInstanceSlug, PSVGDetailsList);
        if (response != FractalResponseCode.SUCCESS) {
            return null;
        }
        return psvgCalcResults;
    }

    private Map<String, Object> calculatePSVGHorizontal(Map<String, Object> selectedPsvgParamData, Map<String, Object> selectedDataSeries) {
        Map<String, Object> psvgCalcResults = new HashMap<>();
        int dataseriesId = Integer.parseInt((String) selectedDataSeries.get(DataSeriesMeta.DATA_SERIES_ID));
        DataSeriesDAO dataSeriesDao = new DataSeriesDAO(DatabaseConnection.EMF);

        int PSVGRequiredStart = Integer.parseInt((String) selectedPsvgParamData.get(PSVGParamMeta.REQUIRED_START));
        double PSVGDataPartFromStart = Double.parseDouble((String) selectedPsvgParamData.get(PSVGParamMeta.DATA_PART_FROM_START));
        String strIncludePSVGInterCept = (String) selectedPsvgParamData.get(PSVGParamMeta.INCLUDE_INTERCEPT);
        boolean includePSVGInterCept = strIncludePSVGInterCept.equals("Yes");
        int maxNodesForCalc = Integer.parseInt((String) selectedPsvgParamData.get(PSVGParamMeta.MAX_NODES_FOR_CALC));
        double rejectCut = Double.parseDouble((String) selectedPsvgParamData.get(PSVGParamMeta.REJECT_CUT));
        double logBase = Double.parseDouble((String) selectedPsvgParamData.get(PSVGParamMeta.LOG_BASE));
        List<PSVGDetails> PSVGDetailsList;
        Double PSVGIntercept, PSVGFractalDimension, PSVGInterceptSE, PSVGFractalDimensionSE, PSVGChiSquareVal, PSVGRSquared;

        List<Double> InputTimeSeries = dataSeriesDao.getDataSeriesYvalPosById(dataseriesId);
        String psvgResultsTermInstanceSlug = selectedDataSeries.get(CMSConstants.TERM_INSTANCE_SLUG) + FractalConstants.TERM_INSTANCE_SLUG_PSVG_EXT + FractalConstants.PSVG_CALC_TYPE_H;
        //write VG in file.
        //open temp VG in file.
        PSVGGraphStore.psvgresultsslug = psvgResultsTermInstanceSlug;
        PSVGGraphStore.createVisibilityGraphFile();
        //initialise VG Calculation

        hVisibilityDegree hvisDegree = new hVisibilityDegree(InputTimeSeries, PSVGRequiredStart, PSVGDataPartFromStart, includePSVGInterCept, maxNodesForCalc, logBase);
        hvisDegree.calculateVisibilityDegree();
        //close  temp VG in file.
        PSVGGraphStore.closeVisibilityGraphFile();
        //store the Graph in table as adjacency list
        PSVGGraphStore.storeVisibilityGraphInDB(DatabaseConnection.EMF);
        //delete temp VG in file.
        PSVGGraphStore.delVisibilityGraphFile();
        PSVGDetailsList = hvisDegree.getPSVGList();
        PSVGIntercept = hvisDegree.getPSVGIntercept();
        PSVGFractalDimension = hvisDegree.getPSVGFractalDimension();

        DecimalFormat df = new DecimalFormat("####0.00");
        psvgCalcResults.put(PSVGResultsMeta.PSVG_PARAM, selectedPsvgParamData.get(CMSConstants.TERM_INSTANCE_SLUG));
        psvgCalcResults.put(PSVGResultsMeta.DATASERIES, selectedDataSeries.get(CMSConstants.TERM_INSTANCE_SLUG));
        psvgCalcResults.put(PSVGResultsMeta.FRACTAL_DIMENSION, df.format(PSVGFractalDimension));
        psvgCalcResults.put(PSVGResultsMeta.PSVG_CALC_TYPE, FractalConstants.PSVG_CALC_TYPE_H);
        psvgCalcResults.put(PSVGResultsMeta.INTERCEPT, df.format(PSVGIntercept));
        psvgCalcResults.put(PSVGResultsMeta.INTERCEPT_SE, "NA");
        psvgCalcResults.put(PSVGResultsMeta.FRACTAL_DIMENSION_SE, "NA");
        psvgCalcResults.put(PSVGResultsMeta.CHI_SQUARED_VAL, "NA");
        psvgCalcResults.put(PSVGResultsMeta.R_SQUARED_VAL, "NA");
        psvgCalcResults.put(PSVGResultsMeta.QUEUED, "No");
        psvgCalcResults.put(CMSConstants.TERM_SLUG, FractalConstants.TERM_SLUG_PSVG_CALC);
        psvgCalcResults.put(CMSConstants.TERM_INSTANCE_SLUG, psvgResultsTermInstanceSlug);

        int response = storePSVGResults(psvgResultsTermInstanceSlug, PSVGDetailsList);
        if (response != FractalResponseCode.SUCCESS) {
            return null;
        }
        return psvgCalcResults;
    }

    private Map<String, Object> calculatePSVGXY(Map<String, Object> selectedPsvgParamData, Map<String, Object> selectedDataSeries) {
        Map<String, Object> psvgCalcResults = new HashMap<>();
        int dataseriesId = Integer.parseInt((String) selectedDataSeries.get(DataSeriesMeta.DATA_SERIES_ID));
        DataSeriesDAO dataSeriesDao = new DataSeriesDAO(DatabaseConnection.EMF);

        int PSVGRequiredStart = Integer.parseInt((String) selectedPsvgParamData.get(PSVGParamMeta.REQUIRED_START));
        double PSVGDataPartFromStart = Double.parseDouble((String) selectedPsvgParamData.get(PSVGParamMeta.DATA_PART_FROM_START));
        String strIncludePSVGInterCept = (String) selectedPsvgParamData.get(PSVGParamMeta.INCLUDE_INTERCEPT);
        boolean includePSVGInterCept = strIncludePSVGInterCept.equals("Yes");
        int maxNodesForCalc = Integer.parseInt((String) selectedPsvgParamData.get(PSVGParamMeta.MAX_NODES_FOR_CALC));
        double rejectCut = Double.parseDouble((String) selectedPsvgParamData.get(PSVGParamMeta.REJECT_CUT));
        double logBase = Double.parseDouble((String) selectedPsvgParamData.get(PSVGParamMeta.LOG_BASE));
        String psvgResultsTermInstanceSlug = selectedDataSeries.get(CMSConstants.TERM_INSTANCE_SLUG) + FractalConstants.TERM_INSTANCE_SLUG_PSVG_EXT + FractalConstants.PSVG_CALC_TYPE_XY;
        List<PSVGDetails> PSVGDetailsList;
        Double PSVGIntercept, PSVGFractalDimension, PSVGInterceptSE, PSVGFractalDimensionSE, PSVGChiSquareVal, PSVGRSquared;

        List<XYData> InputTimeSeries = dataSeriesDao.getDataSeriesXYPosById(dataseriesId);
        //write VG in file.
        //open temp VG in file.
        PSVGGraphStore.psvgresultsslug = psvgResultsTermInstanceSlug;
        PSVGGraphStore.createVisibilityGraphFile();
        //initialise VG Calculation

        VisibilityXYDegree visXYDegree = new VisibilityXYDegree(InputTimeSeries, PSVGRequiredStart, PSVGDataPartFromStart, includePSVGInterCept, maxNodesForCalc, rejectCut, logBase);
        visXYDegree.calculateVisibilityDegree();
        //close  temp VG in file.
        PSVGGraphStore.closeVisibilityGraphFile();
        //store the Graph in table as adjacency list
        PSVGGraphStore.storeVisibilityGraphInDB(DatabaseConnection.EMF);
        //delete temp VG in file.
        PSVGGraphStore.delVisibilityGraphFile();
        PSVGDetailsList = visXYDegree.getPSVGList();
        PSVGIntercept = visXYDegree.getPSVGIntercept();
        PSVGFractalDimension = visXYDegree.getPSVGFractalDimension();
        PSVGInterceptSE = visXYDegree.getPSVGInterceptSE();
        PSVGFractalDimensionSE = visXYDegree.getPSVGFractalDimensionSE();
        PSVGChiSquareVal = visXYDegree.getPSVGChiSquareVal();
        PSVGRSquared = visXYDegree.getPSVGRSquared();

        //List<Psvgresults> psvgResultsList = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("####0.00");
        psvgCalcResults.put(PSVGResultsMeta.PSVG_PARAM, selectedPsvgParamData.get(CMSConstants.TERM_INSTANCE_SLUG));
        psvgCalcResults.put(PSVGResultsMeta.DATASERIES, selectedDataSeries.get(CMSConstants.TERM_INSTANCE_SLUG));
        psvgCalcResults.put(PSVGResultsMeta.FRACTAL_DIMENSION, df.format(PSVGFractalDimension));
        psvgCalcResults.put(PSVGResultsMeta.PSVG_CALC_TYPE, FractalConstants.PSVG_CALC_TYPE_XY);
        psvgCalcResults.put(PSVGResultsMeta.INTERCEPT, df.format(PSVGIntercept));
        psvgCalcResults.put(PSVGResultsMeta.INTERCEPT_SE, df.format(PSVGInterceptSE));
        psvgCalcResults.put(PSVGResultsMeta.FRACTAL_DIMENSION_SE, df.format(PSVGFractalDimensionSE));
        psvgCalcResults.put(PSVGResultsMeta.CHI_SQUARED_VAL, df.format(PSVGChiSquareVal));
        psvgCalcResults.put(PSVGResultsMeta.R_SQUARED_VAL, df.format(PSVGRSquared));
        psvgCalcResults.put(PSVGResultsMeta.QUEUED, "No");
        psvgCalcResults.put(CMSConstants.TERM_SLUG, FractalConstants.TERM_SLUG_PSVG_CALC);
        psvgCalcResults.put(CMSConstants.TERM_INSTANCE_SLUG, psvgResultsTermInstanceSlug);
        PSVGGraphStore.psvgresultsslug = psvgResultsTermInstanceSlug;
        int response = storePSVGResults(psvgResultsTermInstanceSlug, PSVGDetailsList);
        if (response != FractalResponseCode.SUCCESS) {
            return null;
        }
        return psvgCalcResults;
    }

    public FractalDTO queuePSVGCalculation(FractalDTO fractalDTO) {
        String paramSlug = fractalDTO.getParamSlug();
        String dataSeriesSlug = fractalDTO.getDataSeriesSlug();
        String calcType = fractalDTO.getCalcType();
        CMSClientService cmscs = new CMSClientService();
        Map<String, Object> psvgCalcResults = new HashMap<>();

        psvgCalcResults.put(PSVGResultsMeta.PSVG_PARAM, paramSlug);
        psvgCalcResults.put(PSVGResultsMeta.DATASERIES, dataSeriesSlug);
        psvgCalcResults.put(PSVGResultsMeta.PSVG_CALC_TYPE, calcType);
        psvgCalcResults.put(PSVGResultsMeta.FRACTAL_DIMENSION, "--");
        psvgCalcResults.put(PSVGResultsMeta.INTERCEPT, "--");
        psvgCalcResults.put(PSVGResultsMeta.INTERCEPT_SE, "--");
        psvgCalcResults.put(PSVGResultsMeta.FRACTAL_DIMENSION_SE, "--");
        psvgCalcResults.put(PSVGResultsMeta.CHI_SQUARED_VAL, "--");
        psvgCalcResults.put(PSVGResultsMeta.R_SQUARED_VAL, "--");
        psvgCalcResults.put(PSVGResultsMeta.QUEUED, "Yes");

        String psvgResultsTermInstanceSlug = dataSeriesSlug + FractalConstants.TERM_INSTANCE_SLUG_PSVG_EXT + calcType;
        psvgCalcResults.put(CMSConstants.TERM_SLUG, FractalConstants.TERM_SLUG_PSVG_CALC);
        psvgCalcResults.put(CMSConstants.TERM_INSTANCE_SLUG, psvgResultsTermInstanceSlug);
        //List<Map<String, Object>> psvgMetaKeys = mts.getTermMetaList(FractalConstants.TERM_SLUG_PSVG_CALC);
        
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug((String) psvgCalcResults.get(CMSConstants.TERM_SLUG));
        termInstanceDTO.setTermInstanceSlug((String) psvgCalcResults.get(CMSConstants.TERM_INSTANCE_SLUG));
        termInstanceDTO.setTermInstance(psvgCalcResults);
        
        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }

        fractalDTO.setFractalTermInstance(psvgCalcResults);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }

    private int storePSVGResults(String psvgResultsTermInstanceSlug, List<PSVGDetails> PSVGDetailsList) {
        int response;
        PsvgresultsDAO psvgresultsDAO = new PsvgresultsDAO(DatabaseConnection.EMF);
        for (int i = 0; i < PSVGDetailsList.size(); i++) {
            PSVGDetails psvgDetails = PSVGDetailsList.get(i);
            Psvgresults psvgresults = new Psvgresults(psvgResultsTermInstanceSlug, i);
            psvgresults.setDegreeval(psvgDetails.getDegValue());
            psvgresults.setProbofdegreeval(psvgDetails.getProbOfDegVal());
            psvgresults.setLogofdegreeval(psvgDetails.getLogOfDegVal());
            psvgresults.setLogofprobofdegreeval(psvgDetails.getlogOfProbOfDegVal());
            psvgresults.setNodeswithdegval(psvgDetails.getNumOfNodesWithDegVal());
            if (psvgDetails.getIsRequired()) {
                psvgresults.setRequired(Short.parseShort("1"));
            } else {
                psvgresults.setRequired(Short.parseShort("0"));
            }
            try {
                //psvgResultsList.add(psvgresults);
                psvgresultsDAO.create(psvgresults);
            } catch (PreexistingEntityException ex) {
                try {
                    psvgresultsDAO.edit(psvgresults);
                } catch (Exception ex1) {
                    Logger.getLogger(PSVGCalcService.class.getName()).log(Level.SEVERE, null, ex);
                    return FractalResponseCode.DB_SEVERE;
                }
            } catch (Exception ex) {
                Logger.getLogger(PSVGCalcService.class.getName()).log(Level.SEVERE, null, ex);
                return FractalResponseCode.DB_SEVERE;
            }
        }
        return FractalResponseCode.SUCCESS;
    }

    public FractalDTO deletePSVGResults(FractalDTO fractalDTO) {
        //delete k pk values
        Map<String, Object> selectedPSVGTermInstance = fractalDTO.getFractalTermInstance();
        String psvgResultsTermInstanceSlug = (String) selectedPSVGTermInstance.get(CMSConstants.TERM_INSTANCE_SLUG);
        PsvgresultsDAO psvgresultsDAO = new PsvgresultsDAO(DatabaseConnection.EMF);
        int response = psvgresultsDAO.deletePsvgResultsById(psvgResultsTermInstanceSlug);
        fractalDTO.setResponseCode(response);
        if (response!=FractalResponseCode.SUCCESS && response!=FractalResponseCode.DB_NON_EXISTING) {
            return fractalDTO;
        }
        //delete generated VG
        fractalDTO = deleteVisibilityGraph(fractalDTO);
        response = fractalDTO.getResponseCode();
        if (response!=FractalResponseCode.SUCCESS && response!=FractalResponseCode.DB_NON_EXISTING) {
            return fractalDTO;
        }
        //delete VG termInstance
        CMSClientService cmscs = new CMSClientService();
        Map<String, Object> fractalTermInstance = fractalDTO.getFractalTermInstance();
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        String termSlug = (String) fractalTermInstance.get(CMSConstants.TERM_SLUG);
        String termInstanceSlug = (String) fractalTermInstance.get(CMSConstants.TERM_INSTANCE_SLUG);
        termInstanceDTO.setTermSlug(termSlug);
        termInstanceDTO.setTermInstanceSlug(termInstanceSlug);
        termInstanceDTO = cmscs.deleteTermInstance(termInstanceDTO);

        response = termInstanceDTO.getResponseCode();
        fractalDTO.setResponseCode(response);
        if (response!=FractalResponseCode.SUCCESS) {
            return fractalDTO;
        }
        return fractalDTO;

    }

    public FractalDTO deleteVisibilityGraph(FractalDTO fractalDTO) {
        Map<String, Object> selectedPSVGTermInstance = fractalDTO.getFractalTermInstance();
        String psvgResultsTermInstanceSlug = (String) selectedPSVGTermInstance.get(CMSConstants.TERM_INSTANCE_SLUG);
        VgadjacencyDAO vgadjacencyDAO = new VgadjacencyDAO(DatabaseConnection.EMF);
        int response = vgadjacencyDAO.deleteVisibilityGraph(psvgResultsTermInstanceSlug);
        fractalDTO.setResponseCode(response);
        return fractalDTO;
    }

    public FractalDTO getPsvgResults(FractalDTO fractalDTO) {
        Map<String, Object> psvgResultInstance = fractalDTO.getFractalTermInstance();
        String psvgResultsTermInstanceSlug = (String) psvgResultInstance.get(CMSConstants.TERM_INSTANCE_SLUG);
        //Map<String, Object> psvgDataSeriesInstance = mts.getScreenTermInstance(PSVGResultsMeta.DATASERIES, psvgDataSeriesInstanceSlug);
        //String psvgResultsSlug = (String) psvgDataSeriesInstance.get("id");
        PsvgresultsDAO psvgresultsDAO = new PsvgresultsDAO(DatabaseConnection.EMF);
        List<Psvgresults> psvgresultsList = psvgresultsDAO.findResultsByID(psvgResultsTermInstanceSlug);
        List<PSVGResultDTO> psvgResultsDTOs = psvgresultsList.stream().map(psvgresults ->  {
            PSVGResultDTO resultsDTO = new PSVGResultDTO();
            resultsDTO.setPsvgresultsslug(psvgresults.getPsvgresultsPK().getPsvgresultsslug());
            resultsDTO.setResultid(psvgresults.getPsvgresultsPK().getResultid());
            resultsDTO.setRequired(psvgresults.getRequired());
            resultsDTO.setDegreeval(psvgresults.getDegreeval());
            resultsDTO.setLogofdegreeval(psvgresults.getLogofdegreeval());
            resultsDTO.setNodeswithdegval(psvgresults.getNodeswithdegval());
            resultsDTO.setProbofdegreeval(psvgresults.getProbofdegreeval());
            resultsDTO.setLogofprobofdegreeval(psvgresults.getLogofprobofdegreeval());
            return resultsDTO;
        }).collect(Collectors.toList());
        fractalDTO.setPsvgResultDTOs(psvgResultsDTOs);
        return fractalDTO;
    }

}
