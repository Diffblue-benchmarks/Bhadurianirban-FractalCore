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
public class PsvgresultsPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "psvgresultsslug")
    private String psvgresultsslug;
    @Basic(optional = false)
    @Column(name = "resultid")
    private int resultid;

    public PsvgresultsPK() {
    }

    public PsvgresultsPK(String psvgresultsslug, int resultid) {
        this.psvgresultsslug = psvgresultsslug;
        this.resultid = resultid;
    }

    public String getPsvgresultsslug() {
        return psvgresultsslug;
    }

    public void setPsvgresultsslug(String psvgresultsslug) {
        this.psvgresultsslug = psvgresultsslug;
    }

    public int getResultid() {
        return resultid;
    }

    public void setResultid(int resultid) {
        this.resultid = resultid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (psvgresultsslug != null ? psvgresultsslug.hashCode() : 0);
        hash += (int) resultid;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PsvgresultsPK)) {
            return false;
        }
        PsvgresultsPK other = (PsvgresultsPK) object;
        if ((this.psvgresultsslug == null && other.psvgresultsslug != null) || (this.psvgresultsslug != null && !this.psvgresultsslug.equals(other.psvgresultsslug))) {
            return false;
        }
        if (this.resultid != other.resultid) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.fractal.db.entities.PsvgresultsPK[ psvgresultsslug=" + psvgresultsslug + ", resultid=" + resultid + " ]";
    }
    
}
