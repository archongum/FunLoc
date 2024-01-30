package fuck.location.app.ui.models

class FakeLocation(
    val x: Double,
    val y: Double,
    val offset:Double,

    val eci: Long,
    val pci: Int,
    val tac: Int,
    val earfcn: Int,
    val bandwidth: Int,
    val mnc: String
)

// Used for migrate from older version
class FakeLocationHistory(
    val x: Double,
    val y: Double,
)