package org.dgrf.fractal.core.MFDFA;

import org.dgrf.fractal.core.util.LinSpace;


/**
 * 
 * @author Anirban
 *
 */
public class CalculateDq {
	private Double[] hq;
	private Double[] tq;
	private Double[] Dq;
	private LinSpace qLinSpace;
	CalculateDq (Double[] hq,Double[] tq,LinSpace qLinSpace) {
		this.hq = hq;
		this.tq =tq;
		this.qLinSpace = qLinSpace;
		createDq();
	}
	public void createDq () {
		Double qValue;
		Double hqValue;
		Double tqValue;
		Dq = new Double[(qLinSpace.getTotalCount()-1)];
		for (int i=0;i<(qLinSpace.getTotalCount()-1);i++) {
			qValue = this.qLinSpace.getLinSpaceElement(i);
			hqValue = this.hq[i];
			tqValue = this.tq[i];
			Dq[i] = (qValue*hqValue)-tqValue;
		}
		
	}
	public Double[] getDq() {
		return this.Dq;
	}
	

}
