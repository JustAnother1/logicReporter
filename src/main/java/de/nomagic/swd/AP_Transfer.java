package de.nomagic.swd;

public class AP_Transfer
{
    private long packetNumber = 0;
    private boolean isWrite = false;
    private long data = 0;
    private long address = 0;

    public AP_Transfer()
    {
    }

    public void setToWriteTransfer()
    {
        isWrite = true;
    }

    public void setToReadTransfer()
    {
        isWrite = false;
    }

    public void setPacketNumber(long number)
    {
        packetNumber = number;
    }

    public void setData(long data)
    {
        this.data = data;
    }

    public void setAddress(long address)
    {
        this.address = address;
    }

    public boolean isWrite()
    {
        return isWrite;
    }

    public long getAddress()
    {
        return address;
    }

    public long getPacketNumber()
    {
        return packetNumber;
    }

    public long getData()
    {
        return data;
    }

}
