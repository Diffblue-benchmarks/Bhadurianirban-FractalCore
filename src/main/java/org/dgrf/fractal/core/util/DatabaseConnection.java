/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.core.util;

import java.util.HashMap;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author bhaduri
 */
public class DatabaseConnection {

    public static EntityManagerFactory EMF;

    public DatabaseConnection(String DB_USER, String DB_PASSWORD, String DB_CONN_URL) {
        HashMap<String, String> DBPROPERTIES = new HashMap<>();
        DBPROPERTIES.put("javax.persistence.jdbc.url", DB_CONN_URL);
        DBPROPERTIES.put("javax.persistence.jdbc.user", DB_USER);
        DBPROPERTIES.put("javax.persistence.jdbc.password", DB_PASSWORD);
        
        EMF = Persistence.createEntityManagerFactory("org.dgrf_FractalCore_jar_1.0-SNAPSHOTPU", DBPROPERTIES);
        
        //EMF = Persistence.createEntityManagerFactory("org.dgrf_FractalStudio_jar_1.0-SNAPSHOTPU");
    }
    public DatabaseConnection(String DB_USER, String DB_PASSWORD, String DB_CONN_URL, String PU) {
        HashMap<String, String> DBPROPERTIES = new HashMap<>();
        DBPROPERTIES.put("javax.persistence.jdbc.url", DB_CONN_URL);
        DBPROPERTIES.put("javax.persistence.jdbc.user", DB_USER);
        DBPROPERTIES.put("javax.persistence.jdbc.password", DB_PASSWORD);
        
        EMF = Persistence.createEntityManagerFactory(PU, DBPROPERTIES);
        
        //EMF = Persistence.createEntityManagerFactory("org.dgrf_FractalStudio_jar_1.0-SNAPSHOTPU");
    }    
}
