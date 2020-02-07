package com.smartx.util;

import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

import org.apache.log4j.Logger;

public class FileUtil {
    private static final Logger log = Logger.getLogger(FileUtil.class);
    public static final Set<PosixFilePermission> POSIX_SECURED_PERMISSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(OWNER_READ, OWNER_WRITE)));
    /**
     Delete a file or directory recursively.

     @param file
     */
    public static void recursiveDelete(File file) {
        try {
            Files.walk(file.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException e) {
            log.error("Failed to delete file: {}" + file + e);
        }
    }
    /**
     Check if the file's permission is secure.

     @param file
     @return
     @throws IOException
     */
    public static boolean isPosixPermissionSecured(File file) throws IOException {
        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(file.toPath());
        return permissions.containsAll(POSIX_SECURED_PERMISSIONS) && POSIX_SECURED_PERMISSIONS.containsAll(permissions);
    }
    private FileUtil() {
    }
}
