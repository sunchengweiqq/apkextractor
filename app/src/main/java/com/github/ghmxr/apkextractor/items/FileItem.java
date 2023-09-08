package com.github.ghmxr.apkextractor.items;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public final class FileItem {
    private final File file;

    protected FileItem(String path) {
        this(new File(path));
    }

    protected FileItem(File file) {
        this.file = file;
    }

    public String getName() {
        return file.getName();
    }

    public boolean isDocumentFile() {
        return false;
    }

    public String getPath() {
        return file.getAbsolutePath();
    }

    public long length() {
        return file.length();
    }

    public InputStream getInputStream() throws Exception {
        return new FileInputStream(file);
    }

    public boolean isFileInstance() {
        return true;
    }

    public File getFile() {
        return file;
    }

    @NonNull
    @Override
    public String toString() {
        return file.getAbsolutePath();
    }
}
