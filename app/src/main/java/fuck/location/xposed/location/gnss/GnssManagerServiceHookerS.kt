package fuck.location.xposed.location.gnss

import android.annotation.SuppressLint
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.ConfigGateway

class GnssManagerServiceHookerS {
    @SuppressLint("PrivateApi")
    fun hookRegisterGnssNmeaCallback(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz =
            lpparam.classLoader.loadClass("com.android.server.location.gnss.GnssManagerService")

        MethodFinder.fromClass(clazz)
            .filterByName("registerGnssStatusCallback")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    before { param ->
                        val packageName = param.args[1] as String
//            XposedBridge.log("FL: in registerGnssStatusCallback! Caller package name: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: in whiteList! Dropping register request...")
                            param.result = null
                            return@before
                        }
                    }
                }
            }

        MethodFinder.fromClass(clazz)
            .filterByName("registerGnssNmeaCallback")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    before { param ->
                        val packageName = param.args[1] as String
//            XposedBridge.log("FL: in registerGnssNmeaCallback! Caller package name: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: in whiteList! Dropping register request...")
                            param.result = null
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
//            XposedBridge.log("FL: in addGnssMeasurementsListener! Caller package name: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: in whiteList! Dropping register request...")
                            param.result = null
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
//            XposedBridge.log("FL: in addGnssNavigationMessageListener! Caller package name: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: in whiteList! Dropping register request...")
                            param.result = null
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
//            XposedBridge.log("FL: in addGnssAntennaInfoListener! Caller package name: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: in whiteList! Dropping register request...")
                            param.result = null
                            return@before
                        }
                    }
                }
            }
    }
}