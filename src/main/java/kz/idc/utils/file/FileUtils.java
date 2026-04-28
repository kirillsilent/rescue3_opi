package kz.idc.utils.file;

import kz.idc.dto.audio.AudioDTO;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface FileUtils {
    boolean isStorageExists();
    File createFileSettings();
    File getFileSettings();
    File clearDirectoryPlan();
    void saveFile(String filename, byte[] bytes, String path);
    void removeAudioFile(String filename);
    void removePlanFile(String filename);
    InputStream getPlan();
    String getAudioPath(String filename);
    InputStream getAudioStream(String fileName);
    List<AudioDTO> getAudioFileList();
    File createFileIncident();
    File getFileIncident();
    File clearDirectory(String path);
}
