package org.dgrf.fractal.core.MFDXA;

import java.util.ArrayList;
import java.util.List;
import org.dgrf.fractal.core.MFDFA.secondOrderRMS;
import org.dgrf.fractal.core.util.LinSpace;
import org.dgrf.fractal.core.util.LogUtil;
import org.dgrf.fractal.core.util.TimeSeriesUtil;
import org.dgrf.fractal.core.util.qOrderUtil;

public class CalculateMFDXAFq {

    private List<Double> inputTimeSeriesFirst;
    private List<Double> inputTimeSeriesSecond;
//	private QOrderRMS[] Fq;
    private Double scaleMin;
    private Double scaleMax;
    private int ScaleNumber;
    private secondOrderRMS[] F;
    private LinSpace qLinSpace;
    private LinSpace expLinSpace;

    CalculateMFDXAFq(Double scaleMax, Double scaleMin, int ScaleNumber,
            List<Double> inputTimeSeriesFirst, List<Double> inputTimeSeriesSecond) {
        this.scaleMax = scaleMax;
        this.scaleMin = scaleMin;
        this.ScaleNumber = ScaleNumber;
        this.inputTimeSeriesFirst = inputTimeSeriesFirst;
        this.inputTimeSeriesSecond = inputTimeSeriesSecond;
        calCulateFqValues();
    }

    public void calCulateFqValues() {
        Double exponentMin = LogUtil.logBaseK(scaleMin);
        Double exponentMax = LogUtil.logBaseK(scaleMax);

        qLinSpace = new LinSpace(-5.0, 5.0, 101);
        //Double[] qValues = qLinSpace.getLinSpaceList();

        expLinSpace = new LinSpace(exponentMin, exponentMax, ScaleNumber);
        //Double[] exponents = expLinSpace.getLinSpaceList ();
//		Fq = new QOrderRMS[ScaleNumber];
        F = new secondOrderRMS[ScaleNumber];
        for (int expCounter = 0; expCounter < expLinSpace.getTotalCount(); expCounter++) {
            int sliceSize = (int) Math.round(Math.pow(2, expLinSpace.getLinSpaceElement(expCounter)));

            int noOfSlice = (inputTimeSeriesFirst.size() < inputTimeSeriesSecond.size()) ? inputTimeSeriesFirst.size() / sliceSize : inputTimeSeriesSecond.size() / sliceSize;

            ArrayList<Double> RMSOfSliceList = new ArrayList<Double>();
            Double RMSOfSliceVal = 0.0;
            Double RMSOfSliceValRev = 0.0;

            for (int sliceCounter = 0; sliceCounter < noOfSlice; sliceCounter++) {
                Double[] timeSeriesSliceFirst = new Double[sliceSize];
                Double[] timeSeriesSliceSecond = new Double[sliceSize];

                Double[] timeSeriesSliceFirstRev = new Double[sliceSize];
                Double[] timeSeriesSliceSecondRev = new Double[sliceSize];

                timeSeriesSliceFirst = TimeSeriesUtil.getTimeSeriesSlice(inputTimeSeriesFirst, sliceSize, sliceCounter);
                timeSeriesSliceSecond = TimeSeriesUtil.getTimeSeriesSlice(inputTimeSeriesSecond, sliceSize, sliceCounter);

                timeSeriesSliceFirstRev = TimeSeriesUtil.getReverseTimeSeriesSlice(inputTimeSeriesFirst, sliceSize, sliceCounter);
                timeSeriesSliceSecondRev = TimeSeriesUtil.getReverseTimeSeriesSlice(inputTimeSeriesSecond, sliceSize, sliceCounter);

                double[] tsRegFirst = TimeSeriesUtil.getPolyFitCoef(timeSeriesSliceFirst);
                double[] tsRegSecond = TimeSeriesUtil.getPolyFitCoef(timeSeriesSliceSecond);

                double[] tsRegFirstRev = TimeSeriesUtil.getPolyFitCoef(timeSeriesSliceFirstRev);
                double[] tsRegSecondRev = TimeSeriesUtil.getPolyFitCoef(timeSeriesSliceSecondRev);

                Double[] FitPredictValuesFirst = TimeSeriesUtil.getFitValues(tsRegFirst, timeSeriesSliceFirst.length);
                Double[] FitPredictValuesSecond = TimeSeriesUtil.getFitValues(tsRegSecond, timeSeriesSliceSecond.length);

                Double[] FitPredictValuesFirstRev = TimeSeriesUtil.getFitValues(tsRegFirstRev, timeSeriesSliceFirstRev.length);
                Double[] FitPredictValuesSecondRev = TimeSeriesUtil.getFitValues(tsRegSecondRev, timeSeriesSliceSecondRev.length);
                RMSOfSliceVal = TimeSeriesUtil.calculateRMSVectorProduct(timeSeriesSliceFirst, timeSeriesSliceSecond, FitPredictValuesFirst, FitPredictValuesSecond, false);
                RMSOfSliceValRev = TimeSeriesUtil.calculateRMSVectorProduct(timeSeriesSliceFirstRev, timeSeriesSliceSecondRev, FitPredictValuesFirstRev, FitPredictValuesSecondRev, false);

                if (RMSOfSliceVal != 0.0) {
                    RMSOfSliceList.add(RMSOfSliceVal);
                }
                if (RMSOfSliceValRev != 0.0) {
                    RMSOfSliceList.add(RMSOfSliceValRev);
                }
            }
            Double[] RMSOfSlice = new Double[RMSOfSliceList.size()];
            RMSOfSliceList.toArray(RMSOfSlice);

            Double FValue = qOrderUtil.calcqRMS(RMSOfSlice, 2.0);
            F[expCounter] = new secondOrderRMS();
            F[expCounter].setSliceSize(sliceSize);
            F[expCounter].setRMSValue(FValue);

        }

    }

    public secondOrderRMS[] getFVector() {
        return this.F;
    }

    public LinSpace getQLinSpace() {
        return this.qLinSpace;
    }

    public LinSpace getExpLinSpace() {
        return this.expLinSpace;
    }

}
