package fuck.location.xposed.location.oplus

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.ArrayMap
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.FieldFinder
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.ConfigGateway

class NlpDLCS {
    @SuppressLint("PrivateApi")
    @OptIn(ExperimentalStdlibApi::class)
    @RequiresApi(Build.VERSION_CODES.S)
    fun hookColorOS(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz = lpparam.classLoader.loadClass("com.android.server.location.OplusLocationManagerService")

        MethodFinder.fromClass(clazz)
            .filterByName("newLocProviderManager")
            .filterPrivate()
            .forEach { method -> method.createHook {
                after { param ->
                    val providerName = param.args[0] as String

                    XposedBridge.log("FL: [Color] in newLocProviderManager!")
                    if (providerName == "network") {    // Hook Nlp provider manager each time
                        XposedBridge.log("FL: [Color] respawn nlp manager, trying to hook...")

                        val locationProviderManager = param.result
                        if (locationProviderManager != null) {
                            MethodFinder.fromClass(locationProviderManager.javaClass)
                                .filterByName("onReportLocation")
                                .first()
                                .createHook {
                                    before { param ->
                                        hookOnReportLocation(clazz, param)
                                    }
                                }
                        }
                    }
                }
            } }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @OptIn(ExperimentalStdlibApi::class)
    private fun hookOnReportLocation(clazz: Class<*>, param: XC_MethodHook.MethodHookParam) {
        XposedBridge.log("FL: [Color] in onReportLocation!")

        val mRegistrations = FieldFinder.fromClass(clazz)
            .findSuper()
            .filterByName("mRegistrations")
            .first()
        mRegistrations.isAccessible = true

        val registrations = mRegistrations.get(param.thisObject) as ArrayMap<*, *>
        val newRegistrations = ArrayMap<Any, Any>()

        registrations.forEach { registration ->
            val callerIdentity = FieldFinder.fromClass(registration.value.javaClass)
                .findSuper()
                .filterByName("mIdentity")
                .first()
                .get(registration.value)
            val packageName = ConfigGateway.get().callerIdentityToPackageName(callerIdentity!!)

            if (!ConfigGateway.get().inWhitelist(packageName)) {
                newRegistrations[registration.key] = registration.value
            } else {
                val value = registration.value
                val locationResult = param.args[0]

                val mLocationsField = FieldFinder.fromClass(locationResult.javaClass)
                    .findSuper()
                    .filterPrivate()
                    .filterByName("mLocations")
                    .first()
                mLocationsField.isAccessible = true
                val mLocations = mLocationsField.get(locationResult) as ArrayList<*>

                val originLocation = (mLocations[0] as Location).takeIf { mLocations.isNotEmpty() } ?: Location(LocationManager.GPS_PROVIDER)
                val fakeLocation = ConfigGateway.get().readFakeLocation()

                val location = Location(originLocation.provider)

                location.latitude = fakeLocation.x + (Math.random() * fakeLocation.offset - fakeLocation.offset / 2)
                location.longitude = fakeLocation.y + (Math.random() * fakeLocation.offset - fakeLocation.offset / 2)
                location.isMock = false
                location.altitude = 0.0
                location.speed = 0F
                location.speedAccuracyMetersPerSecond = 0F

                location.time = originLocation.time
                location.accuracy = originLocation.accuracy
                location.bearing = originLocation.bearing
                location.bearingAccuracyDegrees = originLocation.bearingAccuracyDegrees
                location.elapsedRealtimeNanos = originLocation.elapsedRealtimeNanos
                location.elapsedRealtimeUncertaintyNanos = originLocation.elapsedRealtimeUncertaintyNanos
                location.verticalAccuracyMeters = originLocation.verticalAccuracyMeters

                mLocationsField.set(locationResult, arrayListOf(location))

                val method = MethodFinder.fromClass(value.javaClass)
                    .filterByName("acceptLocationChange")
                    .findSuper()
                    .first()
                val operation = method.invoke(value, locationResult)

                MethodFinder.fromClass(value.javaClass)
                    .filterByName("executeOperation")
                    .findSuper()
                    .first().invoke(value, operation)
            }
        }

        mRegistrations.set(param.thisObject, newRegistrations)
    }
}