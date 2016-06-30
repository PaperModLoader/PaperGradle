package xyz.papermodloader.tree.task;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.apache.commons.io.IOUtils;
import xyz.papermodloader.tree.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class MakeMinecraftTask extends DefaultTask {
    @TaskAction
    public void doTask() throws IOException {
        ZipFile merged = new ZipFile(Constants.DEOBF_MERGED_JAR_CACHE.get());
        ZipFile client = new ZipFile(Constants.CLIENT_JAR_CACHE.get());
        File libraryJar = Constants.MINECRAFT_LIBRARY_JAR_CACHE.get();
        if (!libraryJar.exists()) {
            libraryJar.createNewFile();
        }
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(libraryJar));
        Enumeration<? extends ZipEntry> entries = client.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().startsWith("assets")) {
                out.putNextEntry(entry);
                IOUtils.copy(client.getInputStream(entry), out);
                out.closeEntry();
            }
        }
        out.putNextEntry(new ZipEntry("1.10.2.mappings"));
        FileUtils.copyFile(Constants.MAPPINGS_FILE_CACHE.get(), out);
        out.closeEntry();
        entries = merged.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            out.putNextEntry(entry);
            IOUtils.copy(merged.getInputStream(entry), out);
            out.closeEntry();
        }
        out.close();
        merged.close();
    }
}
