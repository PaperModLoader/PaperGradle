package xyz.papermodloader.tree.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import xyz.papermodloader.tree.Constants;

import java.io.*;
import java.net.URL;
import java.util.Map;

public class LauncherManifest {
    public Latest latest;
    public ManifestVersion[] versions;

    public static LauncherManifest getLauncherManifest() throws IOException {
        InputStream stream = new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json").openStream();
        LauncherManifest launcherManifest = new Gson().fromJson(new InputStreamReader(stream), LauncherManifest.class);
        stream.close();
        return launcherManifest;
    }

    public ManifestVersion getManifestVersion(String version) {
        for (ManifestVersion v : this.versions) {
            if (v.id.equals(version)) {
                return v;
            }
        }
        return null;
    }

    public class Latest {
        public String snapshot;
        public String release;
    }

    public static class ManifestVersion {
        public String id;
        public String type;
        public String time;
        public String releaseTime;
        public String url;

        public Version getVersion() throws IOException {
            InputStream stream = new URL(this.url).openStream();
            Version version = new Gson().fromJson(new InputStreamReader(stream), Version.class);
            stream.close();
            return version;
        }

        public static class Version {
            public AssetIndex assetIndex;
            public String assets;
            public Downloads downloads;
            public String id;
            public Library[] libraries;
            public Logging logging;
            public String mainClass;
            public String minecraftArguments;
            public int minimumLauncherVersion;
            public String releaseTime;
            public String time;
            public String type;

            public static class AssetIndex {
                public String id;
                public String sha1;
                public String size;
                public String url;
                public String totalSize;

                public Assets getAssets() throws IOException {
                    File file = new File(Constants.ASSET_DIRECTORY_CACHE, "indexes" + File.separator + this.id + ".json");
                    if (!file.exists() || !HashUtil.equalHash(file, this.sha1)) {
                        FileUtils.copyInputStreamToFile(new URL(this.url).openStream(), file);
                    }
                    return new Gson().fromJson(new FileReader(file), Assets.class);
                }

                public static class Assets {
                    public Map<String, AssetEntry> objects;

                    public class AssetEntry {
                        public String hash;
                        public int size;
                    }
                }
            }

            public static class Downloads {
                public Download client;
                public Download server;

                public class Download {
                    public String sha1;
                    public int size;
                    public String url;
                }
            }

            public static class Library {
                public String name;
                public JsonObject natives;
                public JsonObject downloads;

                public Rule[] rules;

                public String getURL() {
                    String path;
                    String[] parts = this.name.split(":", 3);
                    path = parts[0].replace(".", "/") + "/" + parts[1] + "/" + parts[2] + "/" + parts[1] + "-" + parts[2] + getClassifier() + ".jar";
                    return "https://libraries.minecraft.net/" + path;
                }

                public String getFile() {
                    String[] parts = this.name.split(":", 3);
                    return parts[0].replace(".", File.separator) + File.separator + parts[1] + File.separator + parts[2] + File.separator + parts[1] + "-" + parts[2] + getClassifier() + ".jar";
                }

                public String getSHA1() {
                    if (this.downloads == null) {
                        return "";
                    } else if (this.downloads.getAsJsonObject("artifact") == null) {
                        return "";
                    } else if (this.downloads.getAsJsonObject("artifact").get("sha1") == null) {
                        return "";
                    } else {
                        return this.downloads.getAsJsonObject("artifact").get("sha1").getAsString();
                    }
                }

                public String getClassifier() {
                    if (natives == null) {
                        return "";
                    } else {
                        return "-" + natives.get(OperatingSystem.getOS().replace("${arch}", OperatingSystem.getArch())).getAsString().replace("\"", "");
                    }
                }

                public boolean isAllowed() {
                    if (this.rules != null && this.rules.length > 0) {
                        for (Rule rule : this.rules) {
                            boolean allow = rule.action.equalsIgnoreCase("allow");
                            Rule.OS os = rule.os;
                            if (os != null && os.name != null) {
                                if (os.name.equalsIgnoreCase(OperatingSystem.getOS())) {
                                    return allow;
                                } else {
                                    return !allow;
                                }
                            } else if (!allow) {
                                return false;
                            }
                        }
                    }

                    return true;
                }
            }

            public static class Rule {
                public String action;
                public OS os;

                private class OS {
                    String name;
                }
            }
        }

        public static class Logging {
            public Client client;

            public static class Client {
                public File file;
                public String argument;
                public String type;

                public static class File {
                    public String id;
                    public String sha1;
                    public int size;
                    public String url;
                }
            }
        }
    }
}
