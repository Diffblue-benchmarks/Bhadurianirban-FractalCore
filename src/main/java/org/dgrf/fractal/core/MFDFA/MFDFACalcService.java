/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.core.MFDFA;

import org.dgrf.fractal.termmeta.MFDFAResultsMeta;
import org.dgrf.fractal.termmeta.MFDFAParamMeta;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import org.dgrf.fractal.response.FractalResponseCode;
import org.dgrf.cms.core.driver.CMSClientService;
import org.dgrf.cms.constants.CMSConstants;
import org.dgrf.cms.dto.TermInstanceDTO;
import org.dgrf.fractal.core.dto.FractalDTO;
import org.dgrf.fractal.db.DAO.DataSeriesDAO;
import org.dgrf.fractal.db.DAO.MfdfaresultsDAO;
import org.dgrf.fractal.db.JPA.exceptions.PreexistingEntityException;
import org.dgrf.fractal.db.entities.Mfdfaresults;
import org.dgrf.fractal.db.entities.MfdfaresultsPK;
import org.dgrf.fractal.core.util.DatabaseConnection;
import org.dgrf.fractal.constants.FractalConstants;
import org.dgrf.fractal.core.dto.MFDFAResultDTO;
import org.dgrf.fractal.core.util.LogUtil;

/**
 *
 * @author bhaduri
 */
public class MFDFACalcService {

    

    public MFDFACalcService() {

        
    }

