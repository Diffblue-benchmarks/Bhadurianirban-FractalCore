/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.db.entities;

import java.io.Serializable;
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
@Table(name = "psvgresults")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Psvgresults.findAll", query = "SELECT p FROM Psvgresults p")})
public class Psvgresults implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected PsvgresultsPK psvgresultsPK;
    @Column(name = "degreeval")
    private Integer degreeval;
    @Column(name = "nodeswithdegval")
    private Integer nodeswithdegval;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "probofdegreeval")
    private Double probofdegreeval;
    @Column(name = "logofdegreeval")
    private Double logofdegreeval;
    @Column(name = "logofprobofdegreeval")
    private Double logofprobofdegreeval;
    @Column(name = "required")
    private Short required;

    public Psvgresults() {
    }

    public Psvgresults(PsvgresultsPK psvgresultsPK) {
        this.psvgresultsPK = psvgresultsPK;
    }

    public Psvgresults(String psvgresultsslug, int resultid) {
        this.psvgresultsPK = new PsvgresultsPK(psvgresultsslug, resultid);
    }

    public PsvgresultsPK getPsvgresultsPK() {
        return psvgresultsPK;
    }

    public void setPsvgresultsPK(PsvgresultsPK psvgresultsPK) {
        this.psvgresultsPK = psvgresultsPK;
    }

    public Integer getDegreeval() {
        return degreeval;
    }

    public void setDegreeval(Integer degreeval) {
        this.degreeval = degreeval;
    }

    public Integer getNodeswithdegval() {
        return nodeswithdegval;
    }

    public void setNodeswithdegval(Integer nodeswithdegval) {
        this.nodeswithdegval = nodeswithdegval;
    }

    public Double getProbofdegreeval() {
        return probofdegreeval;
    }

    public void setProbofdegreeval(Double probofdegreeval) {
        this.probofdegreeval = probofdegreeval;
    }

    public Double getLogofdegreeval() {
        return logofdegreeval;
    }

    public void setLogofdegreeval(Double logofdegreeval) {
        this.logofdegreeval = logofdegreeval;
    }

    public Double getLogofprobofdegreeval() {
        return logofprobofdegreeval;
    }

    public void setLogofprobofdegreeval(Double logofprobofdegreeval) {
        this.logofprobofdegreeval = logofprobofdegreeval;
    }

    public Short getRequired() {
        return required;
    }

    public void setRequired(Short required) {
        this.required = required;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (psvgresultsPK != null ? psvgresultsPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Psvgresults)) {
            return false;
        }
        Psvgresults other = (Psvgresults) object;
        if ((this.psvgresultsPK == null && other.psvgresultsPK != null) || (this.psvgresultsPK != null && !this.psvgresultsPK.equals(other.psvgresultsPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.fractal.db.entities.Psvgresults[ psvgresultsPK=" + psvgresultsPK + " ]";
    }
    
}
