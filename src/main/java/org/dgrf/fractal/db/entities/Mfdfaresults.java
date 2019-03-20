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
 * @author bhaduri
 */
@Entity
@Table(name = "mfdfaresults")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Mfdfaresults.findAll", query = "SELECT m FROM Mfdfaresults m")})
public class Mfdfaresults implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected MfdfaresultsPK mfdfaresultsPK;
    @Basic(optional = false)
    @Column(name = "hq")
    private double hq;
    @Basic(optional = false)
    @Column(name = "dq")
    private double dq;

    public Mfdfaresults() {
    }

    public Mfdfaresults(MfdfaresultsPK mfdfaresultsPK) {
        this.mfdfaresultsPK = mfdfaresultsPK;
    }

    public Mfdfaresults(MfdfaresultsPK mfdfaresultsPK, double hq, double dq) {
        this.mfdfaresultsPK = mfdfaresultsPK;
        this.hq = hq;
        this.dq = dq;
    }

    public Mfdfaresults(String mfdfaresulsslug, int resultid) {
        this.mfdfaresultsPK = new MfdfaresultsPK(mfdfaresulsslug, resultid);
    }

    public MfdfaresultsPK getMfdfaresultsPK() {
        return mfdfaresultsPK;
    }

    public void setMfdfaresultsPK(MfdfaresultsPK mfdfaresultsPK) {
        this.mfdfaresultsPK = mfdfaresultsPK;
    }

    public double getHq() {
        return hq;
    }

    public void setHq(double hq) {
        this.hq = hq;
    }

    public double getDq() {
        return dq;
    }

    public void setDq(double dq) {
        this.dq = dq;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (mfdfaresultsPK != null ? mfdfaresultsPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Mfdfaresults)) {
            return false;
        }
        Mfdfaresults other = (Mfdfaresults) object;
        if ((this.mfdfaresultsPK == null && other.mfdfaresultsPK != null) || (this.mfdfaresultsPK != null && !this.mfdfaresultsPK.equals(other.mfdfaresultsPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.db.entities.Mfdfaresults[ mfdfaresultsPK=" + mfdfaresultsPK + " ]";
    }
    
}
