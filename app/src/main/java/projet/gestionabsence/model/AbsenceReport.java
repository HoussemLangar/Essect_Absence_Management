package projet.gestionabsence.model;

import java.util.Objects;

public class AbsenceReport {

    private String reportId;
    private String fileUrl;

    public AbsenceReport() {
    }

    public AbsenceReport(String reportId, String fileUrl) {
        this.reportId = reportId;
        this.fileUrl = fileUrl;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    @Override
    public String toString() {
        return "AbsenceReport{" +
                "reportId='" + reportId + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId, fileUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbsenceReport that = (AbsenceReport) o;
        return Objects.equals(reportId, that.reportId) &&
                Objects.equals(fileUrl, that.fileUrl);
    }
}
