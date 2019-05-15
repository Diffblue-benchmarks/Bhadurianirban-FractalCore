/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.db.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author dgrfi
 */
@Entity
@Table(name = "edgelist")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Edgelist.findAll", query = "SELECT e FROM Edgelist e")})
public class Edgelist implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected EdgelistPK edgelistPK;
    @Basic(optional = false)
    @Column(name = "edgelength")
    private double edgelength;

    public Edgelist() {
    }

    public Edgelist(EdgelistPK edgelistPK) {
        this.edgelistPK = edgelistPK;
    }

    public Edgelist(EdgelistPK edgelistPK, double edgelength) {
        this.edgelistPK = edgelistPK;
        this.edgelength = edgelength;
    }

    public Edgelist(String graphslug, int node, int adjnode) {
        this.edgelistPK = new EdgelistPK(graphslug, node, adjnode);
    }

    public EdgelistPK getEdgelistPK() {
        return edgelistPK;
    }

    public void setEdgelistPK(EdgelistPK edgelistPK) {
        this.edgelistPK = edgelistPK;
    }

    public double getEdgelength() {
        return edgelength;
    }

    public void setEdgelength(double edgelength) {
        this.edgelength = edgelength;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (edgelistPK != null ? edgelistPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Edgelist)) {
            return false;
        }
        Edgelist other = (Edgelist) object;
        if ((this.edgelistPK == null && other.edgelistPK != null) || (this.edgelistPK != null && !this.edgelistPK.equals(other.edgelistPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.fractal.db.entities.Edgelist[ edgelistPK=" + edgelistPK + " ]";
    }
    
}
