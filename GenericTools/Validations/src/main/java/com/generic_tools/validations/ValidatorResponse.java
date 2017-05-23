package com.generic_tools.validations;

/**
 * Created by taljmars on 3/8/17.
 */
public class ValidatorResponse {
    enum Status {
        SUCCESS,
        FAILURE
    }

    private Status status;
    private String message;

    public ValidatorResponse(Status status) {
        this.status = status;
    }

    public ValidatorResponse(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public boolean isFailed() {
        return this.status.equals(Status.FAILURE);
    }

    public boolean isSuccess() {
        return this.status.equals(Status.SUCCESS);
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ValidatorResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                '}';
    }

}
