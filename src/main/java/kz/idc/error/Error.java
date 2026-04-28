package kz.idc.error;

import kz.idc.dto.error.ErrorDTO;

public interface Error {
    ErrorDTO createError(String error);
    ErrorDTO createErrorNetworkModule(String error);
    ErrorDTO createErrorACModule(String error);
    ErrorDTO createErrorPlan(String error);
    ErrorDTO createErrorSipRu(String error);
    ErrorDTO createErrorCardRu(String error);
    String translateSip(String error);
    String translateCard(String error);
}
