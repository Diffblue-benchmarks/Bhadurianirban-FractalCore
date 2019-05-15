/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.db.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author dgrfi
 */
@Embeddable
public class EdgelistPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "graphslug")
    private String graphslug;
    @Basic(optional = false)
    @Column(name = "node")
    private int node;
    @Basic(optional = false)
    @Column(name = "adjnode")
    private int adjnode;

    public EdgelistPK() {
    }

    public EdgelistPK(String graphslug, int node, int adjnode) {
        this.graphslug = graphslug;
        this.node = node;
        this.adjnode = adjnode;
    }

    public String getGraphslug() {
        return graphslug;
    }

    public void setGraphslug(String graphslug) {
        this.graphslug = graphslug;
    }

    public int getNode() {
        return node;
    }

    public void setNode(int node) {
        this.node = node;
    }

    public int getAdjnode() {
        return adjnode;
    }

    public void setAdjnode(int adjnode) {
        this.adjnode = adjnode;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (graphslug != null ? graphslug.hashCode() : 0);
        hash += (int) node;
        hash += (int) adjnode;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof EdgelistPK)) {
            return false;
        }
        EdgelistPK other = (EdgelistPK) object;
        if ((this.graphslug == null && other.graphslug != null) || (this.graphslug != null && !this.graphslug.equals(other.graphslug))) {
            return false;
        }
        if (this.node != other.node) {
            return false;
        }
        if (this.adjnode != other.adjnode) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.fractal.db.entities.EdgelistPK[ graphslug=" + graphslug + ", node=" + node + ", adjnode=" + adjnode + " ]";
    }
    
}
