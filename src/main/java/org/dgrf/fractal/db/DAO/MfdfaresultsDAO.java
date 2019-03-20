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
import org.dgrf.fractal.db.JPA.MfdfaresultsJpaController;
import org.dgrf.fractal.db.entities.Mfdfaresults;

/**
 *
 * @author bhaduri
 */
public class MfdfaresultsDAO extends MfdfaresultsJpaController {

    public MfdfaresultsDAO(EntityManagerFactory emf) {
        super(emf);
    }

    public int deleteMfdfaResultsById(String mfdfaTermInstanceSlug) {
        int response;
        EntityManager em = getEntityManager();
        EntityTransaction entr = em.getTransaction();
        Query query = em.createNamedQuery("Mfdfaresults.deleteResultsByID");
        query.setParameter("mfdfaresulsslug", mfdfaTermInstanceSlug);
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
    public List<Mfdfaresults> findResultsByID (String mfdfaTermInstanceSlug) {
        EntityManager em = getEntityManager();
        TypedQuery<Mfdfaresults> query = em.createNamedQuery("Mfdfaresults.findResultsByID", Mfdfaresults.class);
        query.setParameter("mfdfaresulsslug", mfdfaTermInstanceSlug);
        List<Mfdfaresults> MfdfaResultsList = query.getResultList();
        return MfdfaResultsList;
    }
}
