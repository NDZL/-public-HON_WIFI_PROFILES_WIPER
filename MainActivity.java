package com.ndzl.wifiprofileswiper;

import static java.lang.System.exit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.osservice.sdk.CreateListener;
import com.honeywell.osservice.sdk.HonOSException;
import com.honeywell.osservice.sdk.SystemConfigManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;

import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/*
STARTING THIS APP FROM ADB AND SCRIPTING
adb shell am start com.ndzl.wifiprofileswiper/com.ndzl.wifiprofileswiper.MainActivity

PRE-REQUISITES TO GET THE APP RUNNING
0.-DO NOT CHANGE TARGET API=28
1.-MANUALLY GRANT LOCATION PERMISSION TO THIS APP AFTER INSTALLING - AND EXTERNAL FILE PERMISSION
2.-GRANT "EZCONFIG / Provisioning intents unrestricted=TRUE" via ENTERPRISE PROVISIONER OR EZConfig.xml file
3.-REQUIRES A HONEYWELL ANDROID DEVICE SUPPORTING HONEYWELL SDK
4.-WHITELIST THIS PACKAGENAME, USING A SPECIFIC SECTION IN DeviceConfig.xml
*/
public class MainActivity extends Activity {

    private Button bwf;

    private TextView tv;

    WifiManager wifiManager;
    SystemConfigManager mSystemConfigManager;

    Timer tim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        SystemConfigManager.create(this, new CreateListener<SystemConfigManager>() {
            @Override
            public void onCreate(SystemConfigManager systemConfigManager) {
                mSystemConfigManager = systemConfigManager;

                profilesRemove();
                Toast.makeText(getApplicationContext(), "JOB DONE. TARGET SDK="+getTargetSDK() , Toast.LENGTH_SHORT).show();
                finish();
                exit(0);

            }

            @Override
            public void onError(String s) {
                Log.e("OSSDK_Demo", "onError: SystemConfigManager:" + s);
            }
        });


        tim = new Timer("NIK", false);
        tim.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //call home here
                        //System.exit(0); //ok qui! chiamato da timer dopo 2 sec.
                    }

                });

            }
        }, 100000);

        String outres = "";//profilesRemove();

        //finish(); //serve! non rimuovere - fa sparire l'activity dalla recent apps list

    }


    String profilesRemove() {
        String res = "";

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {finish();}
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();  //WIFIMANAGER NOT AVAILABLE API>=30

        for( WifiConfiguration ap : list ) {
            String xmlConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <ConfigDoc name=\"DeviceConfig\">    <Section name=\"Wireless and networks\">       <Section name=\"WiFi\">          <Section name=\"Delete Profiles By SSID\">             <Key name=\"Delete Wi-Fi AP\">AAAAAAAA</Key>          </Section>       </Section>    </Section> </ConfigDoc>";

            if (mSystemConfigManager.isReady())
            {
                try {
                    String ssid = xmlConfig.replace("AAAAAAAA", ap.SSID.replace("\"", "") );
                    mSystemConfigManager.applyConfigByEXM( ssid ); //use this line to directly apply the XML string ==> needs EZCONFIG / Provisioning intents unrestricted=TRUE"
                } catch (HonOSException e) {
                    e.printStackTrace();
                }

            }
        }
        return res;
    }

    int getTargetSDK(){
        int version = 0;
        PackageManager pm = getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = pm.getApplicationInfo("com.ndzl.wifiprofileswiper", 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (applicationInfo != null) {
            version = applicationInfo.targetSdkVersion;
        }
        return  version;
    }

}


// <?xml version="1.0" standalone="yes"?> <ConfigDoc name="DeviceConfig">    <Section name="Wireless and networks">       <Section name="WiFi">          <Section name="Delete Profiles By SSID">             <Key name="Delete Wi-Fi AP">AAAAA</Key>             <Key name="Delete Wi-Fi AP">BBBBB</Key>             <Key name="Delete Wi-Fi AP">CCCCC</Key>             <Key name="Delete Wi-Fi AP">DDDDD</Key>             <Key name="Delete Wi-Fi AP">EEEEE</Key>             <Key name="Delete Wi-Fi AP">FFFFF</Key>             <Key name="Delete Wi-Fi AP">GGGGG</Key>             <Key name="Delete Wi-Fi AP">HHHHH</Key>          </Section>       </Section>    </Section>    <Section name="System">       <Section name="Developer options">          <Key name="boot_from_charger_mode">          </Key>       </Section>    </Section>    <SettingsPackage>pkg/HON4290/[311N-311G].00.08/</SettingsPackage>    <HHPReserved name="HHPReserved">       <Section>          <Key name="EXMVersion">1</Key>       </Section>    </HHPReserved> </ConfigDoc>

//String xmlConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <ConfigDoc name=\"DeviceConfig\">  <Section name=\"Personal\">   <Section name=\"Language and input\">    <Key name=\"LocaleLanguage\">it</Key>    <Key name=\"LocaleContry\">IT</Key>   </Section>  </Section> </ConfigDoc>\t";
