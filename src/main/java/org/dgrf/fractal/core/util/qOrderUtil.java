package org.dgrf.fractal.core.util;


public class qOrderUtil {

	public static Double calcqthRoot (Double x,Double q) {
		Double qOrderRoot = Math.pow(x, (1/q));
		return qOrderRoot;
	}
	public static Double calcqPower (Double x,Double q) {
		Double qPower = Math.pow(x, q);
		return qPower;
	}
	public static Double calcqRMS (Double[] RMS,Double q) {
		Double qRMS=0.0;
		if (q==0) {
			Double[] qPoweredRMS = new Double[RMS.length];
			for (int i=0;i<RMS.length;i++) {
				if (RMS[i] == 0) {
					
					qPoweredRMS[i] =0.0;
				}
				else {
					qPoweredRMS[i] = Math.log(Math.pow(RMS[i], 2));
				}
				
			}
			Double meanQPoweredRMS = calcMean(qPoweredRMS);
			qRMS = Math.exp(0.5*meanQPoweredRMS);
		}
		else {
			Double[] qPoweredRMS = new Double[RMS.length];
			for (int i=0;i<RMS.length;i++) {
				if (RMS[i] == 0) {
					qPoweredRMS[i] =0.0;
				}
				else {
					qPoweredRMS[i] = calcqPower(RMS[i], q);
				}
				
			}
			Double meanQPoweredRMS = calcMean(qPoweredRMS);
			qRMS = calcqthRoot(meanQPoweredRMS, q);		
		}

		return qRMS;
	}
	/*public Double calcMean (Double[] series) {
		BigDecimal aggregate= new BigDecimal(0);
		BigDecimal seriesVal;
		for (int i=0;i<series.length;i++) {
			seriesVal = new BigDecimal(series[i]);
			aggregate = aggregate.add(seriesVal);
		}
		BigDecimal mean = new BigDecimal(0); 
		BigDecimal lengthOfSer = new BigDecimal(series.length);
		mean = 	aggregate.divide(lengthOfSer,16,RoundingMode.HALF_UP);
		return mean.doubleValue();
	}*/
	public static Double calcMean (Double[] series) {
		Double aggregate= 0.0;
		
		for (int i=0;i<series.length;i++) {
		
			aggregate = aggregate+series[i];
		}
		
		Double mean = 	aggregate/series.length;
		return mean;
	}
	public static Double[] calQpoweredRMSSeries (Double[] qValues,Double[] RMS) {
		Double[] FqValues = new Double[qValues.length];
		for (int i=0;i<qValues.length;i++) {
			FqValues[i] = calcqRMS(RMS, qValues[i]);
		}
		return FqValues;
	}
	public static Double findMaxMinDifference (Double data[]) {
		Double MaxValue=data[0];
		Double MinValue=data[0];
		for (int i=0;i<data.length;i++) {
			if (data[i]>MaxValue) {
				MaxValue = data[i];
			}
			if (data[i]<MinValue) {
				MinValue = data[i];
			}
		}
		return (MaxValue - MinValue);
	}

}
