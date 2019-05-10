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
import org.dgrf.fractal.db.entities.Vgadjacency;
import org.dgrf.fractal.db.entities.VgadjacencyPK;

/**
 *
 * @author dgrfi
 */
public class VgadjacencyJpaController implements Serializable {

    public VgadjacencyJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Vgadjacency vgadjacency) throws PreexistingEntityException, Exception {
        if (vgadjacency.getVgadjacencyPK() == null) {
            vgadjacency.setVgadjacencyPK(new VgadjacencyPK());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(vgadjacency);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findVgadjacency(vgadjacency.getVgadjacencyPK()) != null) {
                throw new PreexistingEntityException("Vgadjacency " + vgadjacency + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Vgadjacency vgadjacency) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            vgadjacency = em.merge(vgadjacency);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                VgadjacencyPK id = vgadjacency.getVgadjacencyPK();
                if (findVgadjacency(id) == null) {
                    throw new NonexistentEntityException("The vgadjacency with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(VgadjacencyPK id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Vgadjacency vgadjacency;
            try {
                vgadjacency = em.getReference(Vgadjacency.class, id);
                vgadjacency.getVgadjacencyPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The vgadjacency with id " + id + " no longer exists.", enfe);
            }
            em.remove(vgadjacency);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Vgadjacency> findVgadjacencyEntities() {
        return findVgadjacencyEntities(true, -1, -1);
    }

    public List<Vgadjacency> findVgadjacencyEntities(int maxResults, int firstResult) {
        return findVgadjacencyEntities(false, maxResults, firstResult);
    }

    private List<Vgadjacency> findVgadjacencyEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Vgadjacency.class));
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

    public Vgadjacency findVgadjacency(VgadjacencyPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Vgadjacency.class, id);
        } finally {
            em.close();
        }
    }

    public int getVgadjacencyCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Vgadjacency> rt = cq.from(Vgadjacency.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
