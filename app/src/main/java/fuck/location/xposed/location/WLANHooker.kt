package fuck.location.xposed.location

import android.annotation.SuppressLint
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.os.Build
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import dalvik.system.PathClassLoader
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.ConfigGateway

class WLANHooker {
    @RequiresApi(Build.VERSION_CODES.R)
    @ExperimentalStdlibApi
    @SuppressLint("PrivateApi")
    fun hookWifiManager(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> =
            lpparam.classLoader.loadClass("com.android.server.SystemServiceManager")
        MethodFinder.fromClass(clazz)
            .filterByName("loadClassFromLoader")
            .filterStatic()
            .filterPrivate()
            .forEach { method ->
                method.createHook {
                    after { param ->
//                XposedBridge.log("FL: in loadClassFromLoader with service: " + param.args[0])
                        if (param.args[0] == "com.android.server.wifi.WifiService") {
                            XposedBridge.log("FL: Awesome! Now we are finding the REAL method...")
                            try {
                                val classloader = param.args[1] as PathClassLoader
                                val wifiClazz =
                                    classloader.loadClass("com.android.server.wifi.WifiServiceImpl")

                                MethodFinder.fromClass(wifiClazz)
                                    .filterByName("getScanResults")
                                    .filterPublic()
                                    .forEach { method ->
                                        method.createHook {
                                            after { param ->
                                                val packageName = param.args[0] as String
//                                XposedBridge.log("FL: In getScanResults with caller: $packageName")

                                                if (ConfigGateway.get().inWhitelist(packageName)) {
                                                    XposedBridge.log("FL: in whitelist! Return custom WiFi information")

                                                    val customResult = ScanResult()
                                                    customResult.BSSID = ""
                                                    customResult.SSID = "AndroidAP"
                                                    customResult.capabilities = "WPA-2"
                                                    customResult.level = -1

                                                    val result: List<ScanResult> = listOf()
                                                    param.result = result

                                                    XposedBridge.log("FL: BSSID: ${customResult.BSSID}, SSID: ${customResult.SSID}")
                                                }
                                            }
                                        }
                                    }

                                MethodFinder.fromClass(wifiClazz)
                                    .filterByName("getConnectionInfo")
                                    .filterPublic()
                                    .forEach { method ->
                                        method.createHook {
                                            after { param ->
                                                val packageName = param.args[0] as String
//                                XposedBridge.log("FL: In getConnectionInfo with caller: $packageName")

                                                if (ConfigGateway.get().inWhitelist(packageName)) {
//                                    XposedBridge.log("FL: in whitelist! Return custom WiFi information")

                                                    val customResult = WifiInfo.Builder()
                                                        .setBssid("")
                                                        .setSsid("Android-AP".toByteArray())
                                                        .setRssi(-1)
                                                        .setNetworkId(0)
                                                        .build()

                                                    param.result = customResult
//                                    XposedBridge.log("FL: BSSID: ${customResult.bssid}, SSID: ${customResult.ssid}")
                                                }
                                            }
                                        }
                                    }

                            } catch (e: Exception) {
                                XposedBridge.log("FL: fuck with exceptions! $e")
                            }
                        }
                    }
                }
            }
    }
}