package fuck.location.xposed.location.gnss

import android.annotation.SuppressLint
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.ConfigGateway

class GnssHookerPreQ {
    @SuppressLint("PrivateApi")
    fun hookAddGnssBatchingCallback(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz =
            lpparam.classLoader.loadClass("com.android.server.LocationManagerService")

        MethodFinder.fromClass(clazz)
            .filterByName("addGnssBatchingCallback")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    before { param ->
                        val packageName = param.args[1] as String
//            XposedBridge.log("FL: in addGnssBatchingCallback (Pre Q)! Caller package name: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: in whiteList! Dropping register request...")
                            param.result = false
                            return@before
                        }
                    }
                }
            }

        MethodFinder.fromClass(clazz)
            .filterByName("addGnssDataListener")
            .filterPrivate()
            .forEach { method ->
                method.createHook {
                    before { param ->
                        val packageName = param.args[1] as String
//            XposedBridge.log("FL: in addGnssDataListener (Pre Q)! Caller package name: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: in whiteList! Dropping register request...")
                            param.result = false
                            return@before
                        }
                    }
                }
            }

        MethodFinder.fromClass(clazz)
            .filterByName("addGnssMeasurementsListener")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    before { param ->
                        val packageName = param.args[1] as String
//            XposedBridge.log("FL: in addGnssMeasurementsListener (Pre Q)! Caller package name: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: in whiteList! Dropping register request...")
                            param.result = false
                            return@before
                        }
                    }
                }
            }

        MethodFinder.fromClass(clazz)
            .filterByName("addGnssNavigationMessageListener")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    before { param ->
                        val packageName = param.args[1] as String
//            XposedBridge.log("FL: in addGnssNavigationMessageListener (Pre Q)! Caller package name: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: in whiteList! Dropping register request...")
                            param.result = false
                            return@before
                        }
                    }
                }
            }
    }
}