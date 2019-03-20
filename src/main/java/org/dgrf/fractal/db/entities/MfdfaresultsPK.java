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
public class MfdfaresultsPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "mfdfaresulsslug")
    private String mfdfaresulsslug;
    @Basic(optional = false)
    @Column(name = "resultid")
    private int resultid;

    public MfdfaresultsPK() {
    }

    public MfdfaresultsPK(String mfdfaresulsslug, int resultid) {
        this.mfdfaresulsslug = mfdfaresulsslug;
        this.resultid = resultid;
    }

    public String getMfdfaresulsslug() {
        return mfdfaresulsslug;
    }

    public void setMfdfaresulsslug(String mfdfaresulsslug) {
        this.mfdfaresulsslug = mfdfaresulsslug;
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
        hash += (mfdfaresulsslug != null ? mfdfaresulsslug.hashCode() : 0);
        hash += (int) resultid;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MfdfaresultsPK)) {
            return false;
        }
        MfdfaresultsPK other = (MfdfaresultsPK) object;
        if ((this.mfdfaresulsslug == null && other.mfdfaresulsslug != null) || (this.mfdfaresulsslug != null && !this.mfdfaresulsslug.equals(other.mfdfaresulsslug))) {
            return false;
        }
        if (this.resultid != other.resultid) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.db.entities.MfdfaresultsPK[ mfdfaresulsslug=" + mfdfaresulsslug + ", resultid=" + resultid + " ]";
    }
    
}
