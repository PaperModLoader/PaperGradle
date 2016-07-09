package xyz.papermodloader.tree.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import xyz.papermodloader.tree.paper.PaperConstants;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class MinecraftJSON {
    public AssetIndex assetIndex;
    public Downloads downloads;
    public Library[] libraries;

    public class AssetIndex {
        public String id;
        public String sha1;
        public String url;

        public Assets getAssets() throws IOException {
            File file = new File(PaperConstants.ASSET_DIRECTORY_CACHE, "indexes" + File.separator + this.id + ".json");
            if (!file.exists() || !HashUtil.equalHash(file, this.sha1)) {
                FileUtils.copyInputStreamToFile(new URL(this.url).openStream(), file);
            }
            return new Gson().fromJson(new FileReader(file), Assets.class);
        }

        public class Assets {
            public Map<String, AssetEntry> objects;

            public class AssetEntry {
                public String hash;
            }
        }
    }

    public class Downloads {
        public Download client;
        public Download server;

        public class Download {
            public String sha1;
            public String url;
        }
    }

    public class Library {
        public String name;
        public JsonObject natives;
        public Rule[] rules;
        private Artifact artifact;

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

        public String getArtifactName() {
            if (this.artifact == null) {
                this.artifact = new Artifact(this.name);
            }
            return this.artifact.getArtifact(this.natives == null ? this.artifact.getClassifier() : this.natives.get(OperatingSystem.getOS()).getAsString());
        }

        private class Artifact {
            private String domain, name, version, classifier, ext;

            public Artifact(String name) {
                String[] splitedArtifact = name.split(":");
                int idx = splitedArtifact[splitedArtifact.length - 1].indexOf('@');
                if (idx != -1) {
                    this.ext = splitedArtifact[splitedArtifact.length - 1].substring(idx + 1);
                    splitedArtifact[splitedArtifact.length - 1] = splitedArtifact[splitedArtifact.length - 1].substring(0, idx);
                } else {
                    this.ext = "jar";
                }
                this.domain = splitedArtifact[0];
                this.name = splitedArtifact[1];
                this.version = splitedArtifact[2];
                this.classifier = splitedArtifact.length > 3 ? splitedArtifact[3] : null;
            }

            public String getArtifact(String classifier) {
                String ret = this.domain + ":" + this.name + ":" + this.version;
                if (classifier != null && classifier.indexOf('$') > -1) {
                    classifier = classifier.replace("${arch}", OperatingSystem.getArch());
                }
                if (classifier != null) {
                    ret += ":" + classifier;
                }
                if (!"jar".equals(this.ext)) {
                    ret += "@" + this.ext;
                }
                return ret;
            }

            public String getClassifier() {
                return this.classifier;
            }
        }

        public class Rule {
            public String action;
            public OS os;

            private class OS {
                String name;
            }
        }
    }
}
