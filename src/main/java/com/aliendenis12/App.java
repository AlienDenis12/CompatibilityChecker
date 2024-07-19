package com.aliendenis12;

import java.net.URLClassLoader;
import java.util.List;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

public class App {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String programVersion = "1.0.0";

        System.out.println("Created by AlienDenis12_YT");
        System.out.println("Version: " + programVersion);
        System.out.println();

        System.out.print("Enter the full path to the plugin JAR: ");
        String pluginPath = scanner.nextLine();

        System.out.println();
        System.out.println("Type of user");
        System.out.println();
        System.out.println("[1] Normal");
        System.out.println("[2] Detailed");
        System.out.println();

        System.out.print("Choose an option: ");
        String optionMode = scanner.nextLine();

        System.out.println();

        String[] spigotVersions = {
                "1.7.10", "1.8.8", "1.9.4", "1.10.2",
                "1.11.2", "1.12.2", "1.13.2",
                "1.14.4", "1.15.2", "1.16.5",
                "1.17.1", "1.18.2", "1.19.4",
                "1.20", "1.20.6", "1.21"
        };

        Map<String, Integer> errorCountPerVersion = new HashMap<>();

        for (String version : spigotVersions) {
            try {
                System.out.println("Checking compatibility for Spigot version: " + version);
                System.out.println();
                URLClassLoader spigotLoader = PluginLoader.getClassLoaderWithSpigotJars(pluginPath, version);
                List<Class<?>> classes = PluginLoader.loadAllClasses(pluginPath, spigotLoader, optionMode);
                boolean isCompatible = CompatibilityChecker.checkCompatibility(classes, optionMode);
                if (optionMode.equals("2")) {
                    System.out.println();
                }
                if (isCompatible) {
                    System.out.println("Confirmed compatibility for version: " + version);
                } else {
                    System.out.println("Plugin not compatible with version: " + version);
                    errorCountPerVersion.put(version, errorCountPerVersion.getOrDefault(version, 0) + 1);
                }
            } catch (Throwable t) {
                System.out.println("Error checking compatibility for version: " + version);
                if (optionMode.equals("2")) {
                    t.printStackTrace();
                }
                errorCountPerVersion.put(version, errorCountPerVersion.getOrDefault(version, 0) + 1);
            }
            System.out.println();
            System.out.println("----------------------------------------------------------------------------------");
            System.out.println();
        }

        for (Map.Entry<String, Integer> entry : errorCountPerVersion.entrySet()) {
            if (entry.getValue() > 0) {
                if (entry.getValue() == 1) {
                    System.out.println("The version " + entry.getKey() + " had " + entry.getValue() + " error.");
                }
                if (entry.getValue() > 2) {
                    System.out.println("The version " + entry.getKey() + " had " + entry.getValue() + " errors.");
                }
            }
        }

        System.out.println();
        System.out.println("Press Enter to exit...");
        new java.util.Scanner(System.in).nextLine();
    }
}
