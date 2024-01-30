package fuck.location.xposed.cellar

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.cellar.identity.Lte
import fuck.location.xposed.cellar.identity.Nr
import fuck.location.xposed.helpers.ConfigGateway

class PhoneInterfaceManagerHooker {
    @ExperimentalStdlibApi
    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("PrivateApi")
    fun hookCellLocation(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> =
            lpparam.classLoader.loadClass("com.android.phone.PhoneInterfaceManager")

        XposedBridge.log("FL: [Cellar] Finding method in PhoneInterfaceManager")

        MethodFinder.fromClass(clazz)
            .filterByName("getImeiForSlot")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    after { param ->
                        val packageName = param.args[1] as String
                        val customIMEI = "1234567891011120" // TODO: Support custom IMEI information

//                XposedBridge.log("FL: [Cellar] in getImeiForSlot! Caller package name: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            param.result = customIMEI
                            XposedBridge.log("FL: [Cellar] In whiteList! Return custom value for testing purpose: $customIMEI")
                        }
                    }
                }
            }

        MethodFinder.fromClass(clazz)
            .filterByName("getMeidForSlot")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    after { param ->
                        val packageName = param.args[1] as String
                        val customMEID = "1234567891011120" // TODO: Support custom MEID information

                        XposedBridge.log("FL: [Cellar] in getMeidForSlot! Caller package name: $packageName")
                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            param.result = customMEID
                            ConfigGateway.get().inWhitelist(param.args[1] as String)
                            XposedBridge.log("FL: [Cellar] In whiteList! Return custom value for testing purpose: $customMEID")
                        }
                    }
                }
            }


        MethodFinder.fromClass(clazz)
            .filterByName("getCellLocation")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    after { param ->
                        val packageName = param.args[0] as String
                        XposedBridge.log("FL: [Cellar] in getCellLocation! Caller package name: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: [Cellar] in whiteList! Return custom cell data information")
                            XposedBridge.log("FL: before ${param.result}")

                            when (param.result) {
                                is CellIdentityLte -> {
                                    XposedBridge.log("FL: [Cellar] Using LTE Network...")
                                    param.result =
                                        Lte().alterCellIdentity(param.result as CellIdentityLte)
                                }

                                is CellIdentityNr -> {
                                    XposedBridge.log("FL: [Cellar] Using Nr Network...")
                                    param.result =
                                        Nr().alterCellIdentity(param.result as CellIdentityNr)
                                }

                                else -> {
                                    XposedBridge.log("FL: [Cellar] Unsupported network type. Return null as fallback")
                                    param.result = null
                                }
                            }

                            // Android 9 does not have this network type
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && param.result is CellIdentityNr) {
                                XposedBridge.log("FL: [Cellar] Using NR Network...")
                                param.result =
                                    Nr().alterCellIdentity(param.result as CellIdentityNr)
                            }
                            XposedBridge.log("FL: After ${param.result}")
                        }
                    }
                }
            }

        MethodFinder.fromClass(clazz)
            .filterByName("getAllCellInfo")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    before { param ->
                        val packageName = param.args[0] as String
                        XposedBridge.log("FL: [Cellar] in getAllCellInfo! Caller package name: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: [Cellar] in whiteList! Return empty AllCellInfo for testing purpose.")
                            val customAllCellInfo = ArrayList<CellInfo>()
                            param.result = customAllCellInfo
                        }
                    }
                    after { param ->

                        val result = param.result
                        XposedBridge.log("${param.result}")
//                        XposedBridge.log(result.firstOrNull()?)
                    }
                }
            }

        MethodFinder.fromClass(clazz)
            .filterByName("getNeighboringCellInfo")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    before { param ->
                        val packageName = param.args[0] as String
//                XposedBridge.log("FL: [Cellar] in getNeighboringCellInfo! Caller package name: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: [Cellar] in whiteList! Return empty NeighboringCellInfo for testing purpose.")
                            val customNeighboringCellInfo = ArrayList<NeighboringCellInfo>()
                            param.result = customNeighboringCellInfo
                        }
                    }
                }
            }

        MethodFinder.fromClass(clazz)
            .filterByName("requestCellInfoUpdateInternal")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    before { param ->
                        val packageName = param.args[2] as String
//            XposedBridge.log("FL: [Cellar] in requestCellInfoUpdateInternal! Caller package name: $packageName")

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