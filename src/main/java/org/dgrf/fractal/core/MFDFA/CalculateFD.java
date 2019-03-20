package org.dgrf.fractal.core.MFDFA;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.dgrf.fractal.core.util.LinSpace;
import org.dgrf.fractal.core.util.LogUtil;
import org.dgrf.fractal.core.util.qOrderUtil;

public class CalculateFD {

    private QOrderRMS[] Fq;
    private secondOrderRMS[] F;
    private LinSpace qLinSpace;
    private LinSpace expLinSpace;
    private Double HurstExp;
    private Double HurstExpSE;
    private Double HurstExpRSquare;
    private Double chiSquareRejectCut;
    private Double MFDFAChiSquareVal;
    private Double H;
    private Double[] tq;
    SimpleRegression FReg;
    Double[] hq;

    CalculateFD(QOrderRMS[] Fq, secondOrderRMS[] F, LinSpace qLinSpace, LinSpace expLinSpace, Double chiSquareRejectCut) {
        this.F = F;
        this.Fq = Fq;
        this.qLinSpace = qLinSpace;
        this.expLinSpace = expLinSpace;
        this.chiSquareRejectCut = chiSquareRejectCut;
        processFqandF();
        if (chiSquareRejectCut > 0.0) {
            calcPSVGChiSquareVal();
        }
    }

    public void processFqandF() {
        FReg = new SimpleRegression(true);
        Double logOfSliceSize = 0.0;
        Double logOfSliceSecondOrderRMS = 0.0;
        Double logOfSliceQOrderRMS = 0.0;
        //Double[] qValues = qLinSpace.getLinSpaceList();
        for (int i = 0; i < F.length; i++) {
            logOfSliceSize = F[i].getLogOfSliceSize();
            //logOfSliceSize =LogUtil.logBaseK (F[i].getSliceSize());
            logOfSliceSecondOrderRMS = F[i].getLogOfSliceSecondOrderRMS();
            //logOfSliceSecondOrderRMS = LogUtil.logBaseK(F[i].getRMSValue());
            FReg.addData(logOfSliceSize, logOfSliceSecondOrderRMS);
        }
        HurstExp = FReg.getSlope();
        HurstExpSE = FReg.getSlopeStdErr();
        HurstExpRSquare = FReg.getRSquare();
        Double[] Hq = new Double[qLinSpace.getTotalCount()];
        tq = new Double[qLinSpace.getTotalCount()];
        hq = new Double[qLinSpace.getTotalCount() - 1];
        int prevQCounter = 0;
        SimpleRegression FqReg = new SimpleRegression(true);
        /*Calculation of Hq,hq and tq is done in the following steps. 
		 * For each q the slope of the values of Fq for all different slice sizes are 
		 * calculated. They are stored in the array named Hq. Hq array thereby has the same size
		 * as the number of q values.
		 * 
		 * tq is thereby calculated from Hq multiplying each Hq value with the 
		 * corresponding value of q in the q array and then subtracting 1 from it.
		 * 
		 * hq is then calculated from tq by subtracting qth element in tq from the
		 * (q-1)th element in tq and dividing the result by the space in the q array
		 * that is the different in the qth value from (q-1)th value of the q array.
		 * The size of hq is thereby length of q-1
		 * 
		 * Since elements in q array are linearly differentiated the difference of
		 * each element from the subsequent element is the same. So difference of q2th element
		 * and q1th element is taken.
         */
        for (int qCounter = 0; qCounter < qLinSpace.getTotalCount(); qCounter++) {
            for (int scaleCounter = 0; scaleCounter < expLinSpace.getTotalCount(); scaleCounter++) {
                logOfSliceSize = LogUtil.logBaseK(Fq[scaleCounter].getSliceSize());
                //System.out.print(Fq[scaleCounter].getFqValues()[qCounter]+",");
                logOfSliceQOrderRMS = LogUtil.logBaseK(Fq[scaleCounter].getFqValues()[qCounter]);
                //System.out.print(logOfSliceSize+","+logOfSliceQOrderRMS);
                FqReg.addData(logOfSliceSize, logOfSliceQOrderRMS);
            }
            //System.out.println();

            Hq[qCounter] = FqReg.getSlope();
            FqReg.clear();
            tq[qCounter] = (Hq[qCounter] * qLinSpace.getLinSpaceElement(qCounter)) - 1;
            
            if (qCounter > 0) {
                prevQCounter = qCounter - 1;
                hq[prevQCounter] = (tq[qCounter] - tq[prevQCounter]) / qLinSpace.getStep();

            }
            System.out.println(hq[prevQCounter]);
        }
        H = qOrderUtil.findMaxMinDifference(hq);

    }

    public Double getHurstExp() {
        return this.HurstExp;
    }

    public Double getHurstExpSE() {
        return this.HurstExpSE;
    }

    public Double getMultiFractalWidth() {
        return this.H;
    }

    public Double[] getTq() {
        return this.tq;
    }

    public Double[] gethq() {
        return this.hq;
    }

    public void printFDOnConsole() {
        Logger.getLogger(CalculateFD.class.getName()).log(Level.SEVERE, "Dimension = {0}", H);
    }

    public void printHurstExpOnConsole() {
        Logger.getLogger(CalculateFD.class.getName()).log(Level.SEVERE, "Hurst Exponent = {0}", HurstExp);

    }

    public void calcPSVGChiSquareVal() {

        int listSize = 0;
        Double expectLogOfSliceSecondOrderRMS = 0.0;
        Double actualLogOfSliceSecondOrderRMS = 0.0;
        Double diffExpectedActual = 0.0;
        Double absExpectLogOfSliceSecondOrderRMS = 0.0;
        Double squaredDiffDivExpected = 0.0;
        Double sumOfSquaredDiffDivExpected = 0.0;
        
        for (int i = 0; i < F.length; i++) {

            expectLogOfSliceSecondOrderRMS = FReg.predict(F[i].getLogOfSliceSize());
            actualLogOfSliceSecondOrderRMS = F[i].getLogOfSliceSecondOrderRMS();
            absExpectLogOfSliceSecondOrderRMS = Math.abs(expectLogOfSliceSecondOrderRMS);
            diffExpectedActual = Math.abs(expectLogOfSliceSecondOrderRMS - actualLogOfSliceSecondOrderRMS);

            
            if (diffExpectedActual <= chiSquareRejectCut) {
                squaredDiffDivExpected = (diffExpectedActual * diffExpectedActual) / absExpectLogOfSliceSecondOrderRMS;
                
                sumOfSquaredDiffDivExpected = sumOfSquaredDiffDivExpected + squaredDiffDivExpected;
                listSize++;
            }
        }

        if (listSize < 3) {
            Logger.getLogger(CalculateFD.class.getName()).log(Level.SEVERE,"Chi square could not be calculated" );
            MFDFAChiSquareVal = 999.0;
        }
        int degFreedom = listSize - 2;//2 is because there are expected and actual is for 2
        MFDFAChiSquareVal = sumOfSquaredDiffDivExpected / degFreedom;
    }

    /**
     * @return the hurstExpRSquare
     */
    public Double getHurstExpRSquare() {
        return HurstExpRSquare;
    }

    /**
     * @return the mFDFAChiSquareVal
     */
    public Double getMFDFAChiSquareVal() {
        return MFDFAChiSquareVal;
    }
}
