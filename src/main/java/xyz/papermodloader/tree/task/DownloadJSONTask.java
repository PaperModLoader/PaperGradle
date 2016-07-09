package xyz.papermodloader.tree.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import xyz.papermodloader.tree.util.Initializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class DownloadJSONTask extends DefaultTask {
    private Initializer initializer;
    private String url;
    private String version;
    private File json;

    @TaskAction
    public void doTask() throws IOException {
        this.initializer.initialize();
        if (!this.json.exists()) {
            JsonParser parser = new JsonParser();
            JsonObject manifest = parser.parse(new InputStreamReader(new URL(this.url).openStream())).getAsJsonObject();
            JsonArray versions = manifest.get("versions").getAsJsonArray();
            for (JsonElement element : versions) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.get("id").getAsString().equals(this.version)) {
                    FileUtils.copyInputStreamToFile(new URL(obj.get("url").getAsString()).openStream(), this.json);
                    break;
                }
            }
            if (!this.json.exists()) {
                throw new RuntimeException("Unable to find Minecraft version " + this.version);
            }
        }
    }

    public void setInitializer(Initializer initializer) {
        this.initializer = initializer;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setJSON(File json) {
        this.json = json;
    }
}
