package it.bologna.ausl.masterchef.errors;

/**
 *
 * @author utente
 */
public class SendToParerErrorDetails implements ErrorDetails{
    private String errorType;
    private String errorCode;
    private String errorMessage;
    private String parerResponse;

    public SendToParerErrorDetails() {}
    
    public SendToParerErrorDetails(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    } 
     
    public SendToParerErrorDetails(String errorType, String errorCode, String errorMessage) {
        this.errorType = errorType;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public SendToParerErrorDetails(String errorType, String errorCode, String errorMessage, String parerResponse) {
        this.errorType = errorType;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.parerResponse = parerResponse;
    }
    
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }
    
    public String getParerResponse() {
        return parerResponse;
    }

    public void setParerResponse(String parerResponse) {
        this.parerResponse = parerResponse;
    }
    
    @Override
    public String toString() {
        return "Http Error Type: " + errorType + "\nError Code: " + errorCode + "\nError Message: " + errorMessage + "\nParer Response: " + parerResponse;
    }
    
}