    public FractalDTO deleteMFDFAResults(FractalDTO fractalDTO) {
        //delete mfdfa results data
        Map<String, Object> selectedMFDFATermInstance = fractalDTO.getFractalTermInstance();
        String mfdfaTermInstanceSlug = (String) selectedMFDFATermInstance.get(CMSConstants.TERM_INSTANCE_SLUG);

        MfdfaresultsDAO mfdfaresultsDAO = new MfdfaresultsDAO(DatabaseConnection.EMF);
        int response = mfdfaresultsDAO.deleteMfdfaResultsById(mfdfaTermInstanceSlug);
        
        fractalDTO.setResponseCode(response);
        if (response!=FractalResponseCode.SUCCESS) {
            return fractalDTO;
        }
        //delete mfdfa term instance
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

    public FractalDTO calculateMFDFA(FractalDTO fractalDTO) {
        String mfdfaParamSlug = fractalDTO.getParamSlug();
        String dataSeriesSlug = fractalDTO.getDataSeriesSlug();
        CMSClientService cmscs = new CMSClientService();
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
        termInstanceDTO.setTermSlug(FractalConstants.TERM_SLUG_MFDFA_PARAM);
        termInstanceDTO.setTermInstanceSlug(mfdfaParamSlug);
        termInstanceDTO = cmscs.getTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        Map<String, Object> selectedmfdfaParamData = termInstanceDTO.getTermInstance();

        Map<String, Object> mfdfaCalcResults = new HashMap<>();
        int dataseriesId = Integer.parseInt((String) selectedDataSeries.get("id"));
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
        List<Double> InputTimeSeries = dataSeriesDao.getDataSeriesById(dataseriesId).stream().map(ds -> ds.getYcumulative()).collect(Collectors.toList());
        //Starting MFDFA Calculation...................
        CalculateFq FqVal = new CalculateFq(scaleMax, scaleMin, scaleNumber, InputTimeSeries);
        CalculateFD FDVal = new CalculateFD(FqVal.getFqMatrix(), FqVal.getFVector(), FqVal.getQLinSpace(), FqVal.getExpLinSpace(), rejectCut);
        CalculateDq DqVal = new CalculateDq(FDVal.gethq(), FDVal.getTq(), FqVal.getQLinSpace());
        //Ending MFDFA Calculation...................
        //Retrieve MFDFA Calculation results................
        Double hurstExponent = FDVal.getHurstExp();
        Double hurstExponentSE = FDVal.getHurstExpSE();
        Double rSquaredVal = FDVal.getHurstExpRSquare();
        Double chiSquaredVal = FDVal.getMFDFAChiSquareVal();
        Double multiFractalWidth = FDVal.getMultiFractalWidth();

        DecimalFormat df = new DecimalFormat("####0.00");
        mfdfaCalcResults.put(MFDFAResultsMeta.DATASERIES, dataSeriesSlug);
        mfdfaCalcResults.put(MFDFAResultsMeta.MFDFA_PARAM, mfdfaParamSlug);
        mfdfaCalcResults.put(MFDFAResultsMeta.QUEUED, "No");
        mfdfaCalcResults.put(MFDFAResultsMeta.HURST_EXPONENT, df.format(hurstExponent));
        mfdfaCalcResults.put(MFDFAResultsMeta.HURST_EXPONENT_SE, df.format(hurstExponentSE));
        mfdfaCalcResults.put(MFDFAResultsMeta.MUILTI_FRACTAL_WIDTH, df.format(multiFractalWidth));
        mfdfaCalcResults.put(MFDFAResultsMeta.R_SQUARED_VAL, df.format(rSquaredVal));
        mfdfaCalcResults.put(MFDFAResultsMeta.CHI_SQUARED_VAL, df.format(chiSquaredVal));
        String mfdfaTermInstanceSlug = dataSeriesSlug + FractalConstants.TERM_INSTANCE_SLUG_MFDFA_EXT;
        mfdfaCalcResults.put(CMSConstants.TERM_INSTANCE_SLUG, mfdfaTermInstanceSlug);
        mfdfaCalcResults.put(CMSConstants.TERM_SLUG, FractalConstants.TERM_SLUG_MFDFA_CALC);
        //List<Map<String, Object>> mfdfaCalcMeta = mts.getTermMetaList(FractalConstants.TERM_SLUG_MFDFA_CALC);

        int response = storeMFDFAResults(Arrays.asList(FDVal.gethq()), Arrays.asList(DqVal.getDq()), mfdfaTermInstanceSlug);
        if (response != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(response);
            return fractalDTO;
        }
        //dBResponse = mts.saveTermInstance(mfdfaCalcMeta, mfdfaCalcResults);
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug((String) mfdfaCalcResults.get(CMSConstants.TERM_SLUG));
        termInstanceDTO.setTermInstanceSlug((String) mfdfaCalcResults.get(CMSConstants.TERM_INSTANCE_SLUG));
        termInstanceDTO.setTermInstance(mfdfaCalcResults);

        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        fractalDTO.setFractalTermInstance(mfdfaCalcResults);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }

    public FractalDTO queueMFDFACalculation(FractalDTO fractalDTO) {
        String mfdfaParamSlug = fractalDTO.getParamSlug();
        String dataSeriesSlug = fractalDTO.getDataSeriesSlug();
        CMSClientService cmscs = new CMSClientService();
        Map<String, Object> mfdfaCalcResults = new HashMap<>();
        mfdfaCalcResults.put(MFDFAResultsMeta.DATASERIES, dataSeriesSlug);
        mfdfaCalcResults.put(MFDFAResultsMeta.MFDFA_PARAM, mfdfaParamSlug);
        mfdfaCalcResults.put(MFDFAResultsMeta.QUEUED, "Yes");
        mfdfaCalcResults.put(MFDFAResultsMeta.HURST_EXPONENT, "--");
        mfdfaCalcResults.put(MFDFAResultsMeta.HURST_EXPONENT_SE, "--");
        mfdfaCalcResults.put(MFDFAResultsMeta.MUILTI_FRACTAL_WIDTH, "--");
        mfdfaCalcResults.put(MFDFAResultsMeta.R_SQUARED_VAL, "--");
        mfdfaCalcResults.put(MFDFAResultsMeta.CHI_SQUARED_VAL, "--");
        String mfdfaTermInstanceSlug = dataSeriesSlug + FractalConstants.TERM_INSTANCE_SLUG_MFDFA_EXT;
        mfdfaCalcResults.put(CMSConstants.TERM_INSTANCE_SLUG, mfdfaTermInstanceSlug);
        mfdfaCalcResults.put(CMSConstants.TERM_SLUG, FractalConstants.TERM_SLUG_MFDFA_CALC);
        //List<Map<String, Object>> mfdfaCalcMeta = mts.getTermMetaList(FractalConstants.TERM_SLUG_MFDFA_CALC);
        //DBResponse dBResponse = mts.saveTermInstance(mfdfaCalcMeta, mfdfaCalcResults);
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug((String) mfdfaCalcResults.get(CMSConstants.TERM_SLUG));
        termInstanceDTO.setTermInstanceSlug((String) mfdfaCalcResults.get(CMSConstants.TERM_INSTANCE_SLUG));
        termInstanceDTO.setTermInstance(mfdfaCalcResults);

        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }

        fractalDTO.setFractalTermInstance(mfdfaCalcResults);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }

