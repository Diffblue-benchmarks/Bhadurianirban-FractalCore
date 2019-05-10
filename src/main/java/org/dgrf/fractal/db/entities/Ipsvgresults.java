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
@Table(name = "ipsvgresults")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Ipsvgresults.findAll", query = "SELECT i FROM Ipsvgresults i")})
public class Ipsvgresults implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected IpsvgresultsPK ipsvgresultsPK;
    @Basic(optional = false)
    @Column(name = "lengthofgaps")
    private int lengthofgaps;
    @Basic(optional = false)
    @Column(name = "psvgforgaps")
    private double psvgforgaps;

    public Ipsvgresults() {
    }

    public Ipsvgresults(IpsvgresultsPK ipsvgresultsPK) {
        this.ipsvgresultsPK = ipsvgresultsPK;
    }

    public Ipsvgresults(IpsvgresultsPK ipsvgresultsPK, int lengthofgaps, double psvgforgaps) {
        this.ipsvgresultsPK = ipsvgresultsPK;
        this.lengthofgaps = lengthofgaps;
        this.psvgforgaps = psvgforgaps;
    }

    public Ipsvgresults(String ipsvgresultsslug, int resultid) {
        this.ipsvgresultsPK = new IpsvgresultsPK(ipsvgresultsslug, resultid);
    }

    public IpsvgresultsPK getIpsvgresultsPK() {
        return ipsvgresultsPK;
    }

    public void setIpsvgresultsPK(IpsvgresultsPK ipsvgresultsPK) {
        this.ipsvgresultsPK = ipsvgresultsPK;
    }

    public int getLengthofgaps() {
        return lengthofgaps;
    }

    public void setLengthofgaps(int lengthofgaps) {
        this.lengthofgaps = lengthofgaps;
    }

    public double getPsvgforgaps() {
        return psvgforgaps;
    }

    public void setPsvgforgaps(double psvgforgaps) {
        this.psvgforgaps = psvgforgaps;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (ipsvgresultsPK != null ? ipsvgresultsPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Ipsvgresults)) {
            return false;
        }
        Ipsvgresults other = (Ipsvgresults) object;
        if ((this.ipsvgresultsPK == null && other.ipsvgresultsPK != null) || (this.ipsvgresultsPK != null && !this.ipsvgresultsPK.equals(other.ipsvgresultsPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.fractal.db.entities.Ipsvgresults[ ipsvgresultsPK=" + ipsvgresultsPK + " ]";
    }
    
}
