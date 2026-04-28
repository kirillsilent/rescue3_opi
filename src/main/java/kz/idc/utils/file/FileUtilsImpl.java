package kz.idc.utils.file;

import kz.idc.Application;
import kz.idc.dto.audio.AudioDTO;
import kz.idc.utils.xxh.$GenerateXXH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static kz.idc.utils.file.FileExtensions.WAV;
import static kz.idc.utils.file.PathsEnum.*;

public final class FileUtilsImpl implements FileUtils {
    private static final String PATH;
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    static {
        PATH = getRootPath();
    }

    @Override
    public boolean isStorageExists() {
        File fileSettings = getFileSettings();
        File fileIncident = getFileIncident();
        return fileSettings.exists() && fileIncident.exists();
    }
        private File createDirectory(String dir){
        return new File(PATH
                + File.separator
                + ROOT_PATH.PATH
                + File.separator
                + dir);
    }

    @Override
    public File createFileSettings() {
        File directory = getDirectory(STORAGE.PATH);
        File settings = createFile(directory.getPath(), STORAGE.PATH, FileExtensions.JSON.EXTENSION);
        if (writeDirectory(directory)) {
            return writeEmptyFile(settings);
        }
        return settings;
    }
    @Override
    public File createFileIncident() {
        File directory = createDirectory(INCIDENT.PATH);
        File settings = createFile(directory.getPath(), INCIDENT.PATH, FileExtensions.JSON.EXTENSION);
        if(writeDirectory(directory)){
            return writeEmptyFile(settings);
        }
        return settings;
    }

    @Override
    public File clearDirectoryPlan() {
        return clearDirectory(PLAN.PATH);
    }

    @Override
    public void saveFile(String filename, byte[] bytes, String path) {
        File directory = null;
        if (path.equals(AUDIO.PATH)) {
            directory = getDirectory(AUDIO.PATH);
        } else if (path.equals(PLAN.PATH)) {
            directory = getDirectory(PLAN.PATH);
        }
        File file = createFile(Objects.requireNonNull(directory).getPath(), filename, "");
        writeDirectory(directory);
        fileWrite(file, bytes);
    }

    public File clearDirectory(String path) {
        File directory = getDirectory(path);
        if (directory.listFiles() != null) {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (!file.isDirectory()) {
                    if (file.delete()) {
                        System.out.println("Remove file: " + file.getName());
                    }
                }
            }
        }
        return directory;
    }
    @Override
    public File getFileIncident() {
        String fullPath = PATH
                + File.separator + ROOT_PATH.PATH
                + File.separator + INCIDENT.PATH
                + File.separator + PathsEnum.INCIDENT.PATH
                + FileExtensions.JSON.EXTENSION;
    
        System.out.println("Incident file path: " + fullPath);
        return new File(fullPath);
    }
    

    @Override
    public void removeAudioFile(String fileName) {
        File fileAudio = getDirectory(AUDIO.PATH + File.separator + fileName);
        deleteFile(fileAudio);
    }

    @Override
    public void removePlanFile(String filename) {
        File filePlan = getDirectory(PLAN.PATH + File.separator + filename);
        deleteFile(filePlan);
    }

    @Override
    public InputStream getPlan() {
        try {
            File planFile = firstFile(getDirectory(PLAN.PATH));
            if (planFile != null) {
                return new FileInputStream(planFile);
            }
        } catch (FileNotFoundException e) {
            log.warn("Plan file not found", e);
        }
        log.info("Plan file is not available in {}", getDirectory(PLAN.PATH).getPath());
        return null;
    }

    @Override
    public String getAudioPath(String fileName) {
        return PATH
                + File.separator
                + ROOT_PATH.PATH
                + File.separator
                + AUDIO.PATH + File.separator + fileName + WAV.EXTENSION;
    }

    @Override
    public InputStream getAudioStream(String fileName) {
        try {
            File file = getDirectory(AUDIO.PATH + File.separator + fileName + WAV.EXTENSION);
            if (file.exists()) {
                return new FileInputStream(file);
            } else {
                return new FileInputStream("");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public File getFileSettings() {
        return new File(PATH
                + File.separator + ROOT_PATH.PATH
                + File.separator + STORAGE.PATH
                + File.separator + PathsEnum.STORAGE.PATH
                + FileExtensions.JSON.EXTENSION);
    }


    @Override
    public List<AudioDTO> getAudioFileList() {
        File f = getDirectory(AUDIO.PATH);
        if (f.exists()) {
            List<AudioDTO> files = new ArrayList<>();
            for (String fileName : Objects.requireNonNull(f.list())) {
                try {
                    files.add(AudioDTO.create(fileName, $GenerateXXH.mk()
                            .getChecksumFromStream(new FileInputStream(
                                    f.getPath() + File.separator + fileName))));
                } catch (FileNotFoundException e) {
                    log.info("Audio file not downloaded");
                }
            }
            return files;
        }
        return new ArrayList<>();
    }

    private void deleteFile(File file) {
        if (file.exists()) {
            if (file.delete()) {
                log.info("File delete: " + file.getName());
            }
        }
    }

    private static String getRootPath() {
        // Runtime data should live next to the deployed JAR (e.g. /home/pi/rescue3.0/.rescue)
        return System.getProperty("user.dir");
    }

    private void fileWrite(File file, byte[] bytes) {
        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(bytes);
        } catch (Exception e) {
            log.info("Error write file");
        }
    }

    private boolean writeDirectory(File directory) {
        if (!directory.exists()) {
            log.info("Directory not exist");
            if (directory.mkdirs()) {
                log.info("Create folders directory");
                return true;
            }
        }
        return false;
    }

    private File getDirectory(String dir) {
        return new File(PATH
                + File.separator
                + ROOT_PATH.PATH
                + File.separator
                + dir);
    }

    private File createFile(String directory, String filename, String extension) {
        return new File(directory
                + File.separator
                + filename
                + extension);
    }

    private File firstFile(File directory) {
        if (directory == null || !directory.exists()) {
            return null;
        }
        File[] files = directory.listFiles(File::isFile);
        if (files == null || files.length == 0) {
            return null;
        }
        return files[0];
    }

    private File writeEmptyFile(File file) {
        try {
            if (file.createNewFile()) {
                return file;
            }
        } catch (Exception e) {
            log.info("Can't create file");
        }
        return null;
    }
}
