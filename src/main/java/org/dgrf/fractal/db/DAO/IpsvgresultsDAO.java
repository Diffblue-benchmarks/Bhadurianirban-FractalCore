/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.db.DAO;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.dgrf.fractal.response.FractalResponseCode;
import org.dgrf.fractal.db.JPA.IpsvgresultsJpaController;
import org.dgrf.fractal.db.entities.Ipsvgresults;

/**
 *
 * @author bhaduri
 */
public class IpsvgresultsDAO extends IpsvgresultsJpaController {

    public IpsvgresultsDAO(EntityManagerFactory emf) {
        super(emf);
    }

    public int deletePsvgResultsById(String ipsvgResultsSlug) {
        int response;
        EntityManager em = getEntityManager();
        EntityTransaction entr = em.getTransaction();
        Query query = em.createNamedQuery("Ipsvgresults.deleteResultsByID");
        query.setParameter("ipsvgresultsslug", ipsvgResultsSlug);
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

    public List<Ipsvgresults> findResultsByID(String ipsvgResultsSlug) {
        EntityManager em = getEntityManager();
        TypedQuery<Ipsvgresults> query = em.createNamedQuery("Ipsvgresults.findResultsByID", Ipsvgresults.class);
        query.setParameter("ipsvgresultsslug", ipsvgResultsSlug);
        List<Ipsvgresults> IpsvgresultsList = query.getResultList();
        return IpsvgresultsList;
    }

}
