package net.osdn.gokigen.blecontrol.lib.data.brainwave

class BrainwaveSummaryData
internal constructor() {
    //  3-byte value : delta (0.5 - 2.75Hz), theta (3.5 - 6.75Hz), low-alpha (7.5 - 9.25Hz), high-alpha (10 - 11.75Hz), low-beta (13 - 16.75Hz), high-beta (18 - 29.75Hz), low-gamma (31 - 39.75Hz), and mid-gamma (41 - 49.75Hz).
    private var delta = 0
    private var theta = 0
    private var lowAlpha = 0
    private var highAlpha = 0
    private var lowBeta = 0
    private var highBeta = 0
    private var lowGamma = 0
    private var midGamma = 0
    private var poorSignal = 0
    private var attention = 0
    private var mediation = 0

    fun update(packet: ByteArray): Boolean {
        var ret = false
        try {
            val length = packet.size
            if (length < 36) {
                return (ret)
            }

            poorSignal = packet[4].toInt()

            delta =
                (packet[7].toInt() and 0xff) * 65536 + (packet[8].toInt() and 0xff) * 256 + (packet[9].toInt() and 0xff)
            theta =
                (packet[10].toInt() and 0xff) * 65536 + (packet[11].toInt() and 0xff) * 256 + (packet[12].toInt() and 0xff)
            lowAlpha =
                (packet[13].toInt() and 0xff) * 65536 + (packet[14].toInt() and 0xff) * 256 + (packet[15].toInt() and 0xff)
            highAlpha =
                (packet[16].toInt() and 0xff) * 65536 + (packet[17].toInt() and 0xff) * 256 + (packet[18].toInt() and 0xff)
            lowBeta =
                (packet[19].toInt() and 0xff) * 65536 + (packet[20].toInt() and 0xff) * 256 + (packet[21].toInt() and 0xff)
            highBeta =
                (packet[22].toInt() and 0xff) * 65536 + (packet[23].toInt() and 0xff) * 256 + (packet[24].toInt() and 0xff)
            lowGamma =
                (packet[25].toInt() and 0xff) * 65536 + (packet[26].toInt() and 0xff) * 256 + (packet[27].toInt() and 0xff)
            midGamma =
                (packet[28].toInt() and 0xff) * 65536 + (packet[29].toInt() and 0xff) * 256 + (packet[30].toInt() and 0xff)

            attention = (packet[32].toInt() and 0xff)
            mediation = (packet[34].toInt() and 0xff)

            ret = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (ret)
    }

    val isSkinConnected: Boolean
        get() = (poorSignal != 200)

    fun getPoorSignal(): Int {
        return (poorSignal)
    }

    fun getDelta(): Int {
        return (delta)
    }

    fun getTheta(): Int {
        return (theta)
    }

    fun getLowAlpha(): Int {
        return (lowAlpha)
    }

    fun getHighAlpha(): Int {
        return (highAlpha)
    }

    fun getLowBeta(): Int {
        return (lowBeta)
    }

    fun getHighBeta(): Int {
        return (highBeta)
    }

    fun getLowGamma(): Int {
        return (lowGamma)
    }

    fun getMidGamma(): Int {
        return (midGamma)
    }

    fun getAttention(): Int {
        return (attention)
    }

    fun getMediation(): Int {
        return (mediation)
    }
}
