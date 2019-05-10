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
public class DataseriesPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "seriesid")
    private int seriesid;
    @Basic(optional = false)
    @Column(name = "dataindex")
    private long dataindex;

    public DataseriesPK() {
    }

    public DataseriesPK(int seriesid, long dataindex) {
        this.seriesid = seriesid;
        this.dataindex = dataindex;
    }

    public int getSeriesid() {
        return seriesid;
    }

    public void setSeriesid(int seriesid) {
        this.seriesid = seriesid;
    }

    public long getDataindex() {
        return dataindex;
    }

    public void setDataindex(long dataindex) {
        this.dataindex = dataindex;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) seriesid;
        hash += (int) dataindex;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DataseriesPK)) {
            return false;
        }
        DataseriesPK other = (DataseriesPK) object;
        if (this.seriesid != other.seriesid) {
            return false;
        }
        if (this.dataindex != other.dataindex) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.fractal.db.entities.DataseriesPK[ seriesid=" + seriesid + ", dataindex=" + dataindex + " ]";
    }
    
}
