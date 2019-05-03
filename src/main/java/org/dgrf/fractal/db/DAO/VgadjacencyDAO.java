/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.db.DAO;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import org.dgrf.fractal.response.FractalResponseCode;
import org.dgrf.fractal.db.JPA.VgadjacencyJpaController;

/**
 *
 * @author bhaduri
 */
public class VgadjacencyDAO extends VgadjacencyJpaController {

    public VgadjacencyDAO(EntityManagerFactory emf) {
        super(emf);
    }

    public int loadVisibilityGraph(String fileName) {
        int response;
        EntityManager em = getEntityManager();
        EntityTransaction entr = em.getTransaction();
        String nativeQueryString = "LOAD DATA LOCAL INFILE '" + fileName + "' INTO TABLE vgadjacency FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n'";
        
        Query query = em.createNativeQuery(nativeQueryString);
        
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

    public int deleteVisibilityGraph(String psvgResultsSlug) {
        int response;
        EntityManager em = getEntityManager();
        EntityTransaction entr = em.getTransaction();
        Query query = em.createNamedQuery("Vgadjacency.deleteById");
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
    
    public Map<Integer,Integer> getDegreesOfnodes() {
        
        String nativeQueryString = "select nodenum,sum(degree) as d from \n" +
                    "(select node as nodenum, count(adjnode) as degree from vgadjacency group by node union all\n" +
                    "select adjnode as nodenum, count(node) as degree from vgadjacency group by adjnode  order by nodenum ) a group by nodenum";
        EntityManager em = getEntityManager();
        
        Query query = em.createNativeQuery(nativeQueryString);
        
        List<Object[]> nodesAndDegrees = query.getResultList();
        Map<Integer,Integer> nodesAndDegreeMap = new HashMap<>();
        for (Object[] nodesAndDegree:nodesAndDegrees ) {
            
            
            Number nodeNumNum = (Number)nodesAndDegree[0];
            Number degreeOfNodeNum = (Number)nodesAndDegree[1];
            Integer nodeNum = nodeNumNum.intValue();
            Integer degreeOfNode = degreeOfNodeNum.intValue();
            nodesAndDegreeMap.put(nodeNum, degreeOfNode);
            
        }
        
        return nodesAndDegreeMap;
        
        
    }

    public Map<Integer,Integer> getNodeCountsforDegree() {
        
        String nativeQueryString = "select d as degreeval,count(nodenum) as nodeswithdegval from\n" +
"(select nodenum,sum(degree) as d from \n" +
"(select node as nodenum, count(adjnode) as degree from vgadjacency group by node union all\n" +
"select adjnode as nodenum, count(node) as degree from vgadjacency group by adjnode  order by nodenum ) a group by a.nodenum) b group by b.d order by b.d";
        EntityManager em = getEntityManager();
        
        Query query = em.createNativeQuery(nativeQueryString);
        
        List<Object[]> nodesAndDegrees = query.getResultList();
        Map<Integer,Integer> nodesAndDegreeMap = new HashMap<>();
        for (Object[] nodesAndDegree:nodesAndDegrees ) {
            
            
            Number nodeNumNum = (Number)nodesAndDegree[0];
            Number degreeOfNodeNum = (Number)nodesAndDegree[1];
            Integer nodeNum = nodeNumNum.intValue();
            Integer degreeOfNode = degreeOfNodeNum.intValue();
            nodesAndDegreeMap.put(nodeNum, degreeOfNode);
            
        }
        
        return nodesAndDegreeMap;
        
        
    }    
}
