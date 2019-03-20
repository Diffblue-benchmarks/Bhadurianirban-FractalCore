package org.dgrf.fractal.core.MFDXA;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.dgrf.fractal.core.MFDFA.secondOrderRMS;


//import FractalUtil.LinSpace;
//import FractalUtil.qOrderUtil;
//import MFDFA.QOrderRMS;


public class CalculateMFDXAFD {
//	private QOrderRMS[] Fq;
	private secondOrderRMS[] F;
//	private LinSpace qLinSpace;
//	private LinSpace expLinSpace;
	private Double HurstExp;
	private Double HurstExpSE;
	private Double HurstExpRSquare;
	private Double GammaX;
	private Double[][] scaleValues;
	SimpleRegression FReg;
	Double chiSquareRejectCut;
	Double MFDXAChiSquareVal;

	CalculateMFDXAFD (secondOrderRMS[] F,Double chiSquareRejectCut) {
		this.F = F;
		this.chiSquareRejectCut = chiSquareRejectCut;

		processFqandF ();
		if (chiSquareRejectCut>0.0) {
			calcPSVGChiSquareVal();
		}
	}

	public void processFqandF () {
		FReg = new SimpleRegression(true);
		Double logOfSliceSize = 0.0;
		Double logOfSliceSecondOrderRMS = 0.0;
//		Double logOfSliceQOrderRMS = 0.0;
		//Double[] qValues = qLinSpace.getLinSpaceList();
		scaleValues = new Double[F.length][2];
		for (int i=0;i<F.length;i++) {
			logOfSliceSize =F[i].getLogOfSliceSize();
			logOfSliceSecondOrderRMS = F[i].getLogOfSliceSecondOrderRMS();
			scaleValues[i][0] =  logOfSliceSize;
			scaleValues[i][1] =  logOfSliceSecondOrderRMS;
			FReg.addData(logOfSliceSize,logOfSliceSecondOrderRMS);
		}
		HurstExp = FReg.getSlope();
		HurstExpSE = FReg.getSlopeStdErr();
		HurstExpRSquare = FReg.getRSquare();
		GammaX = 2 - (2*HurstExp); 


	}
	public void calcPSVGChiSquareVal () {
		
		int listSize=0;
		Double expectLogOfSliceSecondOrderRMS = 0.0;
		Double actualLogOfSliceSecondOrderRMS = 0.0;
		Double diffExpectedActual = 0.0;
		Double absExpectLogOfSliceSecondOrderRMS = 0.0;
		Double squaredDiffDivExpected = 0.0;
		Double sumOfSquaredDiffDivExpected = 0.0;
		
		for (int i = 0;i<F.length;i++) {
			
			expectLogOfSliceSecondOrderRMS = FReg.predict(F[i].getLogOfSliceSize());
			actualLogOfSliceSecondOrderRMS = F[i].getLogOfSliceSecondOrderRMS();
			absExpectLogOfSliceSecondOrderRMS = Math.abs(expectLogOfSliceSecondOrderRMS);
			diffExpectedActual = Math.abs(expectLogOfSliceSecondOrderRMS-actualLogOfSliceSecondOrderRMS);
			
			
			if (diffExpectedActual <= chiSquareRejectCut) {
				squaredDiffDivExpected = (diffExpectedActual*diffExpectedActual)/absExpectLogOfSliceSecondOrderRMS;
				
				sumOfSquaredDiffDivExpected = sumOfSquaredDiffDivExpected+squaredDiffDivExpected;
				listSize++;
			}
		}
		
		if (listSize<3) {
			Logger.getLogger(CalculateMFDXAFD.class.getName()).log(Level.SEVERE,"Chi square could not be calculated" );
			MFDXAChiSquareVal = 999.0;
		}
		int degFreedom = listSize-2;//2 is because there are expected and actual is for 2
		MFDXAChiSquareVal = sumOfSquaredDiffDivExpected/degFreedom;
	}
	public Double getHurstExp () {
		return this.HurstExp;
	}
	public Double getHurstExpSE () {
		return this.HurstExpSE;
	}

	public Double getGammaX () {
		return this.GammaX;
	}

	public void printScaleValuesOnConsole () {
		for (int i=0;i<F.length;i++) {
                    Logger.getLogger(CalculateMFDXAFD.class.getName()).log(Level.INFO, "{0} {1}", new Object[]{scaleValues[i][0], scaleValues[i][1]});
			
		}
	}

	
	public void printHurstExpOnConsole () {
		
                Logger.getLogger(CalculateMFDXAFD.class.getName()).log(Level.SEVERE, "Hurst Exponent = {0}", HurstExp);
	}
	public void printGammaXOnConsole () {
		
                Logger.getLogger(CalculateMFDXAFD.class.getName()).log(Level.SEVERE, "GammaX = {0}", GammaX);
	}

	/**
	 * @return the mFDXAChiSquareVal
	 */
	public Double getMFDXAChiSquareVal() {
		return MFDXAChiSquareVal;
	}

	/**
	 * @return the hurstExpRSquare
	 */
	public Double getHurstExpRSquare() {
		return HurstExpRSquare;
	}
}
