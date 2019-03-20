/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dgrf.fractal.core.dataseries;

import org.dgrf.fractal.termmeta.DataSeriesMeta;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import org.dgrf.cloud.response.DGRFResponseMessage;
import org.dgrf.cms.core.driver.CMSClientService;
import org.dgrf.cms.constants.CMSConstants;
import org.dgrf.cms.dto.TermInstanceDTO;
import org.dgrf.fractal.core.dto.DataSeriesDTO;
import org.dgrf.fractal.constants.FractalConstants;
import org.dgrf.fractal.core.dto.FractalDTO;
import org.dgrf.fractal.db.DAO.DataSeriesDAO;
import org.dgrf.fractal.db.entities.Dataseries;
import org.dgrf.fractal.db.entities.DataseriesPK;
import org.dgrf.fractal.core.util.DatabaseConnection;
import org.dgrf.fractal.response.FractalResponseCode;

/**
 *
 * @author bhaduri
 */
public class DataSeriesService {

    
    private int seriesType;
    private int noOfInsertedRows;

    

    public FractalDTO uploadDataSeries(FractalDTO fractalDTO) {
        String tempFilePath = fractalDTO.getCsvFilePath();
//     if no dataseries term instance is passed create one
        if (fractalDTO.getFractalTermInstance() == null) {
            fractalDTO = createDataSeriesTermInstance(fractalDTO);
        }
        Map<String, Object> dataSeriesTermInstance = fractalDTO.getFractalTermInstance();
        int seriesId = Integer.parseInt((String) dataSeriesTermInstance.get(DataSeriesMeta.DATA_SERIES_ID));
        int response = storeCsvInTable(tempFilePath, seriesId);
        if (response != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(response);
            return fractalDTO;
        } else {
            dataSeriesTermInstance.put(DataSeriesMeta.DATA_SERIES_TYPE, Integer.toString(seriesType));
            dataSeriesTermInstance.put(DataSeriesMeta.DATA_SERIES_LENGTH, Integer.toString(noOfInsertedRows));
        }
        CMSClientService cmscs = new CMSClientService();

        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermInstance(dataSeriesTermInstance);
        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
//            dataSeriesResponse.setOtherMessage(termInstanceDTO.getResponseMessage());
//            dataSeriesResponse.setResponseCode(DataSeriesResponse.FATAL_ERROR);
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        fractalDTO.setFractalTermInstance(dataSeriesTermInstance);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }

    private int storeCsvInTable(String tempFilePath, int seriesId) {
        DataSeriesDAO dataSeriesDAO = new DataSeriesDAO(DatabaseConnection.EMF);
        CSVFormat csvFileFormat = CSVFormat.DEFAULT;
        //DataSeriesResponse dataSeriesResponse = new DataSeriesResponse();
        String loadFilePath = tempFilePath + "bak";
        CSVParser csvFileParser;
        CSVPrinter csvPrinter;
        try {
            csvFileParser = new CSVParser(new FileReader(tempFilePath), csvFileFormat);
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(loadFilePath));
            csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

        } catch (IOException ex) {
            Logger.getLogger(DataSeriesService.class.getName()).log(Level.SEVERE, "Problem in openning temp csv file", ex);
            return FractalResponseCode.DATA_SERIES_SEVERE;
        }

        seriesType = FractalConstants.UNIFORM_SERIES;
        String xValueStr;
        String yValueStr;
        int colummCount;
        BigDecimal xvalue;
        BigDecimal yvalue;

        Dataseries dataseries;

