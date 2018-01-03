package pers.steve.sensor.item;

import java.sql.Timestamp;
import java.util.concurrent.ArrayBlockingQueue;

public class SensorJY901 extends SensorIMU<SerialInterface> {


    /**
     * Start sensor means set up a new thread for reading
     *
     * @param State
     * @return
     */
    @Override
    public boolean StartSensor(int State) {
        if (null == data_interface_tool) {
            System.out.print("data interface tool is null");
            return false;
        }
        if (sensor_running_flag == true) {
            System.out.print("Sensor is running.");
            return false;
        }
        sensor_running_flag = true;
        data_interface_tool.addListener(new SerialListener());
        data_interface_tool.StartInterface();


        return true;
    }

    /**
     * @param State
     * @return
     */
    @Override
    public boolean StopSensor(int State) {
        if (sensor_running_flag) {
            data_interface_tool.StopInterface();
            sensor_running_flag = false;
            return true;
        } else {
            System.out.print("Sensor is not running.");

            return true;
        }

    }


    @Override
    public boolean setInterface(SerialInterface data_interface_tool) {
        this.data_interface_tool = data_interface_tool;
        return true;
    }

    public class SerialListener implements SensorOriginalDataListener {
        protected ArrayBlockingQueue<Byte> byte_queue = new ArrayBlockingQueue<>(1000);

        /*
        Value for runnable data_process
         */
        protected IMUDataElement imu_data = null;
        double current_system_time = 0.0;
        byte[] buf = new byte[11];

        public SerialListener() {
            super();
            Thread t = new Thread(data_process);
            t.start();

        }


