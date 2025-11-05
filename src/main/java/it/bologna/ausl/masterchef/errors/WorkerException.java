package it.bologna.ausl.masterchef.errors;

public class WorkerException extends Exception {
private static final long serialVersionUID = 1L;
private String appID = null;
private String jobID = null;
private String jobType = null;
private String jobN = null;
private ErrorDetails errorDetails = null;

    public WorkerException (String jobType, String message){
            super(message);
            this.jobType = jobType;
    }

    public WorkerException(String jobType, String message, Throwable cause) {
            super (message,cause);
            this.jobType = jobType;
    }

    public WorkerException(String jobType, Throwable cause) {
            super (cause);
            this.jobType = jobType;
    }

    public WorkerException (String jobType, String message, ErrorDetails errorDetails){
            super(message);
            this.jobType = jobType;
            this.errorDetails = errorDetails;
    }
    
    public WorkerException(String jobType, String message, Throwable cause, ErrorDetails errorDetails) {
            super (message,cause);
            this.jobType = jobType;
            this.errorDetails = errorDetails;
    }
    
    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getAppID() {
        return appID;
    }

    public String getJobID() {
        return jobID;
    }
        
    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getJobN() {
        return jobN;
    }

    public void setJobN(String jobN) {
        this.jobN = jobN;
    }

    public ErrorDetails getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(ErrorDetails errorDetails) {
        this.errorDetails = errorDetails;
    }
    
    @Override
    public String toString() {
        return super.toString() + "\njobID: " + jobID + "\njobType: " + jobType + "\njobN: " + jobN;
    }
}
