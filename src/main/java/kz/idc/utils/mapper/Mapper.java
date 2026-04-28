package kz.idc.utils.mapper;

import kz.idc.dto.IncidentDTO;
import kz.idc.dto.settings.SettingsDTO;

import java.io.File;

public interface Mapper {
    void writeJsonFile(File file, Object o);
    IncidentDTO readFileIncident(File file);
    SettingsDTO readFileSettings(File file);
}
