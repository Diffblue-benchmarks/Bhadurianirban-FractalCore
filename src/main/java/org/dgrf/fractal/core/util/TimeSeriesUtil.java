package org.dgrf.fractal.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

public class TimeSeriesUtil {

    public static Double calcMeanOfTimeSeries(ArrayList<Double> timeSeries) {
        Double aggregate = 0.0;
        for (int i = 0; i < timeSeries.size(); i++) {
            aggregate = aggregate + timeSeries.get(i);
        }
        Double meanOfTimeSeries = aggregate / timeSeries.size();

        return meanOfTimeSeries;
    }

    public static ArrayList<Double> substractMeanFromTS(ArrayList<Double> timeSeries) {
        Double meanOfTimeSeries = calcMeanOfTimeSeries(timeSeries);
        Double subMeanVal;
        ArrayList<Double> subMeanTimeSeries = new ArrayList<Double>();
        for (int i = 0; i < timeSeries.size(); i++) {
            subMeanVal = timeSeries.get(i) - meanOfTimeSeries;
            
            subMeanTimeSeries.add(subMeanVal);
        }
        return subMeanTimeSeries;
    }

    /*public void cumulateTimeSeries (ArrayList<Double> ts) {
		cumTimeSeries = new ArrayList<Double>();
		Double cumVal = 0.0;
		for (int i=0;i<ts.size();i++) {
			cumVal = cumVal + ts.get(i);
			cumTimeSeries.add(cumVal);
		}
		
	}*/
    public static ArrayList<Double> cumulateTimeSeries(ArrayList<Double> subMeanTimeSeries) {
        ArrayList<Double> cumTimeSeries = new ArrayList<Double>();
        Double cumVal = 0.0;
        for (int i = 0; i < subMeanTimeSeries.size(); i++) {
            cumVal = cumVal + subMeanTimeSeries.get(i);
            cumTimeSeries.add(cumVal);
        }
        return cumTimeSeries;
    }

    public static ArrayList<Double> getCumTimeSeries(ArrayList<Double> rawTimeSeries) {
        ArrayList<Double> subMeanTimeSeries = substractMeanFromTS(rawTimeSeries);
        ArrayList<Double> cumTimeSeries = cumulateTimeSeries(subMeanTimeSeries);
        return cumTimeSeries;
    }

    public static Double[] getTimeSeriesSlice(List<Double> ts, int size, int sliceNo) {
        Double[] timeSeriesSlice = new Double[size];

        int startIndex = size * sliceNo;
        int sliceArrayCounter = 0;
        for (int i = startIndex; i < (startIndex + size); i++) {
            timeSeriesSlice[sliceArrayCounter] = ts.get(i);
            sliceArrayCounter++;
        }
        return timeSeriesSlice;
    }

    public static Double[] getReverseTimeSeriesSlice(List<Double> ts, int size, int sliceNo) {
        Double[] timeSeriesSlice = new Double[size];

        int startIndex = (ts.size() - 1) - size * sliceNo;

        int sliceArrayCounter = 0;
        for (int i = startIndex; i > (startIndex - size); i--) {
            timeSeriesSlice[sliceArrayCounter] = ts.get(i);
            sliceArrayCounter++;
        }
        return timeSeriesSlice;
    }

