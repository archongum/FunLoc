package fuck.location.xposed.cellar.info

import android.os.Build
import android.telephony.CellIdentityNr
import android.telephony.CellInfoNr
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.finders.FieldFinder
import fuck.location.xposed.cellar.identity.Nr

class Nr {
    @RequiresApi(Build.VERSION_CODES.Q)
    fun constructNewCellInfoNr(existedCellInfoNr: CellInfoNr): CellInfoNr {
        val existedResultField = FieldFinder.fromClass(existedCellInfoNr.javaClass)
            .filterByName("mCellIdentity")
            .first()
        val existedResult = existedResultField.get(existedCellInfoNr) as CellIdentityNr
        existedResultField.set(existedCellInfoNr, Nr().alterCellIdentity(existedResult))

        return existedCellInfoNr
    }
}