    private int storeMFDFAResults(List<Double> HqList, List<Double> DqList, String mfdfaTermInstanceSlug) {
        int response;
        MfdfaresultsDAO mfdfaresultsDAO = new MfdfaresultsDAO(DatabaseConnection.EMF);
        for (int counter = 0; counter < HqList.size(); counter++) {
            MfdfaresultsPK mfdfaresultsPK = new MfdfaresultsPK(mfdfaTermInstanceSlug, counter);
            Mfdfaresults mfdfaresults = new Mfdfaresults(mfdfaresultsPK);

            mfdfaresults.setHq(HqList.get(counter));
            mfdfaresults.setDq(DqList.get(counter));
            try {
                mfdfaresultsDAO.create(mfdfaresults);
            } catch (PreexistingEntityException ex) {
                try {
                    mfdfaresultsDAO.edit(mfdfaresults);
                } catch (Exception ex1) {
                    Logger.getLogger(MFDFACalcService.class.getName()).log(Level.SEVERE, null, ex1);
                    response = FractalResponseCode.DB_SEVERE;
                    return response;
                }
            } catch (Exception ex) {
                Logger.getLogger(MFDFACalcService.class.getName()).log(Level.SEVERE, null, ex);
                response = FractalResponseCode.DB_SEVERE;
                return response;
            }
        }
        return FractalResponseCode.SUCCESS;
    }

    public FractalDTO getMfdfaResults(FractalDTO fractalDTO) {
        Map<String, Object> mfdfaResultInstance = fractalDTO.getFractalTermInstance();
        String mfdfaTermInstanceSlug = (String) mfdfaResultInstance.get(CMSConstants.TERM_INSTANCE_SLUG);

        MfdfaresultsDAO mfdfaresultsDAO = new MfdfaresultsDAO(DatabaseConnection.EMF);
        List<Mfdfaresults> mfdfaresultsList = mfdfaresultsDAO.findResultsByID(mfdfaTermInstanceSlug);
        List<MFDFAResultDTO> mfdfaResultDTOList = mfdfaresultsList.stream().map(mfdfaresults -> {
            MFDFAResultDTO mfdfaResultDTO = new MFDFAResultDTO();
            mfdfaResultDTO.setMfdfaresulsslug(mfdfaresults.getMfdfaresultsPK().getMfdfaresulsslug());
            mfdfaResultDTO.setResultid(mfdfaresults.getMfdfaresultsPK().getResultid());
            mfdfaResultDTO.setHq(mfdfaresults.getHq());
            mfdfaResultDTO.setDq(mfdfaresults.getDq());
            return mfdfaResultDTO;
        }).collect(Collectors.toList());
        fractalDTO.setMfdfaResultDTOs(mfdfaResultDTOList);
        return fractalDTO;
    }
}
