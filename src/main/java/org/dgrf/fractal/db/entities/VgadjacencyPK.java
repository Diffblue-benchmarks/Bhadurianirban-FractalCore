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
 * @author bhaduri
 */
@Embeddable
public class VgadjacencyPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "psvgresultsslug")
    private String psvgresultsslug;
    @Basic(optional = false)
    @Column(name = "node")
    private int node;
    @Basic(optional = false)
    @Column(name = "adjnode")
    private int adjnode;

    public VgadjacencyPK() {
    }

    public VgadjacencyPK(String psvgresultsslug, int node, int adjnode) {
        this.psvgresultsslug = psvgresultsslug;
        this.node = node;
        this.adjnode = adjnode;
    }

    public String getPsvgresultsslug() {
        return psvgresultsslug;
    }

    public void setPsvgresultsslug(String psvgresultsslug) {
        this.psvgresultsslug = psvgresultsslug;
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
        hash += (psvgresultsslug != null ? psvgresultsslug.hashCode() : 0);
        hash += (int) node;
        hash += (int) adjnode;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof VgadjacencyPK)) {
            return false;
        }
        VgadjacencyPK other = (VgadjacencyPK) object;
        if ((this.psvgresultsslug == null && other.psvgresultsslug != null) || (this.psvgresultsslug != null && !this.psvgresultsslug.equals(other.psvgresultsslug))) {
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
        return "org.dgrf.db.entities.VgadjacencyPK[ psvgresultsslug=" + psvgresultsslug + ", node=" + node + ", adjnode=" + adjnode + " ]";
    }
    
}
