/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.db.DAO;

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.dgrf.fractal.response.FractalResponseCode;

import org.dgrf.fractal.db.JPA.DataseriesJpaController;
import org.dgrf.fractal.db.entities.Dataseries;
import org.dgrf.fractal.core.PSVG.XYData;

/**
 *
 * @author bhaduri
 */
public class DataSeriesDAO extends DataseriesJpaController {

    public DataSeriesDAO(EntityManagerFactory emf) {
        super(emf);
    }

    public int deleteDataSeriesById(int seriesId) {
        int response;
        EntityManager em = getEntityManager();
        EntityTransaction entr = em.getTransaction();
        Query query = em.createNamedQuery("Dataseries.deleteSeriesByID");
        query.setParameter("seriesid", seriesId);
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

    public List<Dataseries> getDataSeriesById(int seriesId) {
        EntityManager em = getEntityManager();
        TypedQuery<Dataseries> query = em.createNamedQuery("Dataseries.dataSeriesByID", Dataseries.class);
        query.setParameter("seriesid", seriesId);
        List<Dataseries> dataseriesList = query.getResultList();
        return dataseriesList;
    }

    public List<Dataseries> getDataSeriesById(int seriesId, int maxResults) {
        EntityManager em = getEntityManager();
        TypedQuery<Dataseries> query = em.createNamedQuery("Dataseries.dataSeriesByID", Dataseries.class);
        query.setMaxResults(maxResults);
        query.setParameter("seriesid", seriesId);
        List<Dataseries> dataseriesList = query.getResultList();
        return dataseriesList;
    }

    public List<Double> getDataSeriesYvalPosById(int seriesId) {
        
        List<Double> yValPosSeries = getDataSeriesById(seriesId).stream().map(ds->ds.getYvaluePos()).collect(Collectors.toList());
        return yValPosSeries;
    }
    public List<XYData> getDataSeriesXYPosById(int seriesId) {
        
        List<XYData> xyDataSeries = getDataSeriesById(seriesId).stream().map(ds->new XYData(ds.getXvaluePos(),ds.getYvaluePos())).collect(Collectors.toList());
        return xyDataSeries;
    }
    public Double getMinYValueById(int seriesId) {
        EntityManager em = getEntityManager();
        TypedQuery<Double> query = em.createNamedQuery("Dataseries.minY", Double.class);
        query.setParameter("seriesid", seriesId);
        Double minYValue = query.getSingleResult();
        return minYValue;
    }

    public Double getMinXValueById(int seriesId) {
        EntityManager em = getEntityManager();
        TypedQuery<Double> query = em.createNamedQuery("Dataseries.minX", Double.class);
        query.setParameter("seriesid", seriesId);
        Double minYValue = query.getSingleResult();
        return minYValue;
    }

    public Double getMeanYValueById(int seriesId) {
        EntityManager em = getEntityManager();
        TypedQuery<Double> query = em.createNamedQuery("Dataseries.findMean", Double.class);
        query.setParameter("seriesid", seriesId);
        Double meannYValue = query.getSingleResult();
        return meannYValue;
    }

    public int subtractYMean(int seriesId, Double meannYValue) {
        int response;
        EntityManager em = getEntityManager();
        EntityTransaction entr = em.getTransaction();
        Query query = em.createNamedQuery("Dataseries.subtractYMean");
        query.setParameter("seriesid", seriesId);
        query.setParameter("meanY", meannYValue);
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

    public int cumulateY(int seriesId) {
        int response;
        EntityManager em = getEntityManager();
        EntityTransaction entr = em.getTransaction();
        List<Dataseries> dataseriesList = getDataSeriesById(seriesId);
        Double ycumulative = dataseriesList.get(0).getYcumulative();
        entr.begin();
        for (int counter = 1; counter < dataseriesList.size(); counter++) {

            ycumulative = ycumulative + dataseriesList.get(counter).getYcumulative();
            Query query = em.createNamedQuery("Dataseries.cumulateYMean");
            query.setParameter("seriesid", seriesId);
            query.setParameter("cumulateY", ycumulative);
            query.setParameter("dataindex", dataseriesList.get(counter).getDataseriesPK().getDataindex());
            int executeUpdate = query.executeUpdate();
            if (executeUpdate == 0) {
                response = FractalResponseCode.DB_NON_EXISTING;
                return response;
            }

        }
        entr.commit();
        
        response = FractalResponseCode.SUCCESS;
        return response;
    }

    public Long getDataSeriesDataCount(int seriesId) {
        EntityManager em = getEntityManager();
        TypedQuery<Long> query = em.createNamedQuery("Dataseries.count", Long.class);
        query.setParameter("seriesid", seriesId);
        Long dataCount = query.getSingleResult();
        return dataCount;
    }

    public int loadDataSeries(String fileName) {
        
        EntityManager em = getEntityManager();
        EntityTransaction entr = em.getTransaction();
        String nativeQueryString = "LOAD DATA LOCAL INFILE '" + fileName + "' INTO TABLE dataseries FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\r\\n'";
        Query query = em.createNativeQuery(nativeQueryString);
        //query.setParameter("fileName", fileName);
        entr.begin();
        int executeUpdate = query.executeUpdate();
        
        
        entr.commit();
        return executeUpdate;
    }

    public int updateXYPos(int seriesId, Double minXValue, Double minYValue) {
        int response;
        EntityManager em = getEntityManager();
        EntityTransaction entr = em.getTransaction();
        Query query = em.createNamedQuery("Dataseries.updatePosY");
        query.setParameter("seriesid", seriesId);
        query.setParameter("minX", minXValue);
        query.setParameter("minY", minYValue);
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
}
