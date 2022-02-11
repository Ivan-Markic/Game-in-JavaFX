package hr.markic.fireandwater.utils;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

public class FileUtils {

    private static final String LOAD = "Load";
    private static final String SAVE = "Save";

    public static File uploadFileDialog(Window owner, String...extensions) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
        Stream.of(extensions).forEach(e -> {
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(e.toUpperCase(), "*." + e)
            );
        });
        chooser.setTitle(LOAD);
        File file = chooser.showOpenDialog(owner);
        return file;
    }

    public static File saveFileDialog(Window owner, String...extensions) throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
        Stream.of(extensions).forEach(e -> {
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(e.toUpperCase(), "*." + e)
            );
        });
        chooser.setTitle(SAVE);
        File file = chooser.showSaveDialog(owner);
        if (file != null) {
            file.createNewFile();
        }
        return file;
    }
}
