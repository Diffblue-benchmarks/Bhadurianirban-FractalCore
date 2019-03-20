package org.dgrf.fractal.core.MFDFA;

import java.util.ArrayList;
import java.util.List;
import org.dgrf.fractal.core.util.LinSpace;
import org.dgrf.fractal.core.util.LogUtil;
import org.dgrf.fractal.core.util.TimeSeriesUtil;
import org.dgrf.fractal.core.util.qOrderUtil;

public class CalculateFq {

    private List<Double> inputTimeSeries;
    private QOrderRMS[] Fq;
    private Double scaleMin;
    private Double scaleMax;
    private int ScaleNumber;
    private secondOrderRMS[] F;
    private LinSpace qLinSpace;
    private LinSpace expLinSpace;

    CalculateFq(Double scaleMax, Double scaleMin, int ScaleNumber, List<Double> inputTimeSeries) {
        this.scaleMax = scaleMax;
        this.scaleMin = scaleMin;
        this.ScaleNumber = ScaleNumber;
        this.inputTimeSeries = inputTimeSeries;
        calCulateFqValues();
    }

    CalculateFq(ArrayList<Double> inputTimeSeries) {
        this.scaleMax = 1024.0;
        this.scaleMin = 16.0;
        this.ScaleNumber = 19;
        this.inputTimeSeries = inputTimeSeries;
        calCulateFqValues();
    }

    public void calCulateFqValues() {
        Double exponentMin = LogUtil.logBaseK(scaleMin);
        Double exponentMax = LogUtil.logBaseK(scaleMax);

        qLinSpace = new LinSpace(-5.0, 5.0, 101);
        //Double[] qValues = qLinSpace.getLinSpaceList();

        expLinSpace = new LinSpace(exponentMin, exponentMax, ScaleNumber);
        //Double[] exponents = expLinSpace.getLinSpaceList ();
        Fq = new QOrderRMS[ScaleNumber];
        F = new secondOrderRMS[ScaleNumber];
        for (int expCounter = 0; expCounter < expLinSpace.getTotalCount(); expCounter++) {
            int sliceSize = (int) Math.round(Math.pow(2, expLinSpace.getLinSpaceElement(expCounter)));
            //System.out.print(exponents[expCounter]);
            //System.out.print(" ");
            int noOfSlice = inputTimeSeries.size() / sliceSize;

            ArrayList<Double> RMSOfSliceList = new ArrayList<Double>();
            Double RMSOfSliceVal = 0.0;
            //System.out.print("Slicesize "+ sliceSize);
            for (int sliceCounter = 0; sliceCounter < noOfSlice; sliceCounter++) {
                Double[] timeSeriesSlice = new Double[sliceSize];

                timeSeriesSlice = TimeSeriesUtil.getTimeSeriesSlice(inputTimeSeries, sliceSize, sliceCounter);

                double[] tsReg = TimeSeriesUtil.getPolyFitCoef(timeSeriesSlice);
                Double[] FitPredictValues = TimeSeriesUtil.getFitValues(tsReg, timeSeriesSlice.length);
                RMSOfSliceVal = TimeSeriesUtil.getRMSofDiffFitNActual(timeSeriesSlice, FitPredictValues);
                if (RMSOfSliceVal != 0.0) {
                    RMSOfSliceList.add(RMSOfSliceVal);
                }

            }

            Double[] RMSOfSlice = new Double[RMSOfSliceList.size()];
            RMSOfSliceList.toArray(RMSOfSlice);
            Double[] FqValues = qOrderUtil.calQpoweredRMSSeries(qLinSpace.getLinSpaceList(), RMSOfSlice);
            Double FValue = qOrderUtil.calcqRMS(RMSOfSlice, 2.0);
            F[expCounter] = new secondOrderRMS();
            F[expCounter].setSliceSize(sliceSize);
            F[expCounter].setRMSValue(FValue);
            Fq[expCounter] = new QOrderRMS();
            Fq[expCounter].setSliceSize(sliceSize);
            Fq[expCounter].setFqValues(FqValues);

        }
        //for (int i=0;i<Fq[0].FqValues.length;i++)
        //System.out.println(Fq[0].FqValues[i]);

    }

    public QOrderRMS[] getFqMatrix() {
        return this.Fq;
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
