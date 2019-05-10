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
import org.dgrf.fractal.db.entities.Ipsvgresults;
import org.dgrf.fractal.db.entities.IpsvgresultsPK;

/**
 *
 * @author dgrfi
 */
public class IpsvgresultsJpaController implements Serializable {

    public IpsvgresultsJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Ipsvgresults ipsvgresults) throws PreexistingEntityException, Exception {
        if (ipsvgresults.getIpsvgresultsPK() == null) {
            ipsvgresults.setIpsvgresultsPK(new IpsvgresultsPK());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(ipsvgresults);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findIpsvgresults(ipsvgresults.getIpsvgresultsPK()) != null) {
                throw new PreexistingEntityException("Ipsvgresults " + ipsvgresults + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Ipsvgresults ipsvgresults) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            ipsvgresults = em.merge(ipsvgresults);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                IpsvgresultsPK id = ipsvgresults.getIpsvgresultsPK();
                if (findIpsvgresults(id) == null) {
                    throw new NonexistentEntityException("The ipsvgresults with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(IpsvgresultsPK id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Ipsvgresults ipsvgresults;
            try {
                ipsvgresults = em.getReference(Ipsvgresults.class, id);
                ipsvgresults.getIpsvgresultsPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The ipsvgresults with id " + id + " no longer exists.", enfe);
            }
            em.remove(ipsvgresults);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Ipsvgresults> findIpsvgresultsEntities() {
        return findIpsvgresultsEntities(true, -1, -1);
    }

    public List<Ipsvgresults> findIpsvgresultsEntities(int maxResults, int firstResult) {
        return findIpsvgresultsEntities(false, maxResults, firstResult);
    }

    private List<Ipsvgresults> findIpsvgresultsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Ipsvgresults.class));
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

    public Ipsvgresults findIpsvgresults(IpsvgresultsPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Ipsvgresults.class, id);
        } finally {
            em.close();
        }
    }

    public int getIpsvgresultsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Ipsvgresults> rt = cq.from(Ipsvgresults.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
