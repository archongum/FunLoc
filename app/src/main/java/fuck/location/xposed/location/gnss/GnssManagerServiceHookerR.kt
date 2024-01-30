package fuck.location.xposed.location.gnss

import android.annotation.SuppressLint
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.ConfigGateway

class GnssManagerServiceHookerR {
    @SuppressLint("PrivateApi")
    fun hookAddGnssBatchingCallback(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz =
            lpparam.classLoader.loadClass("com.android.server.location.gnss.GnssManagerService")

        MethodFinder.fromClass(clazz)
            .filterByName("addGnssBatchingCallback")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    before { param ->
                        val packageName = param.args[1] as String
//            XposedBridge.log("FL: in addGnssBatchingCallback (R)! Caller package name: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: in whiteList! Dropping register request...")
                            param.result = false
                            return@before
                        }
                    }
                }
            }

        MethodFinder.fromClass(clazz)
            .filterByName("registerGnssStatusCallback")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    before { param ->
                        val packageName = param.args[1] as String
//            XposedBridge.log("FL: in registerGnssStatusCallback (R)! Caller package name: $packageName")

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
                        val packageName = param.args[2] as String
//            XposedBridge.log("FL: in addGnssMeasurementsListener (R)! Caller package name: $packageName")

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
//            XposedBridge.log("FL: in addGnssNavigationMessageListener (R)! Caller package name: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: in whiteList! Dropping register request...")
                            param.result = false
                            return@before
                        }
                    }
                }
            }

        MethodFinder.fromClass(clazz)
            .filterByName("addGnssAntennaInfoListener")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    before { param ->
                        val packageName = param.args[1] as String
//            XposedBridge.log("FL: in addGnssAntennaInfoListener (R)! Caller package name: $packageName")

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