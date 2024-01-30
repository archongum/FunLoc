package fuck.location.xposed.cellar

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import com.github.kyuubiran.ezxhelper.finders.FieldFinder
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.cellar.identity.Lte
import fuck.location.xposed.cellar.identity.Nr
import fuck.location.xposed.helpers.ConfigGateway

class TelephonyRegistryHooker {
    @ExperimentalStdlibApi
    @SuppressLint("PrivateApi")
    fun hookListen(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> =
            lpparam.classLoader.loadClass("com.android.server.TelephonyRegistry")

        XposedBridge.log("FL: [Cellar] Finding method in TelephonyRegistry")

        MethodFinder.fromClass(clazz)
            .filterByName("validateEventAndUserLocked")
            .filterPrivate()
            .forEach { method ->
                method.createHook {
                    after { param ->
                        val record = param.args[0]
                        val event = param.args[1] as Int

                        val packageName = FieldFinder.fromClass(record.javaClass)
                            .filterByName("callingPackage")
                            .first()
                            .get(record) as String

                        XposedBridge.log("FL: [Cellar] in validateEventAndUserLocked! Caller package name: $packageName")

                        val shouldReportOrigin = param.result as Boolean

                        if (ConfigGateway.get().inWhitelist(packageName) && shouldReportOrigin) {
                            val callBack = FieldFinder.fromClass(record.javaClass)
                                .filterByName("callback")
                                .first()
                                .get(record)
                            val phoneId = FieldFinder.fromClass(record.javaClass)
                                .filterByName("phoneId")
                                .first()
                                .get(record)

                            when (event) {
                                5 -> {
                                    XposedBridge.log("FL: [Cellar] in whiteList! Alter EVENT_CELL_LOCATION_CHANGED for now.")

                                    if (phoneId != null) {
                                        val mCellIdentity =
                                            FieldFinder.fromClass(param.thisObject.javaClass)
                                                .filterByName("mCellIdentity")
                                                .first()
                                                .get(param.thisObject)
                                        if (mCellIdentity != null) {
                                            if ((phoneId as Int) >= 0 && phoneId < (mCellIdentity as Array<*>).size) {
                                                val originalCellIdentity = mCellIdentity[phoneId]
                                                if (originalCellIdentity != null) {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                        when (originalCellIdentity) {
                                                            is CellIdentityLte -> {
                                                                MethodFinder.fromClass(callBack.javaClass)
                                                                    .filterByName("onCellLocationChanged")
                                                                    .first()
                                                                    .invoke(
                                                                        callBack,
                                                                        Lte().alterCellIdentity(
                                                                            originalCellIdentity
                                                                        )
                                                                    )
                                                            }

                                                            is CellIdentityNr -> {
                                                                MethodFinder.fromClass(callBack.javaClass)
                                                                    .filterByName("onCellLocationChanged")
                                                                    .first()
                                                                    .invoke(
                                                                        callBack,
                                                                        Nr().alterCellIdentity(
                                                                            originalCellIdentity
                                                                        )
                                                                    )
                                                            }

                                                            else -> {
                                                                MethodFinder.fromClass(callBack.javaClass)
                                                                    .filterByName("onCellLocationChanged")
                                                                    .first()
                                                                    .invoke(callBack, null)
                                                            }
                                                        }
                                                    } else {    // Android 9 do not support 5G network
                                                        when (originalCellIdentity) {
                                                            is CellIdentityLte -> {
                                                                MethodFinder.fromClass(callBack.javaClass)
                                                                    .filterByName("onCellLocationChanged")
                                                                    .first()
                                                                    .invoke(
                                                                        callBack,
                                                                        Lte().alterCellIdentity(
                                                                            originalCellIdentity
                                                                        )
                                                                    )
                                                            }

                                                            else -> {
                                                                MethodFinder.fromClass(callBack.javaClass)
                                                                    .filterByName("onCellLocationChanged")
                                                                    .first()
                                                                    .invoke(callBack, null)
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    MethodFinder.fromClass(callBack.javaClass)
                                                        .filterByName("onCellLocationChanged")
                                                        .first()
                                                        .invoke(callBack, null)
                                                }
                                            }
                                        }
                                    }

                                    param.result = false
                                }

                                11 -> {
                                    XposedBridge.log("FL: [Cellar] in whiteList! Alter EVENT_CELL_INFO_CHANGED for now.")

                                    if (phoneId != null) {
                                        val mCellInfo =
                                            FieldFinder.fromClass(param.thisObject.javaClass)
                                                .filterByName("mCellInfo")
                                                .first()
                                                .get(param.thisObject)
                                        if (mCellInfo != null) {
                                            if ((phoneId as Int) >= 0 && phoneId < (mCellInfo as ArrayList<*>).size) {
                                                val originalCellInfoList = mCellInfo[phoneId]
                                                if (originalCellInfoList != null) {
                                                    val modifiedCellInfoList =
                                                        mutableListOf<CellInfo>()

                                                    (originalCellInfoList as List<*>).forEach { cellInfo ->
                                                        if (cellInfo != null) {
                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                                when (cellInfo) {
                                                                    is CellInfoLte -> {
                                                                        modifiedCellInfoList.add(
                                                                            fuck.location.xposed.cellar.info.Lte()
                                                                                .constructNewCellInfoLte(
                                                                                    cellInfo
                                                                                )
                                                                        )
                                                                    }

                                                                    is CellInfoNr -> {
                                                                        modifiedCellInfoList.add(
                                                                            fuck.location.xposed.cellar.info.Nr()
                                                                                .constructNewCellInfoNr(
                                                                                    cellInfo
                                                                                )
                                                                        )
                                                                    }
                                                                }
                                                            } else {
                                                                when (cellInfo) {
                                                                    is CellInfoLte -> {
                                                                        modifiedCellInfoList.add(
                                                                            fuck.location.xposed.cellar.info.Lte()
                                                                                .constructNewCellInfoLte(
                                                                                    cellInfo
                                                                                )
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    MethodFinder.fromClass(callBack.javaClass)
                                                        .filterByName("onCellInfoChanged")
                                                        .first()
                                                        .invoke(
                                                            callBack,
                                                            modifiedCellInfoList
                                                        )    // return cellInfo
                                                } else {
                                                    MethodFinder.fromClass(callBack.javaClass)
                                                        .filterByName("onCellInfoChanged")
                                                        .first()
                                                        .invoke(callBack, null)
                                                }
                                            }
                                        } else {
                                            MethodFinder.fromClass(callBack.javaClass)
                                                .filterByName("onCellInfoChanged")
                                                .first()
                                                .invoke(callBack, null)
                                        }
                                    }

                                    param.result = false
                                }
                            }
                        }
                    }
                }
            }

        // TODO: Potential breakage in stock behavior. May being used as a detection way
        MethodFinder.fromClass(clazz)
            .filterByName("notifyCellInfoForSubscriber")
            .filterPublic()
            .forEach { method ->
                method.createHook {
                        before { param ->
//            XposedBridge.log("FL: [Cellar] in notifyCellInfoForSubscriber!")

                            val mRecordsField = FieldFinder.fromClass(clazz)
                                .filterByName("mRecords")
                                .first()
                            val mRecords = mRecordsField.get(param.thisObject) as ArrayList<*>
                            val newRecords = arrayListOf<Any>()

                            mRecords.forEach { record ->
                                val packageName = FieldFinder.fromClass(record.javaClass)
                                    .filterByName("callingPackage")
                                    .first()
                                    .get(record) as String
                                if (!ConfigGateway.get().inWhitelist(packageName)) {
                                    newRecords.add(record)
                                }
                            }

                            mRecordsField.set(param.thisObject, newRecords)
                        }
                    }
            }
    }
}