package xyz.papermodloader.tree.task;

import com.google.common.io.ByteStreams;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import xyz.papermodloader.paper.launcher.Side;
import xyz.papermodloader.paper.launcher.SideDependent;
import xyz.papermodloader.tree.Constants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class MergeTask extends DefaultTask {
    @TaskAction
    public void doTask() {
        try {
            ZipFile client = new ZipFile(Constants.CLIENT_JAR_CACHE.get());
            ZipFile server = new ZipFile(Constants.SERVER_JAR_CACHE.get());

            List<ZipEntry> clientClasses = new LinkedList<>();
            List<ZipEntry> serverClasses = new LinkedList<>();

            Map<ZipEntry, byte[]> classes = new HashMap<>();
            Map<ZipEntry, ZipEntry> commonClasses = new HashMap<>();

            this.loadObjects(client, clientClasses);
            this.loadObjects(server, serverClasses);

            for (ZipEntry clientClass : clientClasses) {
                for (ZipEntry serverClass : serverClasses) {
                    if (clientClass.getName().equals(serverClass.getName())) {
                        commonClasses.put(clientClass, serverClass);
                        break;
                    }
                }
            }

            for (ZipEntry clientClass : clientClasses) {
                ZipEntry serverClass = commonClasses.get(clientClass);
                if (serverClass != null) {
                    serverClasses.remove(serverClass);
                }
                classes.put(new ZipEntry(clientClass.getName()), this.processClass(client, clientClass, server, serverClass, Side.CLIENT));
            }

            for (ZipEntry serverClass : serverClasses) {
                classes.put(new ZipEntry(serverClass.getName()), this.processClass(server, serverClass, null, null, Side.SERVER));
            }

            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(Constants.MERGED_JAR_CACHE.get()));

            for (Map.Entry<ZipEntry, byte[]> entry : classes.entrySet()) {
                out.putNextEntry(entry.getKey());
                out.write(entry.getValue());
                out.closeEntry();
            }

            client.close();
            server.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] processClass(ZipFile file, ZipEntry entry, ZipFile mergeFile, ZipEntry merge, Side side) throws IOException {
        ClassNode processed = this.getClassNode(file, entry);
        AnnotationNode sideAnnotation = this.createSideAnnotation(side);
        if (merge != null) {
            ClassNode mergeClassNode = this.getClassNode(mergeFile, merge);
            for (FieldNode field : processed.fields) {
                if (this.getEquivalent(field, mergeClassNode) == null) {
                    if (field.visibleAnnotations == null) {
                        field.visibleAnnotations = new ArrayList<>();
                    }
                    field.visibleAnnotations.add(sideAnnotation);
                }
            }
            for (FieldNode field : mergeClassNode.fields) {
                if (this.getEquivalent(field, processed) == null) {
                    if (field.visibleAnnotations == null) {
                        field.visibleAnnotations = new ArrayList<>();
                    }
                    field.visibleAnnotations.add(this.createSideAnnotation(side.invert()));
                    processed.fields.add(field);
                }
            }

            for (MethodNode method : processed.methods) {
                if (this.getEquivalent(method, mergeClassNode) == null) {
                    if (method.visibleAnnotations == null) {
                        method.visibleAnnotations = new ArrayList<>();
                    }
                    method.visibleAnnotations.add(sideAnnotation);
                }
            }
            for (MethodNode method : mergeClassNode.methods) {
                if (this.getEquivalent(method, processed) == null) {
                    if (method.visibleAnnotations == null) {
                        method.visibleAnnotations = new ArrayList<>();
                    }
                    method.visibleAnnotations.add(this.createSideAnnotation(side.invert()));
                    processed.methods.add(method);
                }
            }
        } else {
            if (processed.visibleAnnotations == null) {
                processed.visibleAnnotations = new ArrayList<>();
            }
            processed.visibleAnnotations.add(sideAnnotation);
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        processed.accept(writer);
        return writer.toByteArray();
    }

    private MethodNode getEquivalent(MethodNode field, ClassNode otherClass) {
        for (MethodNode otherMethod : otherClass.methods) {
            if (otherMethod.name.equals(field.name)) {
                return otherMethod;
            }
        }
        return null;
    }

    private FieldNode getEquivalent(FieldNode field, ClassNode otherClass) {
        for (FieldNode otherField : otherClass.fields) {
            if (otherField.name.equals(field.name)) {
                return otherField;
            }
        }
        return null;
    }

    private AnnotationNode createSideAnnotation(Side side) {
        AnnotationNode annotation = new AnnotationNode(Type.getDescriptor(SideDependent.class));
        annotation.values = new ArrayList<>();
        annotation.values.add("value");
        annotation.values.add(new String[]{Type.getDescriptor(Side.class), side.name()});
        return annotation;
    }

    private ClassNode getClassNode(ZipFile file, ZipEntry entry) throws IOException {
        ClassReader reader = new ClassReader(this.read(file, entry));
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        return node;
    }

    private byte[] read(ZipFile file, ZipEntry entry) throws IOException {
        return ByteStreams.toByteArray(file.getInputStream(entry));
    }

    private void loadObjects(ZipFile file, List<ZipEntry> classes) {
        Enumeration<? extends ZipEntry> entries = file.entries();
        ZipEntry entry;
        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            String name = entry.getName();
            if (!entry.isDirectory() && !name.equals("META-INF/MANIFEST.MF")) {
                if (name.endsWith(".class")) {
                    if (name.startsWith("net/minecraft") || !name.contains("/")) {
                        if (!classes.contains(entry)) {
                            classes.add(entry);
                        }
                    }
                }
            }
        }
    }
}
