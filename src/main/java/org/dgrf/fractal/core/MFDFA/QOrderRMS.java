package org.dgrf.fractal.core.MFDFA;

public class QOrderRMS {
	private int sliceSize;
	Double[] FqValues;
	public Double[] getFqValues () {
		return this.FqValues;
	}
	public void setFqValues(Double[] FqValues) {
		
		this.FqValues = new Double[FqValues.length];
		for (int i=0;i<FqValues.length;i++) {
			this.FqValues[i] = FqValues[i];
			
		}
		
	}
	public int getSliceSize () {
		return this.sliceSize;
	}
	public void setSliceSize (int sliceSize) {
		this.sliceSize = sliceSize;
	}

}
