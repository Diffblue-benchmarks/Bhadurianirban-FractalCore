/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.core.MFDXA;

import org.dgrf.fractal.termmeta.MFDXAResultsMeta;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import org.dgrf.fractal.response.FractalResponseCode;
import org.dgrf.cms.core.driver.CMSClientService;
import org.dgrf.cms.constants.CMSConstants;
import org.dgrf.cms.dto.TermInstanceDTO;
import org.dgrf.fractal.db.DAO.DataSeriesDAO;
import org.dgrf.fractal.termmeta.MFDFAParamMeta;
import org.dgrf.fractal.core.dto.FractalDTO;
import org.dgrf.fractal.core.util.DatabaseConnection;
import org.dgrf.fractal.constants.FractalConstants;
import org.dgrf.fractal.core.util.LogUtil;

/**
 *
 * @author dgrfv
 */
public class MFDXACalcService {

    

    public FractalDTO calculateMFDXA(FractalDTO fractalDTO) {
        String mfdfaParamSlug = fractalDTO.getParamSlug();
        String firstDataSeriesSlug = fractalDTO.getDataSeriesSlug();
        String secondDataSeriesSlug = fractalDTO.getDataSeriesSlugSecond();
        
        CMSClientService cmscs = new CMSClientService();
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug(FractalConstants.TERM_SLUG_DATASERIES);
        termInstanceDTO.setTermInstanceSlug(firstDataSeriesSlug);
        termInstanceDTO = cmscs.getTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        Map<String, Object> selectedFirstDataSeries = termInstanceDTO.getTermInstance();

        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug(FractalConstants.TERM_SLUG_DATASERIES);
        termInstanceDTO.setTermInstanceSlug(secondDataSeriesSlug);
        termInstanceDTO = cmscs.getTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        Map<String, Object> selectedSecondDataSeries = termInstanceDTO.getTermInstance();

        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug(FractalConstants.TERM_SLUG_MFDFA_PARAM);
        termInstanceDTO.setTermInstanceSlug(mfdfaParamSlug);
        termInstanceDTO = cmscs.getTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }

        Map<String, Object> selectedmfdfaParamData = termInstanceDTO.getTermInstance();

        int dataseriesIdFirst = Integer.parseInt((String) selectedFirstDataSeries.get("id"));
        int dataseriesIdSecond = Integer.parseInt((String) selectedSecondDataSeries.get("id"));
        DataSeriesDAO dataSeriesDao = new DataSeriesDAO(DatabaseConnection.EMF);

        Double scaleMin;
        Double scaleMax;
        int scaleNumber;
        Double rejectCut;
        Double logBase;

        scaleMin = Double.parseDouble((String) selectedmfdfaParamData.get(MFDFAParamMeta.SCALE_MIN));
        scaleMax = Double.parseDouble((String) selectedmfdfaParamData.get(MFDFAParamMeta.SCALE_MAX));
        scaleNumber = Integer.parseInt((String) selectedmfdfaParamData.get(MFDFAParamMeta.SCALE_NUMBER));
        rejectCut = Double.parseDouble((String) selectedmfdfaParamData.get(MFDFAParamMeta.REJECT_CUT));
        logBase = Double.parseDouble((String) selectedmfdfaParamData.get(MFDFAParamMeta.LOG_BASE));
        LogUtil.setLogBase(logBase);
        //get cumulative series
        List<Double> InputTimeSeriesFirst = dataSeriesDao.getDataSeriesById(dataseriesIdFirst).stream().map(ds -> ds.getYcumulative()).collect(Collectors.toList());
        List<Double> InputTimeSeriesSecond = dataSeriesDao.getDataSeriesById(dataseriesIdSecond).stream().map(ds -> ds.getYcumulative()).collect(Collectors.toList());
        //Starting MFDXA Calculation...................
        /**
         * Main method for calculating the MFDFA. 1) First this calculates the
         * overall q-order RMS : Fq (matrix of all the q order RMS for different
         * scales) and the monofractal RMS F (vector of all the second order
         * RMS) 2) The q-order Hurst exponent Hq (from singularity exponent
         * hq[]) 3) Singularity dimension Dq
         */
        CalculateMFDXAFq FqVal = new CalculateMFDXAFq(scaleMax, scaleMin, scaleNumber, InputTimeSeriesFirst, InputTimeSeriesSecond);
        CalculateMFDXAFD FDVal = new CalculateMFDXAFD(FqVal.getFVector(), rejectCut);

