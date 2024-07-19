package com.aliendenis12;

import java.lang.reflect.Method;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CompatibilityChecker {

    public static boolean checkCompatibility(List<Class<?>> classes, String mode) {
        boolean isCompatible = true;
        for (Class<?> clazz : classes) {
            try {
                String className = clazz.getName();
                if (!className.startsWith("META-INF.versions")) {
                    isCompatible = checkClassCompatibility(clazz, mode);
                    if (!isCompatible) {
                        break;
                    }
                }
            } catch (Throwable t) {
                if (mode.equals("2")) {
                    System.out.println("Error checking class compatibility: " + clazz.getName());
                    t.printStackTrace();
                }
                isCompatible = false;
                break;
            }
        }
        return isCompatible;
    }

    private static boolean checkClassCompatibility(Class<?> clazz, String mode) {
        boolean isCompatible = true;
        try {
            String className = clazz.getName();
            if (!className.startsWith("META-INF")) {
                checkImplementedInterfaces(clazz, mode);

                isCompatible = checkMethodsAndFields(clazz, mode);
            }
        } catch (Exception e) {
            if (mode.equals("2")) {
                System.out.println("Error checking class compatibility: " + clazz.getName());
                e.printStackTrace();
            }
            isCompatible = false;
        }
        return isCompatible;
    }

    private static void checkImplementedInterfaces(Class<?> clazz, String mode) {
        try {
            String className1 = clazz.getName();
            if (!className1.startsWith("META-INF")) {
                for (Class<?> iface : clazz.getInterfaces()) {
                    String interfaceName = iface.getName();
                    if (interfaceName.startsWith("org.bukkit")) {
                        if (mode.equals("2")) {
                            System.out.println("The class " + clazz.getName() + " implements the Bukkit interface: " + interfaceName);
                        }
                    }
                }

                Class<?> superclass = clazz.getSuperclass();
                while (superclass != null) {
                    String className = superclass.getName();
                    if (className.startsWith("org.bukkit")) {
                        if (mode.equals("2")) {
                            System.out.println("The class " + clazz.getName() + " extends a Bukkit class: " + className);
                        }
                    }
                    superclass = superclass.getSuperclass();
                }

            }
        } catch (Exception e) {
            if (mode.equals("2")) {
                System.out.println("Error verifying class interfaces: " + clazz.getName());
                e.printStackTrace();
            }
        }
    }

    private static boolean checkMethodsAndFields(Class<?> clazz, String mode) {
        try {
            String className = clazz.getName();
            if (!className.startsWith("META-INF")) {
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (mode.equals("2")) {
                        System.out.println("Method found in class " + clazz.getName() + ": " + method.getName());
                    }
                }
            }
        } catch (Throwable t) {
            if (mode.equals("2")) {
                System.out.println("\033[38;2;255;100;100m" + "Error verifying methods and fields of the class: " + clazz.getName() + "\033[0m");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                PrintStream old = System.err;
                System.setErr(ps);
                t.printStackTrace();
                System.err.flush();
                System.setErr(old);

                System.out.println("\033[38;2;255;100;100m" + baos + "\033[0m");
            }
            return false;
        }
        return true;
    }
}
