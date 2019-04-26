package pers.steve.sensor.gui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.NumberBinding;
import javafx.beans.value.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jssc.SerialPortList;
import pers.steve.sensor.item.*;

import javax.print.attribute.standard.NumberUp;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.EventListener;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class SensorImuViewerController extends SensorWriteFileInterface implements Initializable {
    @FXML
    public FlowPane mainPane;

    @FXML
    public HBox allBox;

    @FXML
    public VBox paraBox;

    @FXML
    public Button startButton;

    @FXML
    public Button stopButton;

    @FXML
    public Label nameLabel;

    @FXML
    public CheckBox statuBox;

    @FXML
    public ChoiceBox<String> deviceChoice = null;

    protected ObservableList<String> deviceNameList =
            FXCollections.observableArrayList("Choice it to update");// device name list.

    protected String deviceNameString = ""; // device name , need be initialize.


    @FXML
    public ChoiceBox<Integer> speedChoice = null;

    protected ObservableList<Integer> speedList =
            FXCollections.observableArrayList(115200, 1192000, 460800, 9600); // NspeedList.

    protected int speedInt = 460800; // initial speed.


    @FXML
    ChoiceBox<String> nameChoice = null;
    protected ObservableList<String> nameList =
            FXCollections.observableArrayList("LEFT_FOOT", "RIGHT_FOOT",
                    "HEAD",
                    "LEFT_HAND", "RIGHT_HAND",
                    "LEFT_SHOULDER", "RIGHT_SHOULDER",
                    "Default IMU");

    protected String nameOfImu = "Default_IMU";

    @FXML
    public LineChart<Number, Number> accChart;

    /*
    Series and Data of acc.
     */
    private XYChart.Series<Number, Number> serial_acc_x = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> serial_acc_y = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> serial_acc_z = new XYChart.Series<Number, Number>();

    private ObservableList<XYChart.Data<Number, Number>> accXList =
            FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Number, Number>> accYList =
            FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Number, Number>> accZList =
            FXCollections.observableArrayList();


    @FXML
    public LineChart<Number, Number> gyrChart = null;


    /*
    Series and Data of gyr
     */
    private XYChart.Series<Number, Number> serial_gyr_x = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> serial_gyr_y = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> serial_gyr_z = new XYChart.Series<Number, Number>();


    private ObservableList<XYChart.Data<Number, Number>> gyrXList =
            FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Number, Number>> gyrYList =
            FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Number, Number>> gyrZList =
            FXCollections.observableArrayList();


    @FXML
    public LineChart<Number, Number> magChart = null;

    /*
    Series and Data of gyr
     */
    private XYChart.Series<Number, Number> serial_mag_x = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> serial_mag_y = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> serial_mag_z = new XYChart.Series<Number, Number>();


    private ObservableList<XYChart.Data<Number, Number>> magXList =
            FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Number, Number>> magYList =
            FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Number, Number>> magZList =
            FXCollections.observableArrayList();


    public SensorJY901 imuJY = new SensorJY901(); // IMU Sensor.

    public SerialAbstract serialInterface = new SerialAbstract();// serial reader.

    private File status_file;
    public FileWriter status_file_writer;

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  <tt>null</tt> if the location is not known.
     * @param resources The resources used to localize the root object, or <tt>null</tt> if
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        imuJY = new SensorJY901();

        imuJY.setGUIEventListener(new SensorDataListener<IMUDataElement>() {

            int dataMaxLength = 100; // max length of sensorData List.
            int counter = 0;
            int skip_num = 40; //

            @Override
            /**
             * Put sensor data into ObservableList.
             */
            public void SensorDataEvent(DataEvent<IMUDataElement> event) {

                counter++;
                if (counter % skip_num != 0) {

                    return;
                } else {
                    counter = 0;
                }
//              System.out.println(event.getSensorData().convertDatatoString());
                IMUDataElement sensorData = event.getSensorData();

                double acc_time = new Double(sensorData.getSystem_time_stamp());
                double acc_x = new Double(sensorData.getAcc()[0]);
                double acc_y = new Double(sensorData.getAcc()[1]);
                double acc_z = new Double(sensorData.getAcc()[2]);
                double gyr_x = new Double(sensorData.getGyr()[0]);
                double gyr_y = new Double(sensorData.getGyr()[1]);
                double gyr_z = new Double(sensorData.getGyr()[2]);
                double mag_x = new Double(sensorData.getMag()[0]);
                double mag_y = new Double(sensorData.getMag()[1]);
                double mag_z = new Double(sensorData.getMag()[2]);
//                System.out.println(String.format);


                Platform.runLater(() -> {
                    accXList.add(new XYChart.Data<Number, Number>(acc_time, acc_x));
                    accYList.add(new XYChart.Data<Number, Number>(acc_time, acc_y));
                    accZList.add(new XYChart.Data<Number, Number>(acc_time, acc_z));
                    gyrXList.add(new XYChart.Data<Number, Number>(acc_time, gyr_x));
                    gyrYList.add(new XYChart.Data<Number, Number>(acc_time, gyr_y));
                    gyrZList.add(new XYChart.Data<Number, Number>(acc_time, gyr_z));
                    magXList.add(new XYChart.Data<Number, Number>(acc_time, mag_x));
                    magYList.add(new XYChart.Data<Number, Number>(acc_time, mag_y));
                    magZList.add(new XYChart.Data<Number, Number>(acc_time, mag_z));

                    if (accXList.size() > dataMaxLength) {
                        accXList.remove(0, 10);
                        accYList.remove(0, 10);
                        accZList.remove(0, 10);
                        gyrXList.remove(0, 10);
                        gyrYList.remove(0, 10);
                        gyrZList.remove(0, 10);
                        magXList.remove(0, 10);
                        magYList.remove(0, 10);
                        magZList.remove(0, 10);
                    }


                });
            }
        });


        /**
         * Set up Chart Fisrt
         */
        serial_acc_x.setName("Acc_X");
        serial_acc_y.setName("Acc_Y");
        serial_acc_z.setName("Acc_Z");
        serial_acc_x.dataProperty().set(accXList);
        serial_acc_y.dataProperty().set(accYList);
        serial_acc_z.dataProperty().set(accZList);


        accChart.getData().addAll(serial_acc_x,
                serial_acc_y,
                serial_acc_z);
        accChart.setAnimated(false); // speed up.
        accChart.setCreateSymbols(false); // without mark , only line.
        accChart.setTitle("Acc");

        //------
        serial_gyr_x.setName("Gyr_X");
        serial_gyr_y.setName("Gyr_Y");
        serial_gyr_z.setName("Gyr_Z");
        serial_gyr_x.dataProperty().set(gyrXList);
        serial_gyr_y.dataProperty().set(gyrYList);
        serial_gyr_z.dataProperty().set(gyrZList);


        gyrChart.getData().addAll(serial_gyr_x,
                serial_gyr_y,
                serial_gyr_z);
        gyrChart.setAnimated(false); // speed up.
        gyrChart.setCreateSymbols(false); // without mark , only line.
        gyrChart.setTitle("Gyr");

        //------
        serial_mag_x.setName("Mag_X");
        serial_mag_y.setName("Mag_Y");
        serial_mag_z.setName("Mag_Z");
        serial_mag_x.dataProperty().set(magXList);
        serial_mag_y.dataProperty().set(magYList);
        serial_mag_z.dataProperty().set(magZList);


        magChart.getData().addAll(serial_mag_x,
                serial_mag_y,
                serial_mag_z);
        magChart.setAnimated(false); // speed up.
        magChart.setCreateSymbols(false); // without mark , only line.
        magChart.setTitle("Mag");


//        mainPane.setMinWidth(1800);

        mainPane.setMinWidth((paraBox.widthProperty().add(accChart.widthProperty().add(gyrChart.widthProperty().add(magChart.widthProperty())))).doubleValue());
        /**
         *  Set up ChoiceBOX
         */

        deviceChoice.itemsProperty().set(deviceNameList);

        deviceChoice.setOnMouseClicked(event -> {
//            System.out.println(event);
            SerialPortList portList = new SerialPortList();
            String[] name_list = portList.getPortNames();
            deviceNameList.clear();
            for (String s : name_list) {
                System.out.println(s);
                deviceNameList.add(s);
            }
            portList = null;
        });

        deviceChoice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                deviceNameString = newValue;
                System.out.println("DeviceName change to :" + deviceNameString);
            }
        });


        speedChoice.itemsProperty().set(speedList);


        speedChoice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                speedInt = newValue.intValue();
            }
        });


        nameChoice.itemsProperty().set(nameList);

        nameChoice.getSelectionModel().
                selectedItemProperty().
                addListener(new ChangeListener<String>() {
                                @Override
                                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                                    nameOfImu = newValue;
                                    if (imuJY != null) {
                                        imuJY.setSensorName(nameOfImu);
                                    }
                                    Platform.runLater(() -> {
                                        nameLabel.setText(nameOfImu);
                                    });

                                }
                            }

                );


        /**
         * Button
         */
        // Start sensor and GUI thread
        startButton.setOnAction(event -> {
            try {
                serialInterface = null;
                serialInterface = new SerialAbstract();
                if (deviceNameString.length() > 1 && speedInt >= 9600) {
                    // device name and speed determined.
                    serialInterface.setSerialname(deviceNameString);
                    serialInterface.setNspeed(speedInt);

                    imuJY.setInterface(serialInterface);
                    imuJY.startSensor(0);
//                    imuJY.startFileOutput(0);
                    accXList.clear();
                    accYList.clear();
                    accZList.clear();
                    gyrXList.clear();
                    gyrYList.clear();
                    gyrZList.clear();
                    magXList.clear();
                    magYList.clear();
                    magZList.clear();
                    imuJY.startGUIOutput(0);

                } else {
                    // device name and speed error.
                    Alert a = new Alert(Alert.AlertType.WARNING);
                    a.setTitle("carefully select serial port parameters");
                    a.setHeaderText("Error in select port parameters");
                    a.setContentText(String.format("device:%s \nspeed:%d \n are unacceptable!",
                            deviceNameString, speedInt));
                    a.show();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });


        // stop sensor and GUI thread
        stopButton.setOnAction(event -> {
            // Insure
            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to stop sensor?\n" +
                            "Stop this process will cause a unusual\n situation of mouse!");
            a.showAndWait().ifPresent(response -> {

                if (response == ButtonType.OK) {
                    imuJY.stopSensor(0);
//                    System.out.print("button type is: " + response.toString());
//                    imuJY.stopFileOutput(0);
                    imuJY.stopGUIOutput(0);

                } else {
//                    System.out.print("button type is: " + response.toString());
                }
            });
        });

       statuBox.setOnAction(event -> {
           double time = ((double) System.currentTimeMillis()/1000.0);

           double sstatus = 0;
           if(statuBox.isSelected()){
               System.out.println("check box selected");
               sstatus = 100;
           }else{
               System.out.println("check box un selected");
               sstatus = -100;
           }
           try{
                System.out.println(String.format("%15.03f", time)+","+ Double.toString(sstatus)+"\n");
               status_file_writer.write(String.format("%15.03f", time)+","+ Double.toString(sstatus)+"\n");
               status_file_writer.flush();
           }catch (IOException e){
               e.printStackTrace();

           }
           System.out.println("check box clicked");
       });



    }


    /**
     * @param dirFile File of directory name.
     * @return
     */
    @Override
    public boolean setDirectoryFile(File dirFile) {
        if (nameOfImu.indexOf("Default") < 0) {
            File f = new File(dirFile, nameOfImu + ".data");
            status_file = new File(dirFile, nameOfImu + "statu.data");
            try {


                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }


            imuJY.setDataSaveFile(f);

            return true;
        } else {
            return false;
        }
    }

    /**
     * Start write to files.
     *
     * @return
     */
    @Override
    public boolean startWrite() {
        try{
            status_file_writer = new FileWriter(status_file.toString());

        }catch(IOException e){
            e.printStackTrace();
        }
        return imuJY.startFileOutput(1);
    }

    /**
     * Stop write to files.
     *
     * @return
     */
    @Override
    public boolean stopWrite() {
        try{
            status_file_writer.flush();
            status_file_writer.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        return imuJY.stopFileOutput(0);
    }
}

