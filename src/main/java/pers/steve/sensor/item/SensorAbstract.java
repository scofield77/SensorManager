package pers.steve.sensor.item;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.SynchronousQueue;

/**
 * @param <DataElementType>
 * @param <DataInterface>
 */
public abstract class SensorAbstract<DataElementType, DataInterface> {

    SensorAbstract(){
        setSensorName("AbstractSensor");
    }

    SensorAbstract(String sensorName){
        this.sensorName = sensorName;
    }

    protected boolean sensorRunningFlag = false; // flag for sensor thread which read data from hardware.
    protected boolean fileoutRunningFlag = false; // flag for file output thread which write data to file.
    protected boolean guiRunningFlag = false; // flag for gui thread which show the data online.

    protected java.util.concurrent.SynchronousQueue<DataElementType> dataQueue = new SynchronousQueue<DataElementType>();
    protected DataInterface dataInterfaceTool = null;
    protected HashSet<SensorDataListener<DataElementType>> listenerHashSet = null; // save listeners.

    private Thread sensorDataAccepter;
    private String sensorName;




    protected boolean addDataListener(SensorDataListener<DataElementType> listener) {
        try {
            if (listenerHashSet == null) {
                listenerHashSet = new HashSet<SensorDataListener<DataElementType>>();
            }
            listenerHashSet.add(listener);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected boolean removeDataListener(SensorDataListener<DataElementType> listener) {
        try {
            if (listenerHashSet != null) {
                listenerHashSet.remove(listener);
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }




    /**
     * notify all listeners in listenerHashSet.
     *
     * @param event
     * @return
     */
    public boolean notifyListeners(DataEvent<DataElementType> event) {
        try {
            if (null == listenerHashSet) {
                return false;
            }
            Iterator iter = listenerHashSet.iterator();
            while (iter.hasNext()) {
                SensorDataListener<DataElementType> listener =
                        (SensorDataListener<DataElementType>) iter.next();
                Runnable task = () -> {
                    listener.SensorDataEvent(event);
                };
                Thread t = new Thread(task);
                t.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean isSensorRunningFlag() {
        return sensorRunningFlag;
    }

    public void setSensorRunningFlag(boolean sensorRunningFlag) {
        this.sensorRunningFlag = sensorRunningFlag;
    }

    public boolean isFileoutRunningFlag() {
        return fileoutRunningFlag;
    }

    public void setFileoutRunningFlag(boolean fileoutRunningFlag) {
        this.fileoutRunningFlag = fileoutRunningFlag;
    }

    public boolean isGuiRunningFlag() {
        return guiRunningFlag;
    }

    public void setGuiRunningFlag(boolean guiRunningFlag) {
        this.guiRunningFlag = guiRunningFlag;
    }


    public abstract boolean setInterface(DataInterface data_interface_tool);

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }
}
