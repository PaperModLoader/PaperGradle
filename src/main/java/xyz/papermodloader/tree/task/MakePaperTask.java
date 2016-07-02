package xyz.papermodloader.tree.task;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import xyz.papermodloader.tree.Constants;
import xyz.papermodloader.tree.Tree;

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
            ZipFile buildZip = new ZipFile(Constants.RESULT_JAR.get());
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(temp));
            out.putNextEntry(new ZipEntry(Tree.INSTANCE.getExtension().minecraft + ".mappings"));
            FileUtils.copyFile(Constants.MAPPINGS_FILE_CACHE.get(), out);
            out.closeEntry();
            out.putNextEntry(new ZipEntry(Tree.INSTANCE.getExtension().minecraft + "-obf.index"));
            FileUtils.copyFile(Constants.OBF_INDEX_CACHE.get(), out);
            out.closeEntry();
            out.putNextEntry(new ZipEntry(Tree.INSTANCE.getExtension().minecraft + "-deobf.index"));
            FileUtils.copyFile(Constants.DEOBF_INDEX_CACHE.get(), out);
            out.closeEntry();
            out.putNextEntry(new ZipEntry("paper.hooks"));
            FileUtils.copyFile(Constants.TEMP_HOOKS_FILE.get(), out);
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
            Constants.RESULT_JAR.get().delete();
            temp.renameTo(Constants.RESULT_JAR.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
