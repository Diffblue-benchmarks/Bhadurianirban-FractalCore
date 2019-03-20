package org.dgrf.fractal.core.MFDFA;

import org.dgrf.fractal.core.util.LogUtil;



public class secondOrderRMS {
	private int sliceSize;
	private Double RMSValue;
	private Double logOfSliceSize;
	private Double logOfSliceSecondOrderRMS;
	public void setSliceSize (int sliceSize) {
		this.sliceSize = sliceSize;
		this.logOfSliceSize = LogUtil.logBaseK(sliceSize);
	}
	public void setRMSValue (Double RMSValue) {
		this.RMSValue = RMSValue;
		this.logOfSliceSecondOrderRMS = LogUtil.logBaseK(RMSValue);
	}
	public int getSliceSize () {
		return this.sliceSize;
	}
	public Double getRMSValue () {
		return this.RMSValue;
	}
	/**
	 * @return the logOfSliceSize
	 */
	public Double getLogOfSliceSize() {
		return logOfSliceSize;
	}
	/**
	 * @return the logOfSliceSecondOrderRMS
	 */
	public Double getLogOfSliceSecondOrderRMS() {
		return logOfSliceSecondOrderRMS;
	}
	
}
