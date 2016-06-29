package xyz.papermodloader.tree.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xyz.papermodloader.tree.Constants;
import xyz.papermodloader.tree.Tree;
import xyz.papermodloader.tree.util.LauncherManifest;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class IDEAProjectTask extends DefaultTask {
    @TaskAction
    public void create() {
        try {
            String projectName = this.getProject().getName();

            File projectFile = new File(projectName + ".iml");
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(projectFile);

            Node component = this.getChild(document.getElementsByTagName("module").item(0), "component");
            Node content = this.getChild(component, "content");

            content.appendChild(createSourceFolder(document, "file://$MODULE_DIR$/src/main/java", false));
            content.appendChild(createSourceFolder(document, "file://$MODULE_DIR$/src/main/resources", true));

            component.appendChild(this.createLibrary(document, "jar://" + Constants.MINECRAFT_LIBRARY_JAR.get().getAbsolutePath() + "!/"));

            for (LauncherManifest.ManifestVersion.Version.Library library : Constants.VERSION.get().libraries) {
                if (library.isAllowed()) {
                    File file = new File(Constants.CACHE_DIRECTORY, "libraries" + File.separator + library.getFile());
                    component.appendChild(this.createLibrary(document, "jar://" + file.getAbsolutePath() + "!/"));
                }
            }

            this.save(projectFile, document);

            projectFile = new File(projectName + ".iws");
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(projectFile);

            Element runManager = null;

            NodeList components = document.getElementsByTagName("component");

            for (int i = 0; i < components.getLength(); i++) {
                Node node = components.item(i);
                if (node instanceof Element) {
                    Element element = (Element) node;
                    if (element.getAttribute("name").equals("RunManager")) {
                        runManager = element;
                        break;
                    }
                }
            }

            String workingDirectory = Tree.INSTANCE.getExtension().workingDirectory;
            String ideaWorkingDirectory = "file://$PROJECT_DIR$/" + workingDirectory;
            runManager.appendChild(this.createRunConfiguration(document, runManager, "xyz.papermodloader.launcher.PaperClient", projectName, "Minecraft Client", ideaWorkingDirectory));
            runManager.appendChild(this.createRunConfiguration(document, runManager, "xyz.papermodloader.launcher.PaperServer", projectName, "Minecraft Server", ideaWorkingDirectory));

            File workingDirectoryFile = new File(this.getProject().getProjectDir(), workingDirectory);

            if (!workingDirectoryFile.exists()) {
                workingDirectoryFile.mkdirs();
            }

            this.save(projectFile, document);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Element createLibrary(Document document, String url) {
        Element orderEntry = document.createElement("orderEntry");
        orderEntry.setAttribute("type", "module-library");
        Element library = document.createElement("library");
        Element classes = document.createElement("CLASSES");
        Element root = document.createElement("root");
        root.setAttribute("url", url);
        classes.appendChild(root);
        library.appendChild(classes);
        library.appendChild(document.createElement("JAVADOC"));
        library.appendChild(document.createElement("SOURCES"));
        orderEntry.appendChild(library);
        return orderEntry;
    }

    private void save(File projectFile, Document document) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Result output = new StreamResult(projectFile);
        Source input = new DOMSource(document);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(input, output);
    }

    private Element createRunConfiguration(Document document, Element base, String main, String projectName, String name, String runDirectory) {
        Element component = document.createElement("component");
        component.setAttribute("name", "ProjectRunConfigurationManager");
        base.appendChild(component);

        Element configuration = document.createElement("configuration");
        configuration.setAttribute("default", "false");
        configuration.setAttribute("name", name);
        configuration.setAttribute("type", "Application");
        configuration.setAttribute("factoryName", "Application");
        component.appendChild(configuration);

        Element module = document.createElement("module");
        module.setAttribute("name", projectName);
        configuration.appendChild(module);

        Element mainClass = document.createElement("option");
        mainClass.setAttribute("name", "MAIN_CLASS_NAME");
        mainClass.setAttribute("value", main);
        configuration.appendChild(mainClass);

        Element workingDirectory = document.createElement("option");
        workingDirectory.setAttribute("name", "WORKING_DIRECTORY");
        workingDirectory.setAttribute("value", runDirectory);
        configuration.appendChild(workingDirectory);

        return configuration;
    }

    private Node getChild(Node node, String name) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    private Element createSourceFolder(Document document, String url, boolean resource) {
        Element javaSource = document.createElement("sourceFolder");
        javaSource.setAttribute("url", url);
        javaSource.setAttribute("isTestSource", "false");
        if (resource) {
            javaSource.setAttribute("type", "java-resource");
        }
        return javaSource;
    }
}
