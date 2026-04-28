package kz.idc.error;

public enum Errors {
    NULL_POINTER_NETWORK_DEVICE("Network Device is null"),
    NULL_POINTER_AC_DEVICE("AC Device is null"),
    TERMINAL_ALREADY_EXIST("Terminal already exist with ip"),
    CONNECTION_TIMEOUT("Connect Error: connection timed out:"),
    CONNECT_REFUSED("Connect Error: Connection refused:"),
    HOST_IS_DOWN("Connect Error: Host is down:"),
    NO_ROUTE_TO_HOST("Connect Error: No route to host:"),
    READ_TIMEOUT("Read Timeout"),
    NULL_POINTER_JSON("onSuccess called with null."),
    UNEXPECTED_ERROR("Unexpected error"),
    NETWORK_IS_UNREACHABLE("Connect Error: Network is unreachable:");

    public String EXCEPTION;

    Errors(String exception){
        EXCEPTION = exception;
    }
}
