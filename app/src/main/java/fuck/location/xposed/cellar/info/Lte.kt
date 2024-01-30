package fuck.location.xposed.cellar.info

import android.telephony.CellIdentityLte
import android.telephony.CellInfoLte
import com.github.kyuubiran.ezxhelper.finders.FieldFinder
import fuck.location.xposed.cellar.identity.Lte

class Lte {
    @ExperimentalStdlibApi
    fun constructNewCellInfoLte(existedCellInfoLte: CellInfoLte): CellInfoLte {
        val existedResultField = FieldFinder.fromClass(existedCellInfoLte.javaClass)
            .filterByName("mCellIdentityLte")
            .first()
        val existedResult = existedResultField.get(existedCellInfoLte) as CellIdentityLte

        existedResultField.set(existedCellInfoLte, Lte().alterCellIdentity(existedResult))
        return existedCellInfoLte
    }
}