        /**
         * Process buf from serial port. And call notifyListeners.
         */
        protected Runnable data_process = () -> {
            while (true) {

                try {
                    int end_num = 11;
                    for (int i = 0; i < end_num; ++i) {
                        buf[i] = byte_queue.take();
                        if (i == 0 && buf[0] != 0x55) {
                            System.out.print("throw data");
                            throw new Exception("Throw data");
                        } else if (i == 1 && buf[1] == 0x50) {
                            current_system_time = ((double) System.currentTimeMillis()) / 1000.0;
                        }

                    }
//                    System.out.print("after copy data");
                    switch (buf[1]) {
                        case 0x50:
                            /// Time
                            if (imu_data != null) {
                                DataEvent<IMUDataElement> event = new DataEvent<IMUDataElement>(this);
                                event.setSensorData(imu_data);
                                notifyListeners(event);
                            }
                            imu_data = new IMUDataElement();
                            int year = 0;
                            year = (int) (buf[2] & 0xFF);
                            int mon = 0;
                            mon = (int) (buf[3] & 0xFF);
                            int day = 0;
                            day = (int) (buf[4] & 0xFF);
                            int hour = 0;
                            hour = (int) (buf[5] & 0xFF);
                            int min = 0;
                            min = (int) (buf[6] & 0xFF);
                            int sec = 0;
                            sec = (int) (buf[7] & 0xFF);
                            int ms = 0;
                            ms = (int) (((buf[8] & 0xFF) | ((buf[9] & 0xFF) << 8)));
                            Timestamp ts = Timestamp.valueOf(String.format("20%02d-%02d-%02d %02d:%02d:%02d", year, mon, day, hour, min, sec));
                            long time_int = ts.getTime();
                            imu_data.setTime_stamp((double) time_int + ((double) (ms)) / 1000.0);
                            imu_data.setSystem_time_stamp(current_system_time);

                            break;

                        case 0x51:
                            /// Acc and temperature
                            int ax_int = 0;
                            int ay_int = 0;
                            int az_int = 0;
                            int temperature_int = 0;
                            ax_int = (int) ((buf[2] & 0xFF) | ((buf[3] & 0xFF) << 8));
                            ay_int = (int) ((buf[4] & 0xFF) | ((buf[5] & 0xFF) << 8));
                            az_int = (int) ((buf[6] & 0xFF) | ((buf[7] & 0xFF) << 8));
                            temperature_int = (int) ((buf[8] & 0xFF) | ((buf[9] & 0xFF) << 8));

                            double[] acc_tmp = new double[3];
                            double[] t = new double[1];
                            acc_tmp[0] = ((double) ax_int) / 32768.0 * 16.0;
                            acc_tmp[1] = ((double) ay_int) / 32768.0 * 16.0;
                            acc_tmp[2] = ((double) az_int) / 32768.0 * 16.0;
                            imu_data.setAcc(acc_tmp);
                            t[0] = ((double) temperature_int) / 100.0;
                            imu_data.setTemp(t);

                            break;

                        case 0x52:
                            ///gyr
                            int gx_int = 0;
                            int gy_int = 0;
                            int gz_int = 0;
                            gx_int = (int) ((buf[2] & 0xFF) | ((buf[3] & 0xFF) << 8));
                            gy_int = (int) ((buf[4] & 0xFF) | ((buf[5] & 0xFF) << 8));
                            gz_int = (int) ((buf[6] & 0xFF) | ((buf[7] & 0xFF) << 8));

                            double tmp_gyr[] = new double[3];
                            tmp_gyr[0] = ((double) gx_int) / 32768.0 * 2000.0;
                            tmp_gyr[1] = ((double) gy_int) / 32768.0 * 2000.0;
                            tmp_gyr[2] = ((double) gz_int) / 32768.0 * 2000.0;
                            imu_data.setGyr(tmp_gyr);

                            break;

                        case 0x53:
                            ///angle
                            int angx_int = 0;
                            int angy_int = 0;
                            int angz_int = 0;
                            angx_int = (int) ((buf[2] & 0xFF) | ((buf[3] & 0xFF) << 8));
                            angy_int = (int) ((buf[4] & 0xFF) | ((buf[5] & 0xFF) << 8));
                            angz_int = (int) ((buf[6] & 0xFF) | ((buf[7] & 0xFF) << 8));

                            double tmp_ang[] = new double[3];
                            tmp_ang[0] = ((double) angx_int) / 32768.0 * 180.0;
                            tmp_ang[1] = ((double) angy_int) / 32768.0 * 180.0;
                            tmp_ang[2] = ((double) angz_int) / 32768.0 * 180.0;
                            imu_data.setAngle(tmp_ang);

                            break;

                        case 0x54:
                            /// mag
                            int magx_int = 0;
                            int magy_int = 0;
                            int magz_int = 0;
                            magx_int = (int) ((buf[2] & 0xFF) | ((buf[3] & 0xFF) << 8));
                            magy_int = (int) ((buf[4] & 0xFF) | ((buf[5] & 0xFF) << 8));
                            magz_int = (int) ((buf[6] & 0xFF) | ((buf[7] & 0xFF) << 8));

                            double[] tmp_mag = new double[3];
                            tmp_mag[0] = ((double) magx_int);
                            tmp_mag[1] = ((double) magy_int);
                            tmp_mag[2] = ((double) magz_int);
                            imu_data.setMag(tmp_mag);

                            break;

                        case 0x55:

                            break;
                        case 0x56:
                            ///press
                            int press_int = 0;
                            int heigh_int = 0;
                            press_int = (int) ((buf[2] & 0xFF) |
                                    ((buf[3] & 0xFF) << 8) |
                                    ((buf[4] & 0xFF) << 16) |
                                    ((buf[5] & 0xFF) << 24));
                            heigh_int = (int) ((buf[6] & 0xFF) |
                                    ((buf[7] & 0xFF) << 8) |
                                    ((buf[8] & 0xFF) << 16) |
                                    ((buf[9] & 0xFF) << 24));

                            double[] tmp_pre = new double[1];
                            tmp_pre[0] = (double) press_int;
                            imu_data.setPressure(tmp_pre);

                            double[] tmp_heigh = new double[1];
                            tmp_heigh[0] = (double) heigh_int;
                            imu_data.setHeigh(tmp_heigh);


                            break;
                    }
//                    System.out.print("after process");
                    //TODO:Adjust the sleep time auto.
                    Thread.sleep(0, 10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

            }


        };

        @Override
        public void SensorDataEvent(SensorOriginalDataEvent event) throws InterruptedException {
            byte[] bytes = event.get_bytes();
            for (byte tb : bytes) {
                byte_queue.put(tb);
            }
            System.out.println("byte queue size: " + byte_queue.size());


        }
    }


}
