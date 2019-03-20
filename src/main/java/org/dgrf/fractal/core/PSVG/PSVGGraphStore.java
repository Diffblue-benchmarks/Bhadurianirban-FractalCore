/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.core.PSVG;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import org.dgrf.fractal.response.FractalResponseCode;
import org.dgrf.fractal.db.DAO.VgadjacencyDAO;
import org.dgrf.fractal.constants.FractalConstants;

/**
 *
 * @author bhaduri
 */
public class PSVGGraphStore {

    
    public static String psvgresultsslug;
    public static BufferedWriter writer;

    public PSVGGraphStore() {
        
    }

    public static int storeVisibilityGraphInDB(EntityManagerFactory emf) {
        VgadjacencyDAO vgadjacencyDAO = new VgadjacencyDAO(emf);
        String tempFile = FractalConstants.TEMP_FILE_PATH+psvgresultsslug;
        vgadjacencyDAO.loadVisibilityGraph(tempFile);
        return (FractalResponseCode.SUCCESS);
    }

    public static void createVisibilityGraphFile() {
        
        
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FractalConstants.TEMP_FILE_PATH+File.separator+psvgresultsslug)));
        } catch ( FileNotFoundException ex) {
            Logger.getLogger(PSVGGraphStore.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    public static void storeVisibilityGraphInFile (int node, int adjnode) {
        if (writer==null)
            return;
        String lineToWrite = psvgresultsslug +","+node+","+adjnode+"\n";
        try {
            writer.write(lineToWrite);
        } catch (IOException ex) {
            Logger.getLogger(PSVGGraphStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void closeVisibilityGraphFile() {
        try {
            writer.close();
            writer = null;
        } catch (IOException ex) {
            Logger.getLogger(PSVGGraphStore.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public static void delVisibilityGraphFile() {
        boolean success = (new File(FractalConstants.TEMP_FILE_PATH+psvgresultsslug)).delete();
        if (!success) {
            
            Logger.getLogger(PSVGGraphStore.class.getName()).log(Level.SEVERE,"File does not exists" );
            //should be replaced with exception
        }
    }

}
