package com.example.yalatour.Classes;

public class TripRequestsClass {
    private String RequestId;
    private String Request_UserId;
    private String Request_Username;

    public TripRequestsClass() {
    }

    public TripRequestsClass(String requestId, String request_UserId, String request_Username) {
        RequestId = requestId;
        Request_UserId = request_UserId;
        Request_Username = request_Username;
    }

    public String getRequestId() {
        return RequestId;
    }

    public void setRequestId(String requestId) {
        RequestId = requestId;
    }

    public String getRequest_UserId() {
        return Request_UserId;
    }

    public void setRequest_UserId(String request_UserId) {
        Request_UserId = request_UserId;
    }

    public String getRequest_Username() {
        return Request_Username;
    }

    public void setRequest_Username(String request_Username) {
        Request_Username = request_Username;
    }
}
