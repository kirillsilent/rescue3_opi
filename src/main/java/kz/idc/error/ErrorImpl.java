package kz.idc.error;

import kz.idc.dto.error.ErrorDTO;

import javax.inject.Singleton;

@Singleton
public class ErrorImpl implements Error {

    private String translateError(String error) {
        if (error.contains(Errors.TERMINAL_ALREADY_EXIST.EXCEPTION)) {
            return Translate.TERMINAL_ALREADY_EXIST.EXCEPTION;
        } else if (error.contains(Errors.CONNECTION_TIMEOUT.EXCEPTION)
                || error.contains(Errors.HOST_IS_DOWN.EXCEPTION)
                || error.contains(Errors.NO_ROUTE_TO_HOST.EXCEPTION)
                || error.contains(Errors.CONNECT_REFUSED.EXCEPTION)
                || error.contains(Errors.NETWORK_IS_UNREACHABLE.EXCEPTION)) {
            return Translate.CONNECTION.EXCEPTION;
        } else if (error.contains(Errors.NULL_POINTER_NETWORK_DEVICE.EXCEPTION)) {
            return Translate.NULL_POINTER_NETWORK_DEVICE.EXCEPTION;
        } else if (error.contains(Errors.READ_TIMEOUT.EXCEPTION)) {
            return Translate.READ_TIMEOUT.EXCEPTION;
        } else if(error.contains(Errors.NULL_POINTER_JSON.EXCEPTION)){
            return Translate.NULL_POINTER_JSON.EXCEPTION;
        }
        return null;
    }

    private String sipErrorTranslateRu(String error) {
        if (error.contains(Errors.CONNECTION_TIMEOUT.EXCEPTION)
                || error.contains(Errors.HOST_IS_DOWN.EXCEPTION)
                || error.contains(Errors.NO_ROUTE_TO_HOST.EXCEPTION)
                || error.contains(Errors.CONNECT_REFUSED.EXCEPTION)) {
            return SipRu.CONNECTION.EXCEPTION;
        } else if (error.contains(Errors.READ_TIMEOUT.EXCEPTION)) {
            return SipRu.READ_TIMEOUT.EXCEPTION;
        }
        return error;
    }

    private String sipErrorTranslateEn(String error) {
        if (error.contains(Errors.CONNECTION_TIMEOUT.EXCEPTION)
                || error.contains(Errors.HOST_IS_DOWN.EXCEPTION)
                || error.contains(Errors.NO_ROUTE_TO_HOST.EXCEPTION)
                || error.contains(Errors.CONNECT_REFUSED.EXCEPTION)) {
            return SipEn.CONNECTION.EXCEPTION;
        } else if (error.contains(Errors.READ_TIMEOUT.EXCEPTION)) {
            return SipEn.READ_TIMEOUT.EXCEPTION;
        }
        return error;
    }
    private String cardErrorTranslateRu(String error) {
        if (error.contains(Errors.CONNECTION_TIMEOUT.EXCEPTION)
                || error.contains(Errors.HOST_IS_DOWN.EXCEPTION)
                || error.contains(Errors.NO_ROUTE_TO_HOST.EXCEPTION)
                || error.contains(Errors.CONNECT_REFUSED.EXCEPTION)) {
            return CardRu.CONNECTION.EXCEPTION;
        } else if (error.contains(Errors.READ_TIMEOUT.EXCEPTION)) {
            return CardRu.READ_TIMEOUT.EXCEPTION;
        }
        return error;
    }

    private String cardErrorTranslateEn(String error) {
        if (error.contains(Errors.CONNECTION_TIMEOUT.EXCEPTION)
                || error.contains(Errors.HOST_IS_DOWN.EXCEPTION)
                || error.contains(Errors.NO_ROUTE_TO_HOST.EXCEPTION)
                || error.contains(Errors.CONNECT_REFUSED.EXCEPTION)) {
            return CardEn.CONNECTION.EXCEPTION;
        } else if (error.contains(Errors.READ_TIMEOUT.EXCEPTION)) {
            return CardEn.READ_TIMEOUT.EXCEPTION;
        }
        return error;
    }

    private String networkModuleTranslateError(String error) {
        if (error.contains(Errors.CONNECTION_TIMEOUT.EXCEPTION)
                || error.contains(Errors.HOST_IS_DOWN.EXCEPTION)
                || error.contains(Errors.NO_ROUTE_TO_HOST.EXCEPTION)
                || error.contains(Errors.CONNECT_REFUSED.EXCEPTION)) {
            return NetworkTranslate.CONNECTION.EXCEPTION;
        } else if (error.contains(Errors.READ_TIMEOUT.EXCEPTION)) {
            return NetworkTranslate.READ_TIMEOUT.EXCEPTION;
        }
        return null;
    }

    private String acModuleTranslateError(String error) {
        if (error.contains(Errors.CONNECTION_TIMEOUT.EXCEPTION)
                || error.contains(Errors.HOST_IS_DOWN.EXCEPTION)
                || error.contains(Errors.NO_ROUTE_TO_HOST.EXCEPTION)
                || error.contains(Errors.CONNECT_REFUSED.EXCEPTION)) {
            return ACTranslate.CONNECTION.EXCEPTION;
        } else if (error.contains(Errors.READ_TIMEOUT.EXCEPTION)) {
            return ACTranslate.READ_TIMEOUT.EXCEPTION;
        }
        return null;
    }

    private String planTranslateError(String error) {
        if (error.contains(Errors.UNEXPECTED_ERROR.EXCEPTION)) {
            return PlanTranslate.NOT_FOUND.EXCEPTION;
        }else {
            return translateError(error);
        }
    }

    private ErrorDTO createErrorDTO(String error) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setDescription(error);
        return errorDTO;
    }

    @Override
    public ErrorDTO createError(String error) {
        return createErrorDTO(translateError(error));
    }

    @Override
    public ErrorDTO createErrorNetworkModule(String error) {
        return createErrorDTO(networkModuleTranslateError(error));
    }

    @Override
    public ErrorDTO createErrorACModule(String error) {
        return createErrorDTO(acModuleTranslateError(error));
    }

    @Override
    public ErrorDTO createErrorPlan(String error) {
        return createErrorDTO(planTranslateError(error));
    }

    @Override
    public ErrorDTO createErrorSipRu(String error) {
        return createErrorDTO(sipErrorTranslateRu(error));
    }

    @Override
    public ErrorDTO createErrorCardRu(String error) {
        return createErrorDTO(cardErrorTranslateRu(error));
    }

    @Override
    public String translateSip(String error) {
        return sipErrorTranslateEn(error);
    }

    @Override
    public String translateCard(String error) {
        return cardErrorTranslateEn(error);
    }
}
