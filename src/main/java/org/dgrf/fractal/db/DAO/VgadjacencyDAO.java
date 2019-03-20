/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.db.DAO;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import org.dgrf.fractal.response.FractalResponseCode;
import org.dgrf.fractal.db.JPA.VgadjacencyJpaController;

/**
 *
 * @author bhaduri
 */
public class VgadjacencyDAO extends VgadjacencyJpaController {

    public VgadjacencyDAO(EntityManagerFactory emf) {
        super(emf);
    }

    public int loadVisibilityGraph(String fileName) {
        int response;
        EntityManager em = getEntityManager();
        EntityTransaction entr = em.getTransaction();
        String nativeQueryString = "LOAD DATA LOCAL INFILE '" + fileName + "' INTO TABLE vgadjacency FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n'";
        
        Query query = em.createNativeQuery(nativeQueryString);
        
        entr.begin();
        int executeUpdate = query.executeUpdate();
        if (executeUpdate == 0) {
            response = FractalResponseCode.DB_NON_EXISTING;
        } else {
            response = FractalResponseCode.SUCCESS;
        }
        entr.commit();
        return response;
    }

    public int deleteVisibilityGraph(String psvgResultsSlug) {
        int response;
        EntityManager em = getEntityManager();
        EntityTransaction entr = em.getTransaction();
        Query query = em.createNamedQuery("Vgadjacency.deleteById");
        query.setParameter("psvgresultsslug", psvgResultsSlug);
        entr.begin();
        int executeUpdate = query.executeUpdate();
        if (executeUpdate == 0) {
            response = FractalResponseCode.DB_NON_EXISTING;
        } else {
            response = FractalResponseCode.SUCCESS;
        }
        entr.commit();
        return response;
    }
}
