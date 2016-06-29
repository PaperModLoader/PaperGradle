package xyz.papermodloader.tree.util;

public class OperatingSystem {
    public static String getOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "windows";
        } else if (osName.contains("mac")) {
            return "osx";
        } else {
            return "linux";
        }
    }

    public static String getArch() {
        if (System.getProperty("sun.arch.data.model").contains("64")) {
            return "64";
        } else {
            return "32";
        }
    }
}
