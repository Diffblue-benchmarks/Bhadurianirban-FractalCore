/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.db.JPA;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.dgrf.fractal.db.JPA.exceptions.NonexistentEntityException;
import org.dgrf.fractal.db.JPA.exceptions.PreexistingEntityException;
import org.dgrf.fractal.db.entities.Psvgresults;
import org.dgrf.fractal.db.entities.PsvgresultsPK;

/**
 *
 * @author bhaduri
 */
public class PsvgresultsJpaController implements Serializable {

    public PsvgresultsJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Psvgresults psvgresults) throws PreexistingEntityException, Exception {
        if (psvgresults.getPsvgresultsPK() == null) {
            psvgresults.setPsvgresultsPK(new PsvgresultsPK());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(psvgresults);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findPsvgresults(psvgresults.getPsvgresultsPK()) != null) {
                throw new PreexistingEntityException("Psvgresults " + psvgresults + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Psvgresults psvgresults) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            psvgresults = em.merge(psvgresults);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                PsvgresultsPK id = psvgresults.getPsvgresultsPK();
                if (findPsvgresults(id) == null) {
                    throw new NonexistentEntityException("The psvgresults with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(PsvgresultsPK id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Psvgresults psvgresults;
            try {
                psvgresults = em.getReference(Psvgresults.class, id);
                psvgresults.getPsvgresultsPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The psvgresults with id " + id + " no longer exists.", enfe);
            }
            em.remove(psvgresults);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Psvgresults> findPsvgresultsEntities() {
        return findPsvgresultsEntities(true, -1, -1);
    }

    public List<Psvgresults> findPsvgresultsEntities(int maxResults, int firstResult) {
        return findPsvgresultsEntities(false, maxResults, firstResult);
    }

    private List<Psvgresults> findPsvgresultsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Psvgresults.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Psvgresults findPsvgresults(PsvgresultsPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Psvgresults.class, id);
        } finally {
            em.close();
        }
    }

    public int getPsvgresultsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Psvgresults> rt = cq.from(Psvgresults.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
