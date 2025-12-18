package com.rajida.report.model;

import java.util.Map;

public class ReportRequest {
    private String jrxmlFileName;
    private Map<String, Object> parameters;
    private String exportFormat; // "pdf" or "html"

    public ReportRequest() {
    }

    public ReportRequest(String jrxmlFileName, Map<String, Object> parameters, String exportFormat) {
        this.jrxmlFileName = jrxmlFileName;
        this.parameters = parameters;
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

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }
}
