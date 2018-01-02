package pers.steve.Sensors;


import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;

public class SerialInterface extends HardwareInteface {
    /**
     * How to use rxtx library in Ubuntu:
     * sudo apt-get install librxtx-java
     * <p>
     * sudo cp *rxtx*.so /usr/lib/jvm/java-8-oracle/jre/lib/amd64/
     */

    private String serialname = "/dev/ttyUSB0";
    private int nspeed = 460800;
    private int nbits = 8;
    private String nevent = "n";

    public String getSerialname() {
        return serialname;
    }

    public void setSerialname(String serialname) {
        this.serialname = serialname;
    }

    public int getNspeed() {
        return nspeed;
    }

    public void setNspeed(int nspeed) {
        this.nspeed = nspeed;
    }

    public int getNbits() {
        return nbits;
    }

    public void setNbits(int nbits) {
        this.nbits = nbits;
    }

    public String getNevent() {
        return nevent;
    }

    public void setNevent(String nevent) {
        this.nevent = nevent;
    }

    public int getNstop() {
        return nstop;
    }

    public void setNstop(int nstop) {
        this.nstop = nstop;
    }

    private int nstop = 1;

    private SerialPort serialPort_local = null;


    /**
     * Set and start a serial port parameters.
     * An serial prot openned here.
     *
     * @param serialname
     * @param nspeed
     * @param nbits
     * @param nevent
     * @param nstop
     */
    public boolean startSerialPara(
            String serialname,
            int nspeed,
            int nbits,
            String nevent,
            int nstop
    ) {

        try {

            this.serialPort_local = new SerialPort(serialname);

            if(!this.serialPort_local.isOpened())
            {

                this.serialPort_local.openPort();
            }
            this.serialPort_local.setParams(nspeed, nbits, nstop, SerialPort.PARITY_NONE);
            this.serialPort_local.addEventListener(new SerialEventListener());

        } catch (SerialPortException e) {
            e.printStackTrace();
        }
        return true;


    }

    @Override
    public boolean StartInterface() {
        if (startSerialPara(serialname, nspeed, nbits, nevent, nstop)) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean StopInterface() throws SerialPortException {
        serialPort_local.closePort();
        super.clearListeners();
        return false;
    }

    @Override
    public boolean RestartInterface() throws SerialPortException {
        serialPort_local.closePort();
        serialPort_local = null;
        startSerialPara(serialname, nspeed, nbits, nevent, nstop);
        return false;
    }


    public class SerialEventListener implements SerialPortEventListener {

        public byte[] bytes = null;

        public void serialEvent(SerialPortEvent serialPortEvent) {
            if (serialPortEvent.getEventType() == SerialPortEvent.RXCHAR) {
                try {


                    int buflength = serialPortEvent.getEventValue();

                    while (buflength > 0) {

                        bytes = new byte[buflength];
                        bytes = serialPort_local.readBytes(buflength);
//                        SensorDataEvent event = new SensorDataEvent(this,bytes);
                        notifyListeners(new SensorDataEvent(this, bytes));


                        buflength = 0;

                    }

                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}





