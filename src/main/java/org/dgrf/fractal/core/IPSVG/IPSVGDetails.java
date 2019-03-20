/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.core.IPSVG;

/**
 *
 * @author dgrfv
 */
public class IPSVGDetails {
    private int lengthOfGaps;
    private Double PSVGforGap;

    public IPSVGDetails(int lengthOfGaps, Double PSVGforGap) {
        this.lengthOfGaps = lengthOfGaps;
        this.PSVGforGap = PSVGforGap;
    }

    
    public int getLengthOfGaps() {
        return lengthOfGaps;
    }

    public void setLengthOfGaps(int lengthOfGaps) {
        this.lengthOfGaps = lengthOfGaps;
    }

    public Double getPSVGforGap() {
        return PSVGforGap;
    }

    public void setPSVGforGap(Double PSVGforGap) {
        this.PSVGforGap = PSVGforGap;
    }
}