    public static double[] getPolyFitCoef(Double[] ts) {
        WeightedObservedPoints obs = new WeightedObservedPoints();
        //tsReg = new SimpleRegression(true);

        for (int i = 0; i < ts.length; i++) {
            obs.add(i, ts[i]);
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
        double[] tsRegCoeff = fitter.fit(obs.toList());
        return tsRegCoeff;
    }

    public static Double[] getFitValues(double[] tsRegCoeff, int lengthOfSeries) {

        PolynomialFunction p = new PolynomialFunction(tsRegCoeff);
        Double[] fitValues = new Double[lengthOfSeries];
        for (int i = 0; i < lengthOfSeries; i++) {
            fitValues[i] = p.value(i);
        }
        return fitValues;
    }

    public static Double getRMSofDiffFitNActual(Double[] Actual, Double Fit[]) {

        if (Actual.length != Fit.length) {
            Logger.getLogger(TimeSeriesUtil.class.getName()).log(Level.SEVERE,"Actual and Fit Arrays are of Different Length" );
            
            return null;
        }
        //SummaryStatistics s = new SummaryStatistics();

        Double[] fitActualDiffVals = new Double[Actual.length];
        for (int i = 0; i < Actual.length; i++) {
            fitActualDiffVals[i] = Actual[i] - Fit[i];
        }
        Double secOrderRMS = qOrderUtil.calcqRMS(fitActualDiffVals, 2.0);
        return secOrderRMS;
    }

    public static List<Double> getGappedTimeSeries(List<Double> InputTimeSeriesData, int gap) {
        List<Double> gappedTimeSeries = new ArrayList<Double>();
        int lengthToTraverse = InputTimeSeriesData.size();
        for (int i = 0; i < lengthToTraverse; i = i + gap) {
            gappedTimeSeries.add(InputTimeSeriesData.get(i));
        }
        return gappedTimeSeries;
    }

    public static ArrayList<Double> moveToPositivePlane(ArrayList<Double> rawTimeSeriesData) {

        Double minDataValue = findMinValue(rawTimeSeriesData);

        ArrayList<Double> posPlainTimeSeries = addMinValue(rawTimeSeriesData, minDataValue);

        return posPlainTimeSeries;
    }
//	public static ArrayList<XYTimeSeries>  moveToPositivePlaneXY (ArrayList<XYTimeSeries> rawTimeSeriesData) {
//		ArrayList<Double> rawYTimeSeriesData = new ArrayList<Double>();
//		for (int i=0;i<rawTimeSeriesData.size();i++) {
//			rawYTimeSeriesData.add(rawTimeSeriesData.get(i).getSeriesVal());
//		}
//		ArrayList<Double> posPlainTimeSeries = moveToPositivePlane (rawYTimeSeriesData);
//		ArrayList<XYTimeSeries> posPlainXYTimeSeries = new ArrayList<XYTimeSeries>();
//		for (int i=0;i<posPlainTimeSeries.size();i++) {
//			XYTimeSeries psvgTimeSeries = new XYTimeSeries(rawTimeSeriesData.get(i).getSeriesTime(),posPlainTimeSeries.get(i)); 
//			posPlainXYTimeSeries.add(psvgTimeSeries);
//		}
//		return posPlainXYTimeSeries;
//
//	}

    private static Double findMinValue(ArrayList<Double> rawTimeSeriesData) {
        int i;
        if (rawTimeSeriesData.size() == 0) {
            return null;
        }
        Double minVal = rawTimeSeriesData.get(0);
        for (i = 0; i < rawTimeSeriesData.size(); i++) {
            if (minVal > rawTimeSeriesData.get(i)) {
                minVal = rawTimeSeriesData.get(i);
            }
        }
        if (minVal >= 0) {
            return (double) 0;
        } else {
            return minVal;
        }

    }

    private static ArrayList<Double> addMinValue(ArrayList<Double> rawTimeSeriesData, Double minDataValue) {
        Double valueToAdd = (double) 0;
        Double absMinDataValue = Math.abs(minDataValue);
        ArrayList<Double> posPlainTimeSeries = new ArrayList<Double>();

        for (int i = 0; i < rawTimeSeriesData.size(); i++) {
            valueToAdd = rawTimeSeriesData.get(i) + absMinDataValue;
            
            posPlainTimeSeries.add(valueToAdd);
        }
        return posPlainTimeSeries;
    }

    public static Double calculateRMSVectorProduct(Double[] timeSeriesActualFirst, Double[] timeSeriesActualSecond,
            Double[] timeSeriesFitFirst, Double[] timeSeriesFitSecond, boolean prflag) {
        if (timeSeriesActualFirst.length != timeSeriesFitFirst.length || timeSeriesActualSecond.length != timeSeriesFitSecond.length) {
            
            Logger.getLogger(TimeSeriesUtil.class.getName()).log(Level.SEVERE,"Actual and Fit Arrays are of Different Length" );
            return null;
        }
        Double[] fitActualDiffValsFirst = new Double[timeSeriesActualFirst.length];
        Double[] fitActualDiffValsSecond = new Double[timeSeriesActualSecond.length];
        Double total = 0.0;
        for (int i = 0; i < timeSeriesActualFirst.length; i++) {
            fitActualDiffValsFirst[i] = timeSeriesActualFirst[i] - timeSeriesFitFirst[i];
            fitActualDiffValsSecond[i] = timeSeriesActualSecond[i] - timeSeriesFitSecond[i];
            total += fitActualDiffValsFirst[i] * fitActualDiffValsSecond[i];
        }
        if (prflag) {
            
            Double meanVal = Math.abs(total / timeSeriesActualFirst.length);
            
            Double sqrtVal = Math.sqrt(meanVal);
            
        }

        Double RMSVecProduct = Math.sqrt(Math.abs(total / timeSeriesActualFirst.length));

        return RMSVecProduct;

    }

}
