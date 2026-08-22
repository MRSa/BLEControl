package net.osdn.gokigen.blecontrol.lib.data.brainwave;

public class BrainwaveSummaryData
{

    //  3-byte value : delta (0.5 - 2.75Hz), theta (3.5 - 6.75Hz), low-alpha (7.5 - 9.25Hz), high-alpha (10 - 11.75Hz), low-beta (13 - 16.75Hz), high-beta (18 - 29.75Hz), low-gamma (31 - 39.75Hz), and mid-gamma (41 - 49.75Hz).
    private int delta = 0;
    private int theta = 0;
    private int lowAlpha = 0;
    private int highAlpha = 0;
    private int lowBeta = 0;
    private int highBeta = 0;
    private int lowGamma = 0;
    private int midGamma = 0;
    private int poorSignal = 0;
    private int attention = 0;
    private int mediation = 0;

    BrainwaveSummaryData()
    {

    }

    boolean update(byte[] packet)
    {
        boolean ret = false;
        try
        {
            int length = packet.length;
            if (length < 36)
            {
                return (ret);
            }

            poorSignal = packet[4];

            delta     = (packet[ 7] & 0xff) * 65536 + (packet[ 8] & 0xff) * 256 + (packet[ 9] & 0xff);
            theta     = (packet[10] & 0xff) * 65536 + (packet[11] & 0xff) * 256 + (packet[12] & 0xff);
            lowAlpha  = (packet[13] & 0xff) * 65536 + (packet[14] & 0xff) * 256 + (packet[15] & 0xff);
            highAlpha = (packet[16] & 0xff) * 65536 + (packet[17] & 0xff) * 256 + (packet[18] & 0xff);
            lowBeta   = (packet[19] & 0xff) * 65536 + (packet[20] & 0xff) * 256 + (packet[21] & 0xff);
            highBeta  = (packet[22] & 0xff) * 65536 + (packet[23] & 0xff) * 256 + (packet[24] & 0xff);
            lowGamma  = (packet[25] & 0xff) * 65536 + (packet[26] & 0xff) * 256 + (packet[27] & 0xff);
            midGamma  = (packet[28] & 0xff) * 65536 + (packet[29] & 0xff) * 256 + (packet[30] & 0xff);

            attention = (packet[32] & 0xff);
            mediation = (packet[34] & 0xff);

            ret = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (ret);
    }

    public boolean isSkinConnected()
    {
        return (!(poorSignal == 200));
    }

    public int getPoorSignal()
    {
        return (poorSignal);
    }

    public int getDelta()
    {
        return (delta);
    }

    public int getTheta()
    {
        return (theta);
    }

    public int getLowAlpha()
    {
        return (lowAlpha);
    }

    public int getHighAlpha()
    {
        return (highAlpha);
    }

    public int getLowBeta()
    {
        return (lowBeta);
    }

    public int getHighBeta()
    {
        return (highBeta);
    }

    public int getLowGamma()
    {
        return (lowGamma);
    }

    public int getMidGamma()
    {
        return (midGamma);
    }

    public int getAttention()
    {
        return (attention);
    }

    public int getMediation()
    {
        return (mediation);
    }

}
