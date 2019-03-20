/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.db.entities;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author bhaduri
 */
@Entity
@Table(name = "vgadjacency")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Vgadjacency.findAll", query = "SELECT v FROM Vgadjacency v")})
public class Vgadjacency implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected VgadjacencyPK vgadjacencyPK;

    public Vgadjacency() {
    }

    public Vgadjacency(VgadjacencyPK vgadjacencyPK) {
        this.vgadjacencyPK = vgadjacencyPK;
    }

    public Vgadjacency(String psvgresultsslug, int node, int adjnode) {
        this.vgadjacencyPK = new VgadjacencyPK(psvgresultsslug, node, adjnode);
    }

    public VgadjacencyPK getVgadjacencyPK() {
        return vgadjacencyPK;
    }

    public void setVgadjacencyPK(VgadjacencyPK vgadjacencyPK) {
        this.vgadjacencyPK = vgadjacencyPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (vgadjacencyPK != null ? vgadjacencyPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Vgadjacency)) {
            return false;
        }
        Vgadjacency other = (Vgadjacency) object;
        if ((this.vgadjacencyPK == null && other.vgadjacencyPK != null) || (this.vgadjacencyPK != null && !this.vgadjacencyPK.equals(other.vgadjacencyPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.db.entities.Vgadjacency[ vgadjacencyPK=" + vgadjacencyPK + " ]";
    }
    
}
