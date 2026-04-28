package kz.idc.utils.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import kz.idc.dto.IncidentDTO;
import kz.idc.dto.settings.SettingsDTO;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class MapperImpl implements Mapper {

    private final ObjectMapper mObjectMapper = new ObjectMapper();

    public MapperImpl() {
        mObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void writeJsonFile(File file, Object o) {
        try {
            // atomic write: write to temp then move
            Path target = file.toPath();
            Path tmp = Files.createTempFile(target.getParent(), "client", ".tmp");
            try {
                mObjectMapper.writeValue(tmp.toFile(), o);
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } finally {
                // ensure tmp removed if left
                try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
            }
        } catch (IOException e) {
            // пробрасываем как unchecked чтобы верхний код мог решить что делать
            throw new UncheckedIOException("Failed to write JSON file: " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public IncidentDTO readFileIncident(File file) {
        try {
            return mObjectMapper.readValue(file, IncidentDTO.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading IncidentDTO JSON: " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public SettingsDTO readFileSettings(File file) {
        try {
            // защитный чек: если файла нет или пуст — бросаем
            if (!file.exists() || file.length() == 0) {
                throw new IOException("Settings file not found or empty: " + file.getAbsolutePath());
            }
            return mObjectMapper.readValue(file, SettingsDTO.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading SettingsDTO JSON: " + file.getAbsolutePath(), e);
        }
    }
}

