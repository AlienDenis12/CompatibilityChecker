package com.aliendenis12;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginLoader {

    private static final String SPIGOT_JARS_RESOURCE_PATH = "/spigot-jars/";

    public static URLClassLoader getClassLoaderWithSpigotJars(String pluginPath, String version) throws Exception {
        // Load plugin JAR
        File pluginFile = new File(pluginPath);
        URL pluginJarUrl = pluginFile.toURI().toURL();

        // If version is 1.18.2 or later, extract inner JAR file from outer JAR
        if (isVersionGreaterOrEqual(version, "1.18.2")) {
            // Load outer JAR from resources
            String innerJarName = "spigot-api-" + version + ".jar";
            URL innerJarUrl = PluginLoader.class.getResource(SPIGOT_JARS_RESOURCE_PATH + innerJarName);
            if (innerJarUrl == null) {
                throw new IOException("Spigot JAR file not found for version: " + version);
            }

            // Extract outer JAR file from resources
            File innerJarFile = File.createTempFile("spigot-api-" + version, ".jar");
            innerJarFile.deleteOnExit();
            extractJarFromResource(SPIGOT_JARS_RESOURCE_PATH + innerJarName, innerJarFile);

            // Create URLClassLoader with Spigot and Plugin JARs
            return new URLClassLoader(new URL[]{innerJarFile.toURI().toURL(), pluginJarUrl}, PluginLoader.class.getClassLoader());
        } else {
            // Load outer JAR from resources
            String outerJarName = "spigot-" + version + ".jar";
            URL outerJarUrl = PluginLoader.class.getResource(SPIGOT_JARS_RESOURCE_PATH + outerJarName);
            if (outerJarUrl == null) {
                throw new IOException("Spigot JAR file not found for version: " + version);
            }

            // Extract outer JAR file from resources
            File outerJarFile = File.createTempFile("spigot-" + version, ".jar");
            outerJarFile.deleteOnExit();
            extractJarFromResource(SPIGOT_JARS_RESOURCE_PATH + outerJarName, outerJarFile);

            // For earlier versions, use the outer JAR directly
            return new URLClassLoader(new URL[]{outerJarFile.toURI().toURL(), pluginJarUrl}, PluginLoader.class.getClassLoader());
        }
    }

    private static boolean isVersionGreaterOrEqual(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        for (int i = 0; i < Math.min(parts1.length, parts2.length); i++) {
            int part1 = Integer.parseInt(parts1[i]);
            int part2 = Integer.parseInt(parts2[i]);
            if (part1 > part2) {
                return true;
            } else if (part1 < part2) {
                return false;
            }
        }

        return parts1.length >= parts2.length;
    }

    public static String getPluginName(String pluginPath) throws IOException {
        try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + Paths.get(pluginPath).toUri()), new HashMap<>())) {
            Path pluginYmlPath = fs.getPath("plugin.yml");
            try (InputStream input = Files.newInputStream(pluginYmlPath)) {
                Yaml yaml = new Yaml();
                Map<String, Object> yamlMap = yaml.load(input);
                return yamlMap.get("name").toString();
            }
        }
    }

    public static List<Class<?>> loadAllClasses(String pluginPath, URLClassLoader classLoader, String mode) throws Exception {
        String pluginName = getPluginName(pluginPath);
        Path tempDir = Files.createTempDirectory(pluginName);
        File pluginFile = new File(pluginPath);
        File tempFile = tempDir.resolve(pluginFile.getName()).toFile();
        Files.copy(pluginFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        List<Class<?>> classes = new ArrayList<>();

        try (JarFile jar = new JarFile(tempFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class") && !entry.getName().contains("$") && !entry.getName().startsWith("META-INF")) {
                    String className = entry.getName().replace("/", ".").replace(".class", "");
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        classes.add(clazz);
                    } catch (NoClassDefFoundError | ClassNotFoundException e) {
                        if (mode.equals("2")) {
                            System.out.println("Could not load class: " + className + " - " + e.getMessage());
                        }
                    }
                }
            }
        } finally {
            Files.deleteIfExists(tempFile.toPath());
            Files.delete(tempDir);
        }
        return classes;
    }

    private static void extractJarFromResource(String resourcePath, File outputFile) throws IOException {
        try (InputStream inputStream = PluginLoader.class.getResourceAsStream(resourcePath);
             OutputStream outputStream = Files.newOutputStream(outputFile.toPath())) {
            if (inputStream == null) {
                throw new IOException("The resource could not be found: " + resourcePath);
            }
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }
}

