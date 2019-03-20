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
import org.dgrf.fractal.db.JPA.PsvgresultsJpaController;
import org.dgrf.fractal.db.entities.Psvgresults;

/**
 *
 * @author bhaduri
 */
public class PsvgresultsDAO extends PsvgresultsJpaController {

    public PsvgresultsDAO(EntityManagerFactory emf) {
        super(emf);
    }

    public int deletePsvgResultsById(String psvgResultsSlug) {
        int response;
        EntityManager em = getEntityManager();
        EntityTransaction entr = em.getTransaction();
        Query query = em.createNamedQuery("Psvgresults.deleteResultsByID");
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
    public List<Psvgresults> findResultsByID (String psvgResultsSlug) {
        EntityManager em = getEntityManager();
        TypedQuery<Psvgresults> query = em.createNamedQuery("Psvgresults.findResultsByID", Psvgresults.class);
        query.setParameter("psvgresultsslug", psvgResultsSlug);
        List<Psvgresults> PsvgresultsList = query.getResultList();
        return PsvgresultsList;
    }
}
