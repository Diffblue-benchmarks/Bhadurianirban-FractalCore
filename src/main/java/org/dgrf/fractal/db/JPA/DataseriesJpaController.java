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
import org.dgrf.fractal.db.entities.Dataseries;
import org.dgrf.fractal.db.entities.DataseriesPK;

/**
 *
 * @author dgrfi
 */
public class DataseriesJpaController implements Serializable {

    public DataseriesJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Dataseries dataseries) throws PreexistingEntityException, Exception {
        if (dataseries.getDataseriesPK() == null) {
            dataseries.setDataseriesPK(new DataseriesPK());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(dataseries);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findDataseries(dataseries.getDataseriesPK()) != null) {
                throw new PreexistingEntityException("Dataseries " + dataseries + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Dataseries dataseries) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            dataseries = em.merge(dataseries);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                DataseriesPK id = dataseries.getDataseriesPK();
                if (findDataseries(id) == null) {
                    throw new NonexistentEntityException("The dataseries with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(DataseriesPK id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Dataseries dataseries;
            try {
                dataseries = em.getReference(Dataseries.class, id);
                dataseries.getDataseriesPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The dataseries with id " + id + " no longer exists.", enfe);
            }
            em.remove(dataseries);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Dataseries> findDataseriesEntities() {
        return findDataseriesEntities(true, -1, -1);
    }

    public List<Dataseries> findDataseriesEntities(int maxResults, int firstResult) {
        return findDataseriesEntities(false, maxResults, firstResult);
    }

    private List<Dataseries> findDataseriesEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Dataseries.class));
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

    public Dataseries findDataseries(DataseriesPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Dataseries.class, id);
        } finally {
            em.close();
        }
    }

    public int getDataseriesCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Dataseries> rt = cq.from(Dataseries.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
