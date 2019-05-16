/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.core.graph;

import java.util.HashMap;
import java.util.Map;
import org.dgrf.cms.constants.CMSConstants;
import org.dgrf.cms.core.driver.CMSClientService;
import org.dgrf.cms.dto.TermInstanceDTO;
import org.dgrf.fractal.constants.FractalConstants;
import org.dgrf.fractal.core.dto.FractalDTO;
import org.dgrf.fractal.core.util.DatabaseConnection;
import org.dgrf.fractal.db.DAO.EdgeListDAO;
import org.dgrf.fractal.response.FractalResponseCode;
import org.dgrf.fractal.termmeta.GraphMeta;

/**
 *
 * @author dgrfi
 */
public class GraphCalcService {
    public FractalDTO importPSVGGraph(FractalDTO fractalDTO) {
        String importFromVGSlug = fractalDTO.getImportFromVGInstanceSlug();
        //generate graph terminstance slug for import 
        String graphTermInstanceSlug = importFromVGSlug.replace(FractalConstants.TERM_INSTANCE_SLUG_PSVG_EXT, FractalConstants.TERM_INSTANCE_SLUG_GRAPH_IMP_EXT);
        //copy graph data from VG
        EdgeListDAO edgeListDAO = new EdgeListDAO(DatabaseConnection.EMF);
        int response = edgeListDAO.importVGGraphEdgeList(importFromVGSlug, graphTermInstanceSlug);
        if (response != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(FractalResponseCode.DB_SEVERE);
            return fractalDTO;
        }
        CMSClientService cmscs = new CMSClientService();
        Map<String, Object> graphTermInstance = fractalDTO.getFractalTermInstance();
        
        graphTermInstance.put(CMSConstants.TERM_SLUG, FractalConstants.TERM_SLUG_GRAPH);
        graphTermInstance.put(CMSConstants.TERM_INSTANCE_SLUG, graphTermInstanceSlug);
        
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug(FractalConstants.TERM_SLUG_GRAPH);
        termInstanceDTO.setTermInstanceSlug(graphTermInstanceSlug);
        termInstanceDTO.setTermInstance(graphTermInstance);
        

        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        
        fractalDTO.setFractalTermInstance(graphTermInstance);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }
    public FractalDTO deleteGraph (FractalDTO fractalDTO) {
        Map<String,Object> graphTermInstance = fractalDTO.getFractalTermInstance();
        String graphTermInstanceSlug = (String)graphTermInstance.get(CMSConstants.TERM_INSTANCE_SLUG);
        CMSClientService cmscs = new CMSClientService();
        
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug(FractalConstants.TERM_SLUG_GRAPH);
        termInstanceDTO.setTermInstanceSlug(graphTermInstanceSlug);
        termInstanceDTO.setTermInstance(graphTermInstance);
        
        termInstanceDTO = cmscs.deleteTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        
        return fractalDTO;
    }
}
