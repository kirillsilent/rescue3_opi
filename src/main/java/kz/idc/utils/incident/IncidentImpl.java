package kz.idc.utils.incident;

import kz.idc.dto.IncidentDTO;

public class IncidentImpl implements Incident {

    @Override
    public IncidentDTO initDefault() {
        return DefaultIncident.incident();
    }
}