        //Ending MFDFA Calculation...................
        //Retrieve MFDFA Calculation results................
        Double hurstExponent = FDVal.getHurstExp();
        Double hurstExponentSE = FDVal.getHurstExpSE();
        Double rSquaredVal = FDVal.getHurstExpRSquare();
        Double chiSquaredVal = FDVal.getMFDXAChiSquareVal();
        Double gammaX = FDVal.getGammaX();
//        DBResponse dBResponse = storeMFDFAResults(Arrays.asList(FDVal.gethq()), Arrays.asList(DqVal.getDq()), dataseriesId);
//        if (dBResponse.getResponseCode() != DBResponse.SUCCESS) {
//            return null;
//        }
        DecimalFormat df = new DecimalFormat("####0.00");
        Map<String, Object> mfdxaCalcResults = new HashMap<>();
        mfdxaCalcResults.put(MFDXAResultsMeta.DATASERIES_FIRST, firstDataSeriesSlug);
        mfdxaCalcResults.put(MFDXAResultsMeta.DATASERIES_SECOND, secondDataSeriesSlug);
        mfdxaCalcResults.put(MFDXAResultsMeta.MFDFA_PARAM, mfdfaParamSlug);
        mfdxaCalcResults.put(MFDXAResultsMeta.QUEUED, "No");
        mfdxaCalcResults.put(MFDXAResultsMeta.HURST_EXPONENT, df.format(hurstExponent));
        mfdxaCalcResults.put(MFDXAResultsMeta.HURST_EXPONENT_SE, df.format(hurstExponentSE));
        mfdxaCalcResults.put(MFDXAResultsMeta.GAMMA_X, df.format(gammaX));
        mfdxaCalcResults.put(MFDXAResultsMeta.R_SQUARED_VAL, df.format(rSquaredVal));
        mfdxaCalcResults.put(MFDXAResultsMeta.CHI_SQUARED_VAL, df.format(chiSquaredVal));
        mfdxaCalcResults.put(CMSConstants.TERM_INSTANCE_SLUG, firstDataSeriesSlug + secondDataSeriesSlug + FractalConstants.TERM_INSTANCE_SLUG_MFDXA_EXT);
        mfdxaCalcResults.put(CMSConstants.TERM_SLUG, FractalConstants.TERM_SLUG_MFDXA_CALC);
        //List<Map<String, Object>> mfdxaCalcResultsMeta = mts.getTermMetaList(FractalConstants.TERM_SLUG_MFDXA_CALC);
        //DBResponse dBResponse = mts.saveTermInstance(mfdxaCalcResultsMeta, mfdxaCalcResults);
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug((String) mfdxaCalcResults.get(CMSConstants.TERM_SLUG));
        termInstanceDTO.setTermInstanceSlug((String) mfdxaCalcResults.get(CMSConstants.TERM_INSTANCE_SLUG));
        termInstanceDTO.setTermInstance(mfdxaCalcResults);
        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        fractalDTO.setFractalTermInstance(mfdxaCalcResults);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }

    public FractalDTO queueMFDXACalculation(FractalDTO fractalDTO) {
        String mfdfaParamSlug = fractalDTO.getParamSlug();
        String firstDataSeriesSlug = fractalDTO.getDataSeriesSlug();
        String secondDataSeriesSlug = fractalDTO.getDataSeriesSlugSecond();
        Map<String, Object> mfdxaCalcResults = new HashMap<>();
        mfdxaCalcResults.put(MFDXAResultsMeta.DATASERIES_FIRST, firstDataSeriesSlug);
        mfdxaCalcResults.put(MFDXAResultsMeta.DATASERIES_SECOND, secondDataSeriesSlug);
        mfdxaCalcResults.put(MFDXAResultsMeta.MFDFA_PARAM, mfdfaParamSlug);
        mfdxaCalcResults.put(MFDXAResultsMeta.QUEUED, "Yes");
        mfdxaCalcResults.put(MFDXAResultsMeta.HURST_EXPONENT, "--");
        mfdxaCalcResults.put(MFDXAResultsMeta.HURST_EXPONENT_SE, "--");
        mfdxaCalcResults.put(MFDXAResultsMeta.GAMMA_X, "--");
        mfdxaCalcResults.put(MFDXAResultsMeta.R_SQUARED_VAL, "--");
        mfdxaCalcResults.put(MFDXAResultsMeta.CHI_SQUARED_VAL, "--");
        mfdxaCalcResults.put(CMSConstants.TERM_INSTANCE_SLUG, firstDataSeriesSlug + secondDataSeriesSlug + FractalConstants.TERM_INSTANCE_SLUG_MFDXA_EXT);
        mfdxaCalcResults.put(CMSConstants.TERM_SLUG, FractalConstants.TERM_SLUG_MFDXA_CALC);
        CMSClientService cmscs = new CMSClientService();
        //List<Map<String, Object>> mfdxaCalcResultsMeta = mts.getTermMetaList(FractalConstants.TERM_SLUG_MFDXA_CALC);
        //DBResponse dBResponse = mts.saveTermInstance(mfdxaCalcResultsMeta, mfdxaCalcResults);
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug((String) mfdxaCalcResults.get(CMSConstants.TERM_SLUG));
        termInstanceDTO.setTermInstanceSlug((String) mfdxaCalcResults.get(CMSConstants.TERM_INSTANCE_SLUG));
        termInstanceDTO.setTermInstance(mfdxaCalcResults);

        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        fractalDTO.setFractalTermInstance(mfdxaCalcResults);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }
}
