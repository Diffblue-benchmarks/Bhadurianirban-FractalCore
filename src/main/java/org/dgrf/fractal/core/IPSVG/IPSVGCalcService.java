/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.core.IPSVG;

import org.dgrf.fractal.termmeta.IPSVGResultsMeta;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;

import org.dgrf.cms.constants.CMSConstants;

import org.dgrf.cms.core.driver.CMSClientService;

import org.dgrf.cms.dto.TermInstanceDTO;

import org.dgrf.fractal.db.DAO.DataSeriesDAO;
import org.dgrf.fractal.db.DAO.IpsvgresultsDAO;
import org.dgrf.fractal.db.JPA.exceptions.PreexistingEntityException;
import org.dgrf.fractal.db.entities.Ipsvgresults;
import org.dgrf.fractal.core.PSVG.PSVGCalcService;
import org.dgrf.fractal.termmeta.PSVGParamMeta;
import org.dgrf.fractal.termmeta.DataSeriesMeta;
import org.dgrf.fractal.core.dto.FractalDTO;
import org.dgrf.fractal.core.util.DatabaseConnection;
import org.dgrf.fractal.constants.FractalConstants;
import org.dgrf.fractal.core.dto.IpsvgResultsDTO;
import org.dgrf.fractal.response.FractalResponseCode;

/**
 *
 * @author dgrfv
 */
public class IPSVGCalcService {

    private final EntityManagerFactory emf;

    public IPSVGCalcService() {
        emf = DatabaseConnection.EMF;
    }

    public FractalDTO calculateImprovedPSVG(FractalDTO fractalDTO) {
        String paramSlug = fractalDTO.getParamSlug();
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
        termInstanceDTO.setTermSlug(FractalConstants.TERM_SLUG_PSVG_PARAM);
        termInstanceDTO.setTermInstanceSlug(paramSlug);
        termInstanceDTO = cmscs.getTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        Map<String, Object> selectedPsvgParamData = termInstanceDTO.getTermInstance();

        Map<String, Object> ipsvgCalcResults = new HashMap<>();
        int dataseriesId = Integer.parseInt((String) selectedDataSeries.get(DataSeriesMeta.DATA_SERIES_ID));
        DataSeriesDAO dataSeriesDao = new DataSeriesDAO(emf);

        int PSVGRequiredStart = Integer.parseInt((String) selectedPsvgParamData.get(PSVGParamMeta.REQUIRED_START));
        double PSVGDataPartFromStart = Double.parseDouble((String) selectedPsvgParamData.get(PSVGParamMeta.DATA_PART_FROM_START));
        String strIncludePSVGInterCept = (String) selectedPsvgParamData.get(PSVGParamMeta.INCLUDE_INTERCEPT);
        boolean includePSVGInterCept = strIncludePSVGInterCept.equals("Yes");
        int maxNodesForCalc = Integer.parseInt((String) selectedPsvgParamData.get(PSVGParamMeta.MAX_NODES_FOR_CALC));
        double rejectCut = Double.parseDouble((String) selectedPsvgParamData.get(PSVGParamMeta.REJECT_CUT));
        double logBase = Double.parseDouble((String) selectedPsvgParamData.get(PSVGParamMeta.LOG_BASE));
        int maxGap = Integer.parseInt((String) selectedPsvgParamData.get(PSVGParamMeta.IPSVG_MAX_GAP));
        List<IPSVGDetails> IPSVGDetailsList;
        Double ipsvg;
        String ipsvgResultsTermInstanceSlug = dataSeriesSlug + FractalConstants.TERM_INSTANCE_SLUG_IPSVG_EXT;
        List<Double> InputTimeSeries = dataSeriesDao.getDataSeriesYvalPosById(dataseriesId);
        ImpVisibilityDegree impVisDegree = new ImpVisibilityDegree(InputTimeSeries, PSVGRequiredStart, PSVGDataPartFromStart, includePSVGInterCept, maxNodesForCalc, logBase, maxGap,ipsvgResultsTermInstanceSlug);
        impVisDegree.calculateVisibilityDegree();
        IPSVGDetailsList = impVisDegree.getImpPSVGList();
        ipsvg = impVisDegree.getImprovedPSVG();

        DecimalFormat df = new DecimalFormat("####0.00");
        ipsvgCalcResults.put(IPSVGResultsMeta.PSVG_PARAM, paramSlug);
        ipsvgCalcResults.put(IPSVGResultsMeta.DATASERIES, dataSeriesSlug);
        ipsvgCalcResults.put(IPSVGResultsMeta.IMPROVED_PSVG, df.format(ipsvg));
        ipsvgCalcResults.put(IPSVGResultsMeta.QUEUED, "No");
        
        ipsvgCalcResults.put(CMSConstants.TERM_INSTANCE_SLUG, ipsvgResultsTermInstanceSlug);
        ipsvgCalcResults.put(CMSConstants.TERM_SLUG, FractalConstants.TERM_SLUG_IPSVG_CALC);
        int response = storeIPSVGResults(ipsvgResultsTermInstanceSlug, IPSVGDetailsList);
        if (response != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(response);
            return fractalDTO;
        }
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug((String) ipsvgCalcResults.get(CMSConstants.TERM_SLUG));
        termInstanceDTO.setTermInstanceSlug((String) ipsvgCalcResults.get(CMSConstants.TERM_INSTANCE_SLUG));
        termInstanceDTO.setTermInstance(ipsvgCalcResults);

        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        fractalDTO.setFractalTermInstance(ipsvgCalcResults);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;

    }

