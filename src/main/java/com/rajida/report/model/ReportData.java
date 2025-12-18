package com.rajida.report.model;

import java.util.List;
import java.util.Map;

/**
 * DTO for sending report data and parameters
 * Used to populate JRXML templates with data
 */
public class ReportData {
    private String jrxmlFileName;
    private Map<String, Object> parameters; // Report parameters (title, company, etc.)
    private List<Map<String, Object>> dataRecords; // Report data/rows
    private String exportFormat; // "pdf" or "html"

    public ReportData() {
    }

    public ReportData(String jrxmlFileName, Map<String, Object> parameters,
            List<Map<String, Object>> dataRecords, String exportFormat) {
        this.jrxmlFileName = jrxmlFileName;
        this.parameters = parameters;
        this.dataRecords = dataRecords;
        this.exportFormat = exportFormat;
    }

    public String getJrxmlFileName() {
        return jrxmlFileName;
    }

    public void setJrxmlFileName(String jrxmlFileName) {
        this.jrxmlFileName = jrxmlFileName;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public List<Map<String, Object>> getDataRecords() {
        return dataRecords;
    }

    public void setDataRecords(List<Map<String, Object>> dataRecords) {
        this.dataRecords = dataRecords;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }
}
