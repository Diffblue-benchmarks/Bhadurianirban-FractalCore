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
import org.dgrf.fractal.db.entities.Mfdfaresults;
import org.dgrf.fractal.db.entities.MfdfaresultsPK;

/**
 *
 * @author bhaduri
 */
public class MfdfaresultsJpaController implements Serializable {

    public MfdfaresultsJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Mfdfaresults mfdfaresults) throws PreexistingEntityException, Exception {
        if (mfdfaresults.getMfdfaresultsPK() == null) {
            mfdfaresults.setMfdfaresultsPK(new MfdfaresultsPK());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(mfdfaresults);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMfdfaresults(mfdfaresults.getMfdfaresultsPK()) != null) {
                throw new PreexistingEntityException("Mfdfaresults " + mfdfaresults + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Mfdfaresults mfdfaresults) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            mfdfaresults = em.merge(mfdfaresults);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                MfdfaresultsPK id = mfdfaresults.getMfdfaresultsPK();
                if (findMfdfaresults(id) == null) {
                    throw new NonexistentEntityException("The mfdfaresults with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(MfdfaresultsPK id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Mfdfaresults mfdfaresults;
            try {
                mfdfaresults = em.getReference(Mfdfaresults.class, id);
                mfdfaresults.getMfdfaresultsPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The mfdfaresults with id " + id + " no longer exists.", enfe);
            }
            em.remove(mfdfaresults);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Mfdfaresults> findMfdfaresultsEntities() {
        return findMfdfaresultsEntities(true, -1, -1);
    }

    public List<Mfdfaresults> findMfdfaresultsEntities(int maxResults, int firstResult) {
        return findMfdfaresultsEntities(false, maxResults, firstResult);
    }

    private List<Mfdfaresults> findMfdfaresultsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Mfdfaresults.class));
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

    public Mfdfaresults findMfdfaresults(MfdfaresultsPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Mfdfaresults.class, id);
        } finally {
            em.close();
        }
    }

    public int getMfdfaresultsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Mfdfaresults> rt = cq.from(Mfdfaresults.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
