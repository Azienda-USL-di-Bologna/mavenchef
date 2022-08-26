package it.bologna.ausl.masterchef.errors;

/**
 *
 * @author Giuseppe De Marco (gdm)
 */
public class UploadGdDocErrorDetails implements ErrorDetails {
private int httpErrorStatusCode = -1;
private String errorMessage;

    public UploadGdDocErrorDetails() {    
    }

    public UploadGdDocErrorDetails(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public UploadGdDocErrorDetails(int httpErrorStatusCode, String errorMessage) {
        this.httpErrorStatusCode = httpErrorStatusCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getHttpErrorStatusCode() {
        return httpErrorStatusCode;
    }

    public void setHttpErrorStatusCode(int httpErrorStatusCode) {
        this.httpErrorStatusCode = httpErrorStatusCode;
    }

    @Override
    public String toString() {
        return "Http Error Status Code: " + httpErrorStatusCode + "\nMessage:" + errorMessage;
    }

}
