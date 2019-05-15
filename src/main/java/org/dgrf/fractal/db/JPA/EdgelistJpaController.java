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
import org.dgrf.fractal.db.entities.Edgelist;
import org.dgrf.fractal.db.entities.EdgelistPK;

/**
 *
 * @author dgrfi
 */
public class EdgelistJpaController implements Serializable {

    public EdgelistJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Edgelist edgelist) throws PreexistingEntityException, Exception {
        if (edgelist.getEdgelistPK() == null) {
            edgelist.setEdgelistPK(new EdgelistPK());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(edgelist);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findEdgelist(edgelist.getEdgelistPK()) != null) {
                throw new PreexistingEntityException("Edgelist " + edgelist + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Edgelist edgelist) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            edgelist = em.merge(edgelist);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                EdgelistPK id = edgelist.getEdgelistPK();
                if (findEdgelist(id) == null) {
                    throw new NonexistentEntityException("The edgelist with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(EdgelistPK id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Edgelist edgelist;
            try {
                edgelist = em.getReference(Edgelist.class, id);
                edgelist.getEdgelistPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The edgelist with id " + id + " no longer exists.", enfe);
            }
            em.remove(edgelist);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Edgelist> findEdgelistEntities() {
        return findEdgelistEntities(true, -1, -1);
    }

    public List<Edgelist> findEdgelistEntities(int maxResults, int firstResult) {
        return findEdgelistEntities(false, maxResults, firstResult);
    }

    private List<Edgelist> findEdgelistEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Edgelist.class));
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

    public Edgelist findEdgelist(EdgelistPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Edgelist.class, id);
        } finally {
            em.close();
        }
    }

    public int getEdgelistCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Edgelist> rt = cq.from(Edgelist.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
