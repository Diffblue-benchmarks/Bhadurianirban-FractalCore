/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.db.DAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import org.dgrf.fractal.db.JPA.EdgelistJpaController;
import org.dgrf.fractal.response.FractalResponseCode;

/**
 *
 * @author dgrfi
 */
public class EdgeListDAO extends EdgelistJpaController {

    public EdgeListDAO(EntityManagerFactory emf) {
        super(emf);
    }

    public int importVGGraphEdgeList(String psvgResultsTermInstanceSlug, String graphTermInstanceSlug) {

        String nativeQueryString = "insert into edgelist (graphslug, node, adjnode, edgelength) \n"
                + "select ?1, node, adjnode, realedgelength from vgadjacency where\n"
                + "psvgresultsslug = ?2";

        EntityManager em = getEntityManager();

        EntityTransaction entr = em.getTransaction();
        Query query = em.createNativeQuery(nativeQueryString);
        query.setParameter(1, graphTermInstanceSlug);
        query.setParameter(2, psvgResultsTermInstanceSlug);
        entr.begin();
        int executeUpdate = query.executeUpdate();
        entr.commit();
        int response;
        if (executeUpdate == 0) {
            response = FractalResponseCode.DB_NON_EXISTING;
        } else {
            response = FractalResponseCode.SUCCESS;
        }

        return response;

    }

}
