/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.core.PSVG;

/**
 *
 * @author bhaduri
 */
public class XYData {
    private Double xValue;
    private Double yValue;

    public XYData(Double xValue, Double yValue) {
        this.xValue = xValue;
        this.yValue = yValue;
    }
    
    public Double getxValue() {
        return xValue;
    }

    public void setxValue(Double xValue) {
        this.xValue = xValue;
    }

    public Double getyValue() {
        return yValue;
    }

    public void setyValue(Double yValue) {
        this.yValue = yValue;
    }
    
    
}
