package xyz.papermodloader.tree.paper.task;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.apache.commons.io.IOUtils;
import xyz.papermodloader.tree.paper.PaperConstants;
import xyz.papermodloader.tree.paper.PaperTree;

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
        ZipFile merged = new ZipFile(PaperConstants.DEOBF_MERGED_JAR_CACHE.get());
        ZipFile client = new ZipFile(PaperConstants.CLIENT_JAR_CACHE.get());
        File libraryJar = PaperConstants.MINECRAFT_LIBRARY_JAR_CACHE.get();
        if (!libraryJar.exists()) {
            libraryJar.createNewFile();
        }
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(libraryJar));
        Enumeration<? extends ZipEntry> entries = client.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.startsWith("assets") || name.endsWith(".png")|| name.endsWith(".xml")) {
                out.putNextEntry(entry);
                IOUtils.copy(client.getInputStream(entry), out);
                out.closeEntry();
            }
        }
        out.putNextEntry(new ZipEntry(PaperTree.INSTANCE.getExtension().minecraft + ".mappings"));
        FileUtils.copyFile(PaperConstants.MAPPINGS_FILE_CACHE.get(), out);
        out.closeEntry();
        out.putNextEntry(new ZipEntry(PaperTree.INSTANCE.getExtension().minecraft + "-obf.index"));
        FileUtils.copyFile(PaperConstants.OBF_INDEX_CACHE.get(), out);
        out.closeEntry();
        out.putNextEntry(new ZipEntry(PaperTree.INSTANCE.getExtension().minecraft + "-deobf.index"));
        FileUtils.copyFile(PaperConstants.DEOBF_INDEX_CACHE.get(), out);
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
