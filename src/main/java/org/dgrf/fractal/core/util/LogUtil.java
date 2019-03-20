package org.dgrf.fractal.core.util;

public class LogUtil {
	private static double logBase=2.0;

	public static double logBaseK (double a) {
		double res;
		
		res =  ((double)(Math.log(a))/(double)(Math.log(logBase)));
		
		return res;
	}
	public static void setLogBase (double logBase) {
		LogUtil.logBase=logBase;
	}

}