        for (CSVRecord csvRecord : csvFileParser) {
            colummCount = csvRecord.size();
            xvalue = BigDecimal.ZERO;
            yvalue = BigDecimal.ZERO;
            long recordNumber = csvRecord.getRecordNumber();
            //check first record to identify series type
            if (recordNumber == 1 && colummCount == 1) {

                seriesType = FractalConstants.UNIFORM_SERIES;
            }
            if (recordNumber == 1 && colummCount == 2) {

                seriesType = FractalConstants.XY_SERIES;
            }
            //if not the identified series then rollback and return message
            if (seriesType == FractalConstants.XY_SERIES && colummCount == 1) {

                Logger.getLogger(DataSeriesService.class.getName()).log(Level.SEVERE, "Some Y values are missing", "Error in record number" + recordNumber);
                return FractalResponseCode.DATA_SERIES_SOME_Y_VALUES_MISSING;
            }

            if (seriesType == FractalConstants.XY_SERIES) {
                xValueStr = csvRecord.get(0);
                if (xValueStr.equals("")) {

                    return FractalResponseCode.DATA_SERIES_SOME_X_VALUES_MISSING;
                }
                xvalue = new BigDecimal(xValueStr);
                yValueStr = csvRecord.get(1);
                if (yValueStr.equals("")) {

                    Logger.getLogger(DataSeriesService.class.getName()).log(Level.SEVERE, "Some Y values are missing", "Error in record number" + recordNumber);
                    return FractalResponseCode.DATA_SERIES_SOME_Y_VALUES_MISSING;
                }
                yvalue = new BigDecimal(yValueStr);
            } else if (seriesType == FractalConstants.UNIFORM_SERIES) {

                yValueStr = csvRecord.get(0);
                yvalue = new BigDecimal(yValueStr);
            }
            try {

                csvPrinter.printRecord(Integer.toString(seriesId), Long.toString(recordNumber - 1), xvalue.toString(), yvalue.toString(), "0.0", "0.0");
            } catch (IOException ex) {
                Logger.getLogger(DataSeriesService.class.getName()).log(Level.SEVERE, "Problem in writing temp csv file", ex);
                return FractalResponseCode.DATA_SERIES_SEVERE;
            }
        }
        try {
            csvFileParser.close();
            csvPrinter.close();
        } catch (IOException ex) {
            Logger.getLogger(DataSeriesService.class.getName()).log(Level.SEVERE, "Problem in closing temp csv file", ex);
            return FractalResponseCode.DATA_SERIES_SEVERE;
        }
        //concurrency problem.. Two Users trying to create data series with same id.
        dataseries = dataSeriesDAO.findDataseries(new DataseriesPK(seriesId, 0));
        if (dataseries != null) {


            return FractalResponseCode.DATA_SERIES_UPDATE_CONCURRENCY_PROBLEM;
        }
        //batch load data from the generated csv
        noOfInsertedRows = dataSeriesDAO.loadDataSeries(loadFilePath);
        if (noOfInsertedRows == 0) {
//            dataSeriesResponse.setOtherMessage("Dataseries missing");
//            dataSeriesResponse.setResponseCode(DataSeriesResponse.FATAL_ERROR);
            Logger.getLogger(DataSeriesService.class.getName()).log(Level.SEVERE, "Problem loading table from temp file");
            return FractalResponseCode.DATA_SERIES_UPDATE_ERROR;
        }
//        else {
//            dataSeriesResponse.setNoOfRowsInserted(noOfInsertedRows);
//            dataSeriesResponse.setOtherMessage(noOfInsertedRows+" records uploaded.");
//            dataSeriesResponse.setResponseCode(DataSeriesResponse.SUCCESS);
//        }
        int response;
        response = moveToPosPlane(seriesId);
        if (response != FractalResponseCode.SUCCESS) {
            DGRFResponseMessage responseMessage = new DGRFResponseMessage();
//            dataSeriesResponse.setOtherMessage(responseMessage.getResponseMessage(response));
//            dataSeriesResponse.setResponseCode(DataSeriesResponse.FATAL_ERROR);
            Logger.getLogger(DataSeriesService.class.getName()).log(Level.SEVERE, "Dataseries move to positive plain failed");
            return response;
        }
        response = cumulateYseries(seriesId);
        if (response != FractalResponseCode.SUCCESS) {
//            ResponseMessage responseMessage = new ResponseMessage();
//            dataSeriesResponse.setOtherMessage(responseMessage.getResponseMessage(response));
//            dataSeriesResponse.setResponseCode(DataSeriesResponse.FATAL_ERROR);
            Logger.getLogger(DataSeriesService.class.getName()).log(Level.SEVERE, "Dataseries cumulation failed");
            return response;
        }
        return FractalResponseCode.SUCCESS;
    }

    private int cumulateYseries(int seriesId) {
        DataSeriesDAO dataSeriesDAO = new DataSeriesDAO(DatabaseConnection.EMF);
        Double meanYvalue = dataSeriesDAO.getMeanYValueById(seriesId);
        int response;
        response = dataSeriesDAO.subtractYMean(seriesId, meanYvalue);
        if (response != FractalResponseCode.SUCCESS) {
            return response;
        }
        response = dataSeriesDAO.cumulateY(seriesId);
        if (response != FractalResponseCode.SUCCESS) {
            return response;
        }
        return response;
    }

    private int moveToPosPlane(int seriesId) {
        DataSeriesDAO dataSeriesDAO = new DataSeriesDAO(DatabaseConnection.EMF);
        //get min y value to move the series to the positive plane. update the positive Y value
        Double minYValue = dataSeriesDAO.getMinYValueById(seriesId);
        if (minYValue >= 0) {
            minYValue = 0.0;
        } else {
            minYValue = 0 - minYValue;
        }
        //get min X value to move the series to the positive plane. update the positive X value
        Double minXValue = dataSeriesDAO.getMinXValueById(seriesId);
        if (minXValue >= 0) {
            minXValue = 0.0;
        } else {
            minXValue = 0 - minXValue;
        }
        //add min xy value to move the series to the positive plane. update the positive X and Y column with the min values obtained in previous steps
        int response = dataSeriesDAO.updateXYPos(seriesId, minXValue, minYValue);
        return response;
    }

    public FractalDTO deleteDataSeries(FractalDTO fractalDTO) {
        CMSClientService cmscs = new CMSClientService();
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug(FractalConstants.TERM_SLUG_DATASERIES);
        termInstanceDTO.setTermInstanceSlug(fractalDTO.getDataSeriesSlug());
        termInstanceDTO = cmscs.getTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        Map<String, Object> selectedDataSeries = termInstanceDTO.getTermInstance();
        String id = (String) selectedDataSeries.get(DataSeriesMeta.DATA_SERIES_ID);
        int seriesId = Integer.parseInt(id);
        DataSeriesDAO dataSeriesDAO = new DataSeriesDAO(DatabaseConnection.EMF);
        int response = dataSeriesDAO.deleteDataSeriesById(seriesId);
        if (response != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }

        termInstanceDTO = cmscs.deleteTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        return fractalDTO;
    }

    public FractalDTO getDataSeries(FractalDTO fractalDTO) {
        CMSClientService cmscs = new CMSClientService();
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug(FractalConstants.TERM_SLUG_DATASERIES);
        termInstanceDTO.setTermInstanceSlug(fractalDTO.getDataSeriesSlug());
        termInstanceDTO = cmscs.getTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        Map<String, Object> selectedDataSeries = termInstanceDTO.getTermInstance();
        String id = (String) selectedDataSeries.get(DataSeriesMeta.DATA_SERIES_ID);
        int seriesId = Integer.parseInt(id);
        DataSeriesDAO dataSeriesDAO = new DataSeriesDAO(DatabaseConnection.EMF);

        List<Dataseries> dataseriesList = dataSeriesDAO.getDataSeriesById(seriesId, FractalConstants.DATA_SERIES_VIEW_LIMIT);
        List<DataSeriesDTO> dataSeriesDTOList = dataseriesList.stream().map(dataseries -> {
            DataSeriesDTO dataSeriesDTO = new DataSeriesDTO();
            dataSeriesDTO.setDataindex(dataseries.getDataseriesPK().getDataindex());
            dataSeriesDTO.setSeriesid(dataseries.getDataseriesPK().getSeriesid());
            dataSeriesDTO.setXvalue(dataseries.getXvalue());
            dataSeriesDTO.setYvalue(dataseries.getYvalue());
            dataSeriesDTO.setXvaluePos(dataseries.getXvaluePos());
            dataSeriesDTO.setYvaluePos(dataseries.getYvaluePos());
            dataSeriesDTO.setYcumulative(dataseries.getYcumulative());
            return dataSeriesDTO;
        }).collect(Collectors.toList());
        fractalDTO.setDataseriesDTOList(dataSeriesDTOList);
        return fractalDTO;
    }

    public Long seriesDataCount(Map<String, Object> selectedDataSeries) {
        int dataseriesId = Integer.parseInt((String) selectedDataSeries.get("id"));
        DataSeriesDAO dataSeriesDao = new DataSeriesDAO(DatabaseConnection.EMF);
        Long dataCount = dataSeriesDao.getDataSeriesDataCount(dataseriesId);
        return dataCount;
    }

    public FractalDTO createDataSeriesTermInstance(FractalDTO fractalDTO) {

        int seriesId = generateSeriesID(fractalDTO);
        String termInstanceSlug = convertToAlphabets(seriesId);
        Map<String, Object> screenTermInstance = new HashMap<>();
        screenTermInstance.put(DataSeriesMeta.DATA_SERIES_USER, fractalDTO.getAuthCredentials().getUserId());
        screenTermInstance.put(DataSeriesMeta.DATA_SERIES_NAME, termInstanceSlug);
        screenTermInstance.put(DataSeriesMeta.DATA_SERIES_ID, Integer.toString(seriesId));
        screenTermInstance.put(DataSeriesMeta.DATA_SERIES_ORIGINAL_FILENAME, termInstanceSlug);
        screenTermInstance.put(CMSConstants.TERM_INSTANCE_SLUG, termInstanceSlug);
        screenTermInstance.put(CMSConstants.TERM_SLUG, FractalConstants.TERM_SLUG_DATASERIES);
        fractalDTO.setFractalTermInstance(screenTermInstance);
        return fractalDTO;
    }

    private int generateSeriesID(FractalDTO fractalDTO) {
        CMSClientService cmscs = new CMSClientService();
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setAuthCredentials(fractalDTO.getAuthCredentials());
        termInstanceDTO.setTermSlug(FractalConstants.TERM_SLUG_DATASERIES);
        termInstanceDTO = cmscs.getTermInstanceList(termInstanceDTO);
        List<Map<String, Object>> dataSeriesList = termInstanceDTO.getTermInstanceList();
        //List<Map<String, Object>> dataSeriesListUser = dataSeriesList.stream().filter(ds -> ds.get("user").equals(fractalDTO.getAuthCredentials().getUserId())).collect(Collectors.toList());
        int maxId;
        try {
            if (dataSeriesList.isEmpty()) {
                maxId = 1;
            } else {
                Map<String, Object> maxData = dataSeriesList.stream().max(Comparator.comparingInt(ds -> Integer.parseInt((String) ds.get(DataSeriesMeta.DATA_SERIES_ID)))).get();
                maxId = Integer.parseInt((String) maxData.get(DataSeriesMeta.DATA_SERIES_ID));
                maxId = maxId + 1;
            }
        } catch (NullPointerException ne) {
            maxId = 1;
        }
        return maxId;
    }

    private String convertToAlphabets(int num) {
        HashMap<String, String> intToAlphMap = new HashMap<>();
        intToAlphMap.put("0", "a");
        intToAlphMap.put("1", "b");
        intToAlphMap.put("2", "c");
        intToAlphMap.put("3", "d");
        intToAlphMap.put("4", "e");
        intToAlphMap.put("5", "f");
        intToAlphMap.put("6", "g");
        intToAlphMap.put("7", "h");
        intToAlphMap.put("8", "i");
        intToAlphMap.put("9", "j");

        String alph = Integer.toString(num);

        String convertedInt = "";
        for (int i = 0; i < alph.length(); i++) {
            int startPos = i;
            int endPos = i + 1;
            String c = alph.substring(startPos, endPos);
            String cToA = intToAlphMap.get(c);
            convertedInt = convertedInt + cToA;
        }

        return convertedInt;
    }
}
