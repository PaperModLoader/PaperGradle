package xyz.papermodloader.tree.paper.task;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import xyz.papermodloader.tree.paper.PaperConstants;
import xyz.papermodloader.tree.paper.PaperTree;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class MakePaperTask extends DefaultTask {
    @TaskAction
    public void doTask() {
        try {
            String buildFileName = this.getProject().getName() + "-" + this.getProject().getVersion();
            File temp = new File(this.getProject().getRootDir(), "build" + File.separator + "libs" + File.separator + buildFileName + "-temp.jar");
            ZipFile buildZip = new ZipFile(PaperConstants.RESULT_JAR.get());
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(temp));
            out.putNextEntry(new ZipEntry(PaperTree.INSTANCE.getExtension().minecraft + ".json"));
            FileUtils.copyFile(PaperConstants.MAPPINGS_FILE_CACHE.get(), out);
            out.closeEntry();
            Enumeration<? extends ZipEntry> entries = buildZip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                out.putNextEntry(entry);
                IOUtils.copy(buildZip.getInputStream(entry), out);
                out.closeEntry();
            }
            out.close();
            buildZip.close();
            PaperConstants.RESULT_JAR.get().delete();
            temp.renameTo(PaperConstants.RESULT_JAR.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
