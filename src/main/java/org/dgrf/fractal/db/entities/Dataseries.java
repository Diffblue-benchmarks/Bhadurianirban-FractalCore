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
@Table(name = "dataseries")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Dataseries.findAll", query = "SELECT d FROM Dataseries d")})
public class Dataseries implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected DataseriesPK dataseriesPK;
    @Basic(optional = false)
    @Column(name = "xvalue")
    private double xvalue;
    @Basic(optional = false)
    @Column(name = "yvalue")
    private double yvalue;
    @Basic(optional = false)
    @Column(name = "xvaluePos")
    private double xvaluePos;
    @Basic(optional = false)
    @Column(name = "yvaluePos")
    private double yvaluePos;
    @Basic(optional = false)
    @Column(name = "ycumulative")
    private double ycumulative;

    public Dataseries() {
    }

    public Dataseries(DataseriesPK dataseriesPK) {
        this.dataseriesPK = dataseriesPK;
    }

    public Dataseries(DataseriesPK dataseriesPK, double xvalue, double yvalue, double xvaluePos, double yvaluePos, double ycumulative) {
        this.dataseriesPK = dataseriesPK;
        this.xvalue = xvalue;
        this.yvalue = yvalue;
        this.xvaluePos = xvaluePos;
        this.yvaluePos = yvaluePos;
        this.ycumulative = ycumulative;
    }

    public Dataseries(int seriesid, long dataindex) {
        this.dataseriesPK = new DataseriesPK(seriesid, dataindex);
    }

    public DataseriesPK getDataseriesPK() {
        return dataseriesPK;
    }

    public void setDataseriesPK(DataseriesPK dataseriesPK) {
        this.dataseriesPK = dataseriesPK;
    }

    public double getXvalue() {
        return xvalue;
    }

    public void setXvalue(double xvalue) {
        this.xvalue = xvalue;
    }

    public double getYvalue() {
        return yvalue;
    }

    public void setYvalue(double yvalue) {
        this.yvalue = yvalue;
    }

    public double getXvaluePos() {
        return xvaluePos;
    }

    public void setXvaluePos(double xvaluePos) {
        this.xvaluePos = xvaluePos;
    }

    public double getYvaluePos() {
        return yvaluePos;
    }

    public void setYvaluePos(double yvaluePos) {
        this.yvaluePos = yvaluePos;
    }

    public double getYcumulative() {
        return ycumulative;
    }

    public void setYcumulative(double ycumulative) {
        this.ycumulative = ycumulative;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (dataseriesPK != null ? dataseriesPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Dataseries)) {
            return false;
        }
        Dataseries other = (Dataseries) object;
        if ((this.dataseriesPK == null && other.dataseriesPK != null) || (this.dataseriesPK != null && !this.dataseriesPK.equals(other.dataseriesPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.db.entities.Dataseries[ dataseriesPK=" + dataseriesPK + " ]";
    }
    
}
