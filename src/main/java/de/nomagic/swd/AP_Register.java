package de.nomagic.swd;

public enum AP_Register {
    UNKNOWN("unknown"),
    CSW("csw"),
    TAR("tar"),
    DRW("drw"),
    BD0("bd0"),
    BD1("bd1"),
    BD2("bd2"),
    BD3("bd3"),
    MBT("mbt"),
    T0TR("t0tr"),
    CFG1("cfg1"),
    BASE1("base1"),
    CFG("cfg"),
    BASE("base"),
    IDR("idr");

    private final String name;

    AP_Register(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
