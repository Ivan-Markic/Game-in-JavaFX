package hr.markic.fireandwater.controllers;

import hr.markic.fireandwater.utils.SceneUtil;
import javafx.scene.control.Alert;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.List;

public class HomeScreenController{

    private String nameOfPackage = "hr.markic.fireandwater.";

    public void exitApplication()
    {
        SceneUtil.exitApp();
    }

    public void newGame(){
        SceneUtil.loadGame(true, false, false);
    }
    //EventHandler for bntPlayGame
    public void startGame(){
        SceneUtil.loadGame(false, true, false);
    }

    public void loadGame(){
        SceneUtil.loadGame(false, false, false);
    }

    public void replayOfGame(){
        SceneUtil.loadGame(true, false, true);
    }

    public void generateDocumentation(){

        StringBuilder builder = new StringBuilder();

        builder.append("<!DOCTYPE html>\n");
        builder.append("<html>\n");
        builder.append("<head>\n");
        builder.append("<title>Project documentation for FireAndWater"
                + "</title>\n");
        builder.append("</head>\n");
        builder.append("<body>\n");
        builder.append("<h1>List of packages in the project:</h1>\n");

        String packageLocation = ".\\src\\main\\java\\hr\\markic\\fireandwater";

        List<String> packages = new java.util.ArrayList<>(List.of(new File(packageLocation).list()));

        for (String nameOfClass : packages){
            if (!nameOfClass.endsWith(".java"))
                continue;
            documentClass(builder, "" , nameOfClass);
        }

        packages.removeIf(p -> p.endsWith("java"));

        for (String packageName : packages) {
            builder.append("<H2>");
            builder.append(packageName);
            builder.append("</H2>\n");

            builder.append("<h1>List of classes in the package:</h1>\n");

            String[] classes = new File(packageLocation + "\\"
                    + packageName).list();

            documentClasses(builder, packageName, classes);

        }

        builder.append("</body>\n");
        builder.append("</html>\n");

        try (FileWriter writer = new FileWriter("documentation.html")) {
            writer.write(builder.toString());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Successful documentation generation");
            alert.setHeaderText(
                    "Documentation with class list successfully generated!");
            alert.setContentText(
                    "File \"documentation.html\" successfully generated!");

            alert.showAndWait();

        } catch (IOException ex) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(
                    "Failed to generate documentation");
            alert.setHeaderText(
                    "An error occurred while generating the file"
                            + " s documentation!");
            alert.setContentText(
                    "File \"documentation.html\" failed to "
                            + "generate!");

            alert.showAndWait();
        }
    }

    private void documentClasses(StringBuilder builder, String localPackageName, String[] classes) {
        for (String className : classes) {

            if (className.endsWith(".java") == false) {
                continue;
            }

            documentClass(builder, localPackageName, className);
        }
    }

    private void documentClass(StringBuilder builder, String localPackageName, String className) {
        builder.append("<H3>");
        builder.append(className);
        builder.append("</H3>\n");


        className = localPackageName == "" ? nameOfPackage + className : nameOfPackage + localPackageName + "." + className;

        try {

            Class c = Class.forName(className.substring(0, className.lastIndexOf(".java")));

            Field[] fields = c.getDeclaredFields();

            builder.append("Variables:<br />");

            for (Field field : fields) {

                if (field.getName().equals("$VALUES")) {
                    continue;
                }

                int modifiers = field.getModifiers();

                fromModifierValue(modifiers, builder);

                builder.append(field.getType().getName());
                builder.append(" ");
                builder.append(field.getName());
                builder.append("<br />");
            }

            builder.append("Constructors:<br />");

            Constructor[] constructors = c.getConstructors();

            for (Constructor constructor : constructors) {

                int modifiers = constructor.getModifiers();

                fromModifierValue(modifiers, builder);

                builder.append(constructor.getName());
                builder.append("(");

                Parameter[] params = constructor.getParameters();

                for (Parameter param : params) {

                    getParameter(builder, modifiers, params, param);
                }
            }

            if(c.getConstructors().length > 0) {
                builder.append(")");
            }
            builder.append("<br />");

            builder.append("Methods:<br />");

            Method[] methods = c.getDeclaredMethods();

            for (Method method : methods) {

                if (method.getName().equals("values")) {
                    continue;
                }

                int modifiers = method.getModifiers();

                fromModifierValue(modifiers, builder);

                builder.append(method.getReturnType().getName());
                builder.append(" ");
                builder.append(method.getName());
                builder.append("(");

                Parameter[] params = method.getParameters();

                for (Parameter param : params) {

                    getParameter(builder, modifiers, params, param);
                }

                builder.append(")");
                builder.append("<br />");
            }

        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void getParameter(StringBuilder builder, int modifiers, Parameter[] params, Parameter param) {
        fromModifierValue(modifiers, builder);

        builder.append(param.getType().getName());
        builder.append(" ");
        builder.append(param.getName());
        if (params[params.length - 1].equals(param) == false) {
            builder.append(", ");
        }
    }

    private void fromModifierValue(int modifiers, final StringBuilder builder) {
        if (Modifier.isFinal(modifiers)) {
            builder.append("final ");
        }
        if (Modifier.isPrivate(modifiers)) {
            builder.append("private ");
        }
        if (Modifier.isProtected(modifiers)) {
            builder.append("protected ");
        }
        if (Modifier.isPublic(modifiers)) {
            builder.append("public ");
        }
        if (Modifier.isStatic(modifiers)) {
            builder.append("static ");
        }
        if (Modifier.isAbstract(modifiers)) {
            builder.append("abstract ");
        }
        if (Modifier.isSynchronized(modifiers)) {
            builder.append("synchronized ");
        }
    }

}
