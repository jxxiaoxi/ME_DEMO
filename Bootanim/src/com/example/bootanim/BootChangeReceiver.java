package com.example.bootanim;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Toast;

public class BootChangeReceiver extends BroadcastReceiver {
    static {
        System.loadLibrary("fileopt");
    }
    @Override
    public void onReceive(Context arg0, Intent arg1) {
        // TODO Auto-generated method stub
        if (arg1.getAction().equals("persist.sys.bootanimation")) {
            int what = arg1.getIntExtra("what", 0);
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                    .getDefaultAdapter();
            if(!bluetoothAdapter.isEnabled()){
                bluetoothAdapter.enable();
            }
            if (what == 0) {

                // SystemProperties.set("persist.sys.bootanimation", "0");
                copyFile("/system/vendor/costom/build.prop");
                copyMedia("/system/media/bootanimation0.zip",
                        "/system/media/bootaudio0.mp3");
                copyConfFile("/system/vendor/costom/custom.conf");
                bluetoothAdapter.setName("K6");
                Toast.makeText(arg0, "SWitch to default", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            } else if (what == 1) {

                // SystemProperties.set("persist.sys.bootanimation", "1");
                copyFile("/system/vendor/costom/build1.prop");
                copyMedia("/system/media/bootanimation1.zip",
                        "/system/media/bootaudio1.mp3");
                copyConfFile("/system/vendor/costom/custom1.conf");
                bluetoothAdapter.setName("android");
                Toast.makeText(arg0, "SWitch to and", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            } else if (what == 2) {
                // SystemProperties.set("persist.sys.bootanimation", "2");
                copyFile("/system/vendor/costom/build2.prop");
                copyMedia("/system/media/bootanimation2.zip",
                        "/system/media/bootaudio2.mp3");
                copyConfFile("/system/vendor/costom/custom2.conf");
                bluetoothAdapter.setName("asus");
                Toast.makeText(arg0, "SWitch to asus", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            } else if (what == 3) {
                // SystemProperties.set("persist.sys.bootanimation", "3");
                copyFile("/system/vendor/costom/build3.prop");
                copyMedia("/system/media/bootanimation3.zip",
                        "/system/media/bootaudio3.mp3");
                copyConfFile("/system/vendor/costom/custom3.conf");
                bluetoothAdapter.setName("coolpad");
                Toast.makeText(arg0, "SWitch to coolpad", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            } else if (what == 4) {
                // SystemProperties.set("persist.sys.bootanimation", "4");
                copyFile("/system/vendor/costom/build4.prop");
                copyMedia("/system/media/bootanimation4.zip",
                        "/system/media/bootaudio4.mp3");
                copyConfFile("/system/vendor/costom/custom4.conf");
                bluetoothAdapter.setName("doov");
                Toast.makeText(arg0, "SWitch to doov", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            } else if (what == 5) {
                // SystemProperties.set("persist.sys.bootanimation", "5");
                copyFile("/system/vendor/costom/build5.prop");
                copyMedia("/system/media/bootanimation5.zip",
                        "/system/media/bootaudio5.mp3");
                copyConfFile("/system/vendor/costom/custom5.conf");
                bluetoothAdapter.setName("honor");
                Toast.makeText(arg0, "SWitch to honor", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            } else if (what == 6) {
                // SystemProperties.set("persist.sys.bootanimation", "6");
                copyFile("/system/vendor/costom/build6.prop");
                copyMedia("/system/media/bootanimation6.zip",
                        "/system/media/bootaudio6.mp3");
                copyConfFile("/system/vendor/costom/custom6.conf");
                bluetoothAdapter.setName("htc");
                Toast.makeText(arg0, "SWitch to htc", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            } else if (what == 7) {
                // SystemProperties.set("persist.sys.bootanimation", "7");
                copyFile("/system/vendor/costom/build7.prop");
                copyMedia("/system/media/bootanimation7.zip",
                        "/system/media/bootaudio7.mp3");
                copyConfFile("/system/vendor/costom/custom7.conf");
                bluetoothAdapter.setName("huawei");
                Toast.makeText(arg0, "SWitch to huawei", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            } else if (what == 8) {
                // SystemProperties.set("persist.sys.bootanimation", "8");
                copyFile("/system/vendor/costom/build8.prop");
                copyMedia("/system/media/bootanimation8.zip",
                        "/system/media/bootaudio8.mp3");
                copyConfFile("/system/vendor/costom/custom8.conf");
                bluetoothAdapter.setName("jiayu");
                Toast.makeText(arg0, "SWitch to jiayu", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            } else if (what == 9) {
                // SystemProperties.set("persist.sys.bootanimation", "9");
                copyFile("/system/vendor/costom/build9.prop");
                copyMedia("/system/media/bootanimation9.zip",
                        "/system/media/bootaudio9.mp3");
                copyConfFile("/system/vendor/costom/custom9.conf");
                bluetoothAdapter.setName("lenovo");
                Toast.makeText(arg0, "SWitch to lenovo", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            } else if (what == 10) {
                // SystemProperties.set("persist.sys.bootanimation", "10");
                copyFile("/system/vendor/costom/build10.prop");
                copyMedia("/system/media/bootanimation10.zip",
                        "/system/media/bootaudio10.mp3");
                copyConfFile("/system/vendor/costom/custom10.conf");
                bluetoothAdapter.setName("liantong4g");
                Toast.makeText(arg0, "SWitch to liantong4g", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            } else if (what == 11) {
                // SystemProperties.set("persist.sys.bootanimation", "11");
                copyFile("/system/vendor/costom/build11.prop");
                copyMedia("/system/media/bootanimation11.zip",
                        "/system/media/bootaudio11.mp3");
                copyConfFile("/system/vendor/costom/custom11.conf");
                bluetoothAdapter.setName("oppo");
                Toast.makeText(arg0, "SWitch to oppo", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            } else if (what == 12) {
                // SystemProperties.set("persist.sys.bootanimation", "12");
                copyFile("/system/vendor/costom/build12.prop");
                copyMedia("/system/media/bootanimation12.zip",
                        "/system/media/bootaudio12.mp3");
                copyConfFile("/system/vendor/costom/custom12.conf");
                bluetoothAdapter.setName("sony");
                Toast.makeText(arg0, "SWitch to sony", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            } else if (what == 13) {
                // SystemProperties.set("persist.sys.bootanimation", "13");
                copyFile("/system/vendor/costom/build13.prop");
                copyMedia("/system/media/bootanimation13.zip",
                        "/system/media/bootaudio13.mp3");
                copyConfFile("/system/vendor/costom/custom13.conf");
                bluetoothAdapter.setName("vivo");
                Toast.makeText(arg0, "SWitch to vivo", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            } else if (what == 14) {
                // SystemProperties.set("persist.sys.bootanimation", "14");
                copyFile("/system/vendor/costom/build14.prop");
                copyMedia("/system/media/bootanimation14.zip",
                        "/system/media/bootaudio14.mp3");
                copyConfFile("/system/vendor/costom/custom14.conf");
                bluetoothAdapter.setName("MI");
                Toast.makeText(arg0, "SWitch to xiaomi", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            } else if (what == 15) {
                // SystemProperties.set("persist.sys.bootanimation", "15");
                copyFile("/system/vendor/costom/build15.prop");
                copyMedia("/system/media/bootanimation15.zip",
                        "/system/media/bootaudio15.mp3");
                copyConfFile("/system/vendor/costom/custom15.conf");
                bluetoothAdapter.setName("yidong4g");
                Toast.makeText(arg0, "SWitch to yidong4g", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            } else if (what == 16) {
                // SystemProperties.set("persist.sys.bootanimation", "16");
                copyFile("/system/vendor/costom/build16.prop");
                copyMedia("/system/media/bootanimation16.zip",
                        "/system/media/bootaudio16.mp3");
                copyConfFile("/system/vendor/costom/custom16.conf");
                bluetoothAdapter.setName("zte");
                Toast.makeText(arg0, "SWitch to zte", Toast.LENGTH_SHORT)
                        .show();
                setReset(arg0);
            }
        }
    }

    public void copyFile(String newFile) {
        String oldFile = "/system/build.prop";
        SystemProperties.set("ro.secure", "0");
        doCommand("mount -o remount rw /system");
        doCommand("chmod 777 /system");
        doCommand("chmod 777 /system/build.prop");
        doCommand("chmod 777 " + newFile);
        FileOPT.WriteFile(oldFile, newFile);
        Log.e("liuwei", " reset build.prop 644 ok !!!!!!");
        doCommand("chmod 644 /system/build.prop");
        // SystemProperties.set("ro.secure", "1");
        // SystemProperties.set("persist.sys.root_access", "0");
    }

    public void copyConfFile(String newFile) {
        SystemProperties.set("ro.secure", "0");
        String oldFile = "/system/etc/custom.conf";
        doCommand("chmod 777 " + oldFile);
        FileOPT.WriteFile(oldFile, newFile);
        doCommand("chmod 644 /system/etc/custom.conf");
        SystemProperties.set("ro.secure", "1");
        SystemProperties.set("persist.sys.root_access", "0");
    }

    public static String doCommand(String cmd) {
        Process p;
        DataOutputStream os = null;
        InputStream is = null;
        SystemProperties.set("persist.sys.root_access", "2");
        try {
            p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
            is = p.getInputStream();
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            StringBuffer result = new StringBuffer();
            String line;
            while ((line = bf.readLine()) != null) {
                result.append(line);
            }
            os.flush();
            int ret = p.waitFor();
            if (ret == 0) {
                Log.d("liuwei", "docommand success!");
                return result.toString();
            } else {
                Log.d("liuwei", "docommand fail!");
                return "0";
            }

        } catch (IOException e) {
            Log.e("liuwei", "doCommond Exception= " + e.toString());
        } catch (InterruptedException e) {
            Log.e("liuwei", "doCommond InterruptedException = " + e.toString());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "0";
    }

    public void copyMedia(String oldFile, String oldaudio) {
        try {
            String newFile = "/system/media/bootanimation.zip";
            String newAudio = "/system/media/bootaudio.mp3";
            int bytesum = 0;
            int byteread = 0;
            SystemProperties.set("ro.secure", "0");
            // doCommand("mount -o remount rw /system/media");
            // doCommand("chmod 777 /system");
            doCommand("chmod 777 /system/media");
            doCommand("chmod 777 /system/media/bootanimation.zip");
            doCommand("chmod 777 /system/media/bootaudio.mp3");
            // doCommand("mount -o remount rw /system/ect");
            // doCommand("chmod 777 /system/ect");
            // doCommand("chmod 777 /system/ect/custom.conf");
            File oldfile = new File(oldFile);
            if (oldfile.exists()) {
                InputStream ins = new FileInputStream(oldFile);
                File newfile = new File(newFile);

                FileOutputStream outs = new FileOutputStream(newFile);
                byte[] buffer = new byte[500];
                while ((byteread = ins.read(buffer)) != -1) {
                    bytesum += byteread;
                    Log.e("liuwei", "bytesum  :  " + bytesum);
                    outs.write(buffer, 0, byteread);
                }
                ins.close();
            } else {
                throw new Exception();
            }

            File oldaudiofile = new File(oldaudio);
            if (oldaudiofile.exists()) {
                InputStream insoutaudio = new FileInputStream(oldaudio);
                File newaudiofile = new File(newAudio);

                FileOutputStream outaudio = new FileOutputStream(newAudio);
                byte[] buffer = new byte[500];
                while ((byteread = insoutaudio.read(buffer)) != -1) {
                    bytesum += byteread;
                    outaudio.write(buffer, 0, byteread);
                }
                Log.e("liuwei", "write ok !!!");
                insoutaudio.close();
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            Log.e("liuwei", "----------------------" + ex.toString());
            ex.printStackTrace();
        }
        doCommand("chmod 644 /system/build.prop");
        // SystemProperties.set("ro.secure", "1");
        // SystemProperties.set("persist.sys.root_access", "0");
    }

    public void setReset(Context context) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.e("liuwei", "MASTER_CLEAR !!! ");
        context.sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
    }
}
