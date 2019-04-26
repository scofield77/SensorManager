package steve.edu.cn;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pers.steve.sensor.gui.SensorImuViewerController;
import pers.steve.sensor.gui.SensorUWBViewerController;
import pers.steve.sensor.gui.SensorWriteFileInterface;
import pers.steve.sensor.gui.SmartPhoneSimpleController;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Pattern;

public class GUITest extends Application {

    protected HashSet<SensorWriteFileInterface> controllerHashSet = new HashSet<>();


    private MenuBar menuBar = new MenuBar();

    private DirectoryChooser dirChooser = new DirectoryChooser();

    private boolean saveRunningFlag = false;

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) {
        try {


            // root
            VBox root = new VBox();

            // Menu
            Menu menuFile = new Menu("File");
            Menu menuEdit = new Menu("Edit");

            MenuItem startSaveItem = new MenuItem("Start Save File");
            MenuItem stopSaveItem = new MenuItem("Stop Save File");

            menuFile.getItems().add(startSaveItem);
            menuFile.getItems().add(stopSaveItem);
            startSaveItem.setOnAction(event -> {
                if (saveRunningFlag) {
                    Alert a = new Alert(Alert.AlertType.ERROR,
                            "Save File threads is running.");
                    a.show();
                    return;
                }
                saveRunningFlag = true;
                // Start save file
                String dirString = dirChooser.showDialog(primaryStage).getAbsolutePath();
                dirChooser.setInitialDirectory(new File(dirString));
                File dirFile = new File(dirString);
                int max_number = -1;
                // obtain all dir name fulfill "\\d{4}"
                File[] filelistDigit = dirFile.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        if (Pattern.matches("\\d{4}", name)) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                // find the max index of dir.
                for (File tf : filelistDigit) {
                    String ts = tf.getName();
                    max_number = Math.max(max_number, Integer.valueOf(ts));
                }

                max_number += 1;

                File saveDirFile = new File(dirFile.getAbsolutePath(), String.format("%04d", max_number));
                saveDirFile.mkdir();
                System.out.println("saveDirFilePath"+saveDirFile.getAbsolutePath());


                for (SensorWriteFileInterface writer : controllerHashSet) {
                    if (writer.setDirectoryFile(saveDirFile)) {

                        writer.startWrite();
                    }
                }


            });

            stopSaveItem.setOnAction(event -> {
                if (!saveRunningFlag) {
                    new Alert(Alert.AlertType.WARNING,
                            "Saving threads isn't running.").show();
                    return;
                }
                saveRunningFlag = false;
                // Stop save file.
                for (SensorWriteFileInterface writer : controllerHashSet) {
                    writer.stopWrite();
                }

                new Alert(Alert.AlertType.WARNING,
                        "Saving thread stopped.").show();


            });

            menuBar.getMenus().addAll(menuFile, menuEdit);
            root.getChildren().add(menuBar);

            // file chooser
            dirChooser.setTitle("Choice Directory for save");


            //SensorPane
            ScrollPane scrollPane = new ScrollPane();
            GridPane sensorsPane = new GridPane();
            scrollPane.setContent(sensorsPane);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

            root.getChildren().add(scrollPane);

            // Load Pane and controller. Add controller to HashSet.
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().
                    getResource("./IMUViwerSimple.fxml"));
            FlowPane imuBox = loader.load();
            SensorImuViewerController imuViewerController = loader.
                    getController();

            FXMLLoader loader2 = new FXMLLoader(getClass().getClassLoader().
                    getResource("./IMUViwerSimple.fxml"));
            FlowPane imuBox2 = loader2.load();
            SensorImuViewerController imuViewerController2 = loader2.getController();

            FXMLLoader loader3 = new FXMLLoader(getClass().getClassLoader().
                    getResource("./IMUViwerSimple.fxml"));
            FlowPane imuBox3 = loader3.load();
            SensorImuViewerController imuViewerController3 = loader3.getController();

            FXMLLoader uwbLoader = new FXMLLoader(getClass().getClassLoader()
                    .getResource("./UWBViewerSimple.fxml"));
            FlowPane uwbBox = uwbLoader.load();
            SensorUWBViewerController uwbViewerController = uwbLoader.getController();


            FXMLLoader smartPhoneLoader = new FXMLLoader(getClass().getClassLoader()
                    .getResource("./SmartPhoneSimple.fxml"));
            FlowPane smartPhoneBox = smartPhoneLoader.load();
            SmartPhoneSimpleController smartPhoneSimpleController = smartPhoneLoader.getController();

            FXMLLoader smartPhoneLoader2 = new FXMLLoader(getClass().getClassLoader()
                    .getResource("./SmartPhoneSimple.fxml"));
            FlowPane smartPhoneBox2 = smartPhoneLoader2.load();
            SmartPhoneSimpleController smartPhoneSimpleController2 = smartPhoneLoader2.getController();



            sensorsPane.add(imuBox, 0, 0);
            sensorsPane.add(imuBox2, 0, 1);
            sensorsPane.add(imuBox3, 0, 2);
            sensorsPane.add(uwbBox, 0, 3);
            sensorsPane.add(smartPhoneBox, 0, 4);
            sensorsPane.add(smartPhoneBox2,0,5);

            controllerHashSet.add(imuViewerController);
            controllerHashSet.add(imuViewerController2);
            controllerHashSet.add(imuViewerController3);
            controllerHashSet.add(uwbViewerController);
            controllerHashSet.add(smartPhoneSimpleController);
            controllerHashSet.add(smartPhoneSimpleController2);


//            Button addImuButton = new Button("addNewImu");
//            addImuButton.setOnMouseClicked(event -> {
//                try {
//                    FlowPane timuBox2 = (FlowPane) FXMLLoader.load(getClass().getClassLoader().
//                            getResource("./IMUViwerSimple.fxml")
//                    );
//                    sensorsPane.add(timuBox2, 0, 3);
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            });
//            sensorsPane.add(addImuButton,0,1);


            // Add
            Scene scen = new Scene(root, 1800, 1000);

            primaryStage.setScene(scen);
            primaryStage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
