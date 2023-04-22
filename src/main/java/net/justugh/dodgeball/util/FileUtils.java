package net.justugh.dodgeball.util;

import net.justugh.dodgeball.Dodgeball;

import java.io.*;

public class FileUtils {

    public static void deleteDirectory(File directory) {
        if (directory == null) {
            return;
        }

        File[] files = directory.listFiles();

        if (files == null) {
            directory.delete();
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                deleteDirectory(file);
                continue;
            }

            file.delete();
        }
    }

    /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName ie.: "/SmartLibrary.dll"
     * @return The path to the exported resource
     */
    public static void exportResource(String resourceName) {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        String jarFolder;

        try {
            stream = Dodgeball.class.getResourceAsStream(resourceName);
            if (stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            jarFolder = new File(Dodgeball.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');
            resStreamOut = new FileOutputStream(jarFolder + resourceName);
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (Exception ex) {

        } finally {
            try {
                if(stream != null) stream.close();
                if(resStreamOut != null) resStreamOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
