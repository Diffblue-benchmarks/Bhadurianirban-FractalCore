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
public class IpsvgresultsPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "ipsvgresultsslug")
    private String ipsvgresultsslug;
    @Basic(optional = false)
    @Column(name = "resultid")
    private int resultid;

    public IpsvgresultsPK() {
    }

    public IpsvgresultsPK(String ipsvgresultsslug, int resultid) {
        this.ipsvgresultsslug = ipsvgresultsslug;
        this.resultid = resultid;
    }

    public String getIpsvgresultsslug() {
        return ipsvgresultsslug;
    }

    public void setIpsvgresultsslug(String ipsvgresultsslug) {
        this.ipsvgresultsslug = ipsvgresultsslug;
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
        hash += (ipsvgresultsslug != null ? ipsvgresultsslug.hashCode() : 0);
        hash += (int) resultid;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IpsvgresultsPK)) {
            return false;
        }
        IpsvgresultsPK other = (IpsvgresultsPK) object;
        if ((this.ipsvgresultsslug == null && other.ipsvgresultsslug != null) || (this.ipsvgresultsslug != null && !this.ipsvgresultsslug.equals(other.ipsvgresultsslug))) {
            return false;
        }
        if (this.resultid != other.resultid) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.fractal.db.entities.IpsvgresultsPK[ ipsvgresultsslug=" + ipsvgresultsslug + ", resultid=" + resultid + " ]";
    }
    
}
