package xyz.papermodloader.papergradle.util;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import xyz.papermodloader.papergradle.Constants;

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

    public class ManifestVersion {
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

        public class Version {
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

            public class AssetIndex {
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

                public class Assets {
                    public Map<String, AssetEntry> objects;

                    public class AssetEntry {
                        public String hash;
                        public int size;
                    }
                }
            }

            public class Downloads {
                public Download client;
                public Download server;

                public class Download {
                    public String sha1;
                    public int size;
                    public String url;
                }
            }

            public class Library {
                public String name;
                public Downloads downloads;
                public Rule[] rules;
                public Natives natives;

                public class Downloads {
                    public Artifact artifact;

                    public class Artifact {
                        public int size;
                        public String sha1;
                        public String path;
                        public String url;
                    }
                }

                public class Rule {
                    public String action;
                    public OS os;

                    public class OS {
                        public String name;
                    }
                }

                public class Extract {
                    public String[] exclude;
                }

                public class Natives {
                    public String linux;
                    public String osx;
                    public String windows;
                }
            }

            public class Logging {
                public Client client;

                public class Client {
                    public File file;
                    public String argument;
                    public String type;

                    public class File {
                        public String id;
                        public String sha1;
                        public int size;
                        public String url;
                    }
                }
            }
        }
    }
}
