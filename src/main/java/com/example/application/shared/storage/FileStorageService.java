package com.example.application.shared.storage;

import java.io.InputStream;

public interface FileStorageService {

    /**
     * Sube un archivo al almacenamiento (S3 Floci).
     *
     * @param inputStream Flujo de datos del archivo
     * @param fileName    Nombre con el que se almacenará el archivo en el bucket
     * @param contentType Tipo MIME del archivo
     * @param size        Tamaño en bytes
     * @return Nombre del archivo almacenado
     */
    String uploadFile(InputStream inputStream, String fileName, String contentType, long size);

    /**
     * Obtiene la URL pública o accesible del archivo en el bucket.
     *
     * @param fileName Nombre del archivo
     * @return URL completa del archivo
     */
    String getFileUrl(String fileName);

    /**
     * Elimina un archivo del almacenamiento.
     *
     * @param fileName Nombre del archivo a eliminar
     */
    void deleteFile(String fileName);
}
