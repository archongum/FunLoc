package fuck.location.xposed.location.miui

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityNr
import android.telephony.CellInfo
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.cellar.identity.Lte
import fuck.location.xposed.cellar.identity.Nr
import fuck.location.xposed.helpers.ConfigGateway
import java.lang.Exception

class MiuiBlurLocationManagerHookerS {
    @SuppressLint("PrivateApi")
    @RequiresApi(Build.VERSION_CODES.S)
    @OptIn(ExperimentalStdlibApi::class)
    fun hookGetBlurryLocationS(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz =
            lpparam.classLoader.loadClass("com.android.server.location.MiuiBlurLocationManagerImpl")

        MethodFinder.fromClass(clazz)
            .filterByName("getBlurryLocation")
            .filterByParamCount(2)
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    after { param ->
                        val packageName =
                            ConfigGateway.get().callerIdentityToPackageName(param.args[1])
                        XposedBridge.log("FL: [Shaomi S] in getBlurryLocation (2)! Caller packageName: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: [Shaomi S] in whitelist! Return custom location")
                            val fakeLocation = ConfigGateway.get().readFakeLocation()

                            val locationResult = param.result

                            if (locationResult != null) {

                                val size = MethodFinder.fromClass(locationResult.javaClass)
                                    .filterByName("size")
                                    .first()
                                    .invoke(locationResult) as Int
                                for (i in 0 until size) {
                                    val originLocation =
                                        MethodFinder.fromClass(locationResult.javaClass)
                                            .filterByName("get")
                                            .first()
                                            .invoke(locationResult, i) as Location
                                    originLocation.latitude =
                                        fakeLocation.x + (Math.random() * fakeLocation.offset - fakeLocation.offset / 2)
                                    originLocation.longitude =
                                        fakeLocation.y + (Math.random() * fakeLocation.offset - fakeLocation.offset / 2)
                                    originLocation.altitude = 0.0
                                    originLocation.speed = 0F
                                    originLocation.speedAccuracyMetersPerSecond = 0F
                                    originLocation.isMock = false
                                }
                            }

                            XposedBridge.log("FL: [Shaomi S] Batched location processed!")
                            param.result = locationResult
                        }
                    }
                }
            }

        MethodFinder.fromClass(clazz)
            .filterByName("getBlurryLocation")
            .filterByParamCount(3)
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    after { param ->
                        val packageName = param.args[2] as String
                        XposedBridge.log("FL: [Shaomi S] in getBlurryLocation! Caller packageName: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: [Shaomi S] in whitelist! Return custom location")
                            val fakeLocation = ConfigGateway.get().readFakeLocation()

                            lateinit var location: Location
                            lateinit var originLocation: Location

                            if (param.result == null) {
                                location = Location(LocationManager.GPS_PROVIDER)
                                location.time = System.currentTimeMillis() - (100..10000).random()
                            } else {
                                originLocation = param.result as Location
                                location = Location(originLocation.provider)

                                location.time = originLocation.time
                                location.accuracy = originLocation.accuracy
                                location.bearing = originLocation.bearing
                                location.bearingAccuracyDegrees =
                                    originLocation.bearingAccuracyDegrees
                                location.elapsedRealtimeNanos = originLocation.elapsedRealtimeNanos
                                location.verticalAccuracyMeters =
                                    originLocation.verticalAccuracyMeters
                            }

                            location.latitude =
                                fakeLocation.x + (Math.random() * fakeLocation.offset - fakeLocation.offset / 2)
                            location.longitude =
                                fakeLocation.y + (Math.random() * fakeLocation.offset - fakeLocation.offset / 2)
                            location.altitude = 0.0
                            location.speed = 0F
                            location.speedAccuracyMetersPerSecond = 0F
                            location.isMock = false

                            XposedBridge.log("FL: [Shaomi S] x: ${location.latitude}, y: ${location.longitude}")
                            param.result = location
                        }
                    }
                }
            }

        MethodFinder.fromClass(clazz)
            .filterByName("getBlurryCellLocation")
            .filterByParamCount(3)
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    after { param ->
                        try {
                            val packageName = param.args[2] as String
                            XposedBridge.log("FL: [Shaomi S] in getBlurryCellLocation (3)! Caller packageName: $packageName")

                            if (ConfigGateway.get().inWhitelist(packageName)) {
                                when (param.result) {
                                    is CellIdentityLte -> {
                                        XposedBridge.log("FL: [Shaomi S] Using LTE Network...")
                                        param.result =
                                            Lte().alterCellIdentity(param.result as CellIdentityLte)
                                    }

                                    is CellIdentityNr -> {
                                        XposedBridge.log("FL: [Shaomi S] Using Nr Network...")
                                        param.result =
                                            Nr().alterCellIdentity(param.result as CellIdentityNr)
                                    }

                                    else -> {
                                        XposedBridge.log("FL: [Shaomi S] Unsupported network type. Return null as fallback")
                                        param.result = null
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            XposedBridge.log("FL: [Shaomi S] Wtf?! $e")
                            e.printStackTrace()
                        }
                    }
                }
            }

        MethodFinder.fromClass(clazz)
            .filterByName("getBlurryCellLocation")
            .filterByParamCount(1)
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    after { param ->
                        try {
                            val packageName =
                                ConfigGateway.get().callerIdentityToPackageName(param.args[0])
                            XposedBridge.log("FL: [Shaomi S] in getBlurryCellLocation (1)! Caller packageName: $packageName")

                            if (ConfigGateway.get().inWhitelist(packageName)) {
                                when (param.result) {
                                    is CellIdentityLte -> {
                                        XposedBridge.log("FL: [Shaomi S] Using LTE Network...")
                                        param.result =
                                            Lte().alterCellIdentity(param.result as CellIdentityLte)
                                    }

                                    is CellIdentityNr -> {
                                        XposedBridge.log("FL: [Shaomi S] Using Nr Network...")
                                        param.result =
                                            Nr().alterCellIdentity(param.result as CellIdentityNr)
                                    }

                                    else -> {
                                        XposedBridge.log("FL: [Shaomi S] Unsupported network type. Return null as fallback")
                                        param.result = null
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            XposedBridge.log("FL: [Shaomi S] Wtf?! $e")
                            e.printStackTrace()
                        }
                    }
                }
            }

        MethodFinder.fromClass(clazz)
            .filterByName("getBlurryCellInfos")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    after { param ->
                        val packageName = param.args[2] as String
                        XposedBridge.log("FL: [Shaomi S] in getBlurryCellInfos! Caller packageName: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: [Shaomi S] in whiteList! Return empty CellInfos for testing purpose.")
                            val customAllCellInfo = ArrayList<CellInfo>()
                            param.result = customAllCellInfo
                        }
                    }
                }
            }

        MethodFinder.fromClass(clazz)
            .filterByName("handleGpsLocationChangedLocked")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                    before { param ->
                        val packageName =
                            ConfigGateway.get().callerIdentityToPackageName(param.args[1])
                        XposedBridge.log("FL: [Shaomi S] in handleGpsLocationChangedLocked! Caller packageName: $packageName")

                        if (ConfigGateway.get().inWhitelist(packageName)) {
                            XposedBridge.log("FL: [Shaomi S] in whiteList! Dropping update request for testing purpose...")
                            param.result = null
                            return@before
                        }
                    }
                }
            }
    }
}