    private int storeIPSVGResults(String ipsvgResultsTermInstanceSlug, List<IPSVGDetails> IPSVGDetailsList) {
        int response;
        IpsvgresultsDAO ipsvgresultsDAO = new IpsvgresultsDAO(emf);
        for (int i = 0; i < IPSVGDetailsList.size(); i++) {
            IPSVGDetails ipsvgDetails = IPSVGDetailsList.get(i);
            Ipsvgresults ipsvgresults = new Ipsvgresults(ipsvgResultsTermInstanceSlug, i);

            ipsvgresults.setLengthofgaps(ipsvgDetails.getLengthOfGaps());
            ipsvgresults.setPsvgforgaps(ipsvgDetails.getPSVGforGap());

            try {
                //psvgResultsList.add(psvgresults);
                ipsvgresultsDAO.create(ipsvgresults);
            } catch (PreexistingEntityException ex) {
                try {
                    ipsvgresultsDAO.edit(ipsvgresults);
                } catch (Exception ex1) {
                    Logger.getLogger(PSVGCalcService.class.getName()).log(Level.SEVERE, null, ex1);
                    response = FractalResponseCode.DB_SEVERE;
                    return response;
                }
            } catch (Exception ex) {
                Logger.getLogger(PSVGCalcService.class.getName()).log(Level.SEVERE, null, ex);
                response = FractalResponseCode.DB_SEVERE;
                return response;
            }
        }

        return FractalResponseCode.SUCCESS;
    }

    public FractalDTO queueImprovedPSVGCalculation(FractalDTO fractalDTO) {
        String paramSlug = fractalDTO.getParamSlug();
        String dataSeriesSlug = fractalDTO.getDataSeriesSlug();
        CMSClientService cmscs = new CMSClientService();
        Map<String, Object> ipsvgCalcResults = new HashMap<>();
        ipsvgCalcResults.put(IPSVGResultsMeta.PSVG_PARAM, paramSlug);
        ipsvgCalcResults.put(IPSVGResultsMeta.DATASERIES, dataSeriesSlug);
        ipsvgCalcResults.put(IPSVGResultsMeta.IMPROVED_PSVG, "--");
        ipsvgCalcResults.put(IPSVGResultsMeta.QUEUED, "Yes");
        String ipsvgResultsTermInstanceSlug = dataSeriesSlug + FractalConstants.TERM_INSTANCE_SLUG_IPSVG_EXT;
        ipsvgCalcResults.put(CMSConstants.TERM_INSTANCE_SLUG, ipsvgResultsTermInstanceSlug);
        ipsvgCalcResults.put(CMSConstants.TERM_SLUG, FractalConstants.TERM_SLUG_IPSVG_CALC);

        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug((String) ipsvgCalcResults.get(CMSConstants.TERM_SLUG));
        termInstanceDTO.setTermInstanceSlug((String) ipsvgCalcResults.get(CMSConstants.TERM_INSTANCE_SLUG));
        termInstanceDTO.setTermInstance(ipsvgCalcResults);

        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }

        fractalDTO.setFractalTermInstance(ipsvgCalcResults);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }

    public FractalDTO deletePSVGResults(FractalDTO fractalDTO) {
        //delete IPSVG Results
        Map<String, Object> selectedIPSVGTermInstance = fractalDTO.getFractalTermInstance();
        String iPsvgResultsTermInstanceSlug = (String) selectedIPSVGTermInstance.get(CMSConstants.TERM_INSTANCE_SLUG);

        IpsvgresultsDAO ipsvgresultsDAO = new IpsvgresultsDAO(emf);
        int response = ipsvgresultsDAO.deletePsvgResultsById(iPsvgResultsTermInstanceSlug);
        fractalDTO.setResponseCode(response);
        if (response != FractalResponseCode.SUCCESS) {
            return fractalDTO;
        }
        //delete IPSVG Term Instance
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

    public FractalDTO getPsvgResults(FractalDTO fractalDTO) {
        Map<String, Object> ipsvgResultInstance = fractalDTO.getFractalTermInstance();
        String iPsvgResultsTermInstanceSlug = (String) ipsvgResultInstance.get(CMSConstants.TERM_INSTANCE_SLUG);

        IpsvgresultsDAO ipsvgresultsDAO = new IpsvgresultsDAO(emf);
        List<Ipsvgresults> ipsvgresultsList = ipsvgresultsDAO.findResultsByID(iPsvgResultsTermInstanceSlug);
        List<IpsvgResultsDTO> ipsvgResultsDTOs = ipsvgresultsList.stream().map(ipsvgresults -> {
            IpsvgResultsDTO resultsDTO = new IpsvgResultsDTO();
            resultsDTO.setIpsvgResultsSlug(ipsvgresults.getIpsvgresultsPK().getIpsvgresultsslug());
            resultsDTO.setResultId(ipsvgresults.getIpsvgresultsPK().getResultid());
            resultsDTO.setLengthOfGaps(ipsvgresults.getLengthofgaps());
            resultsDTO.setPsvgForGaps(ipsvgresults.getPsvgforgaps());
            return resultsDTO;
        }).collect(Collectors.toList());
        fractalDTO.setIpsvgResultsDTOs(ipsvgResultsDTOs);
        return fractalDTO;
    }
}
