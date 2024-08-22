package de.nomagic.swd;

public enum DP_Register {
    UNKNOWN("unknown"),
    ABORT("abort"),
    BASEPTR0("baseptr0"),
    BASEPTR1("baseptr1"),
    CTRL_STAT("ctrl/stat"),
    DLCR("dlcr"),
    DLPIDR("dlpidr"),
    DPIDR("dpidr"),
    DPIDR1("dpidr1"),
    EVENTSTAT("eventstat"),
    RDBUFF("rdbuff"),
    RESEND("resend"),
    SELECT("select"),
    SELECT1("select1"),
    TARGETID("targetid"),
    TARGETSEL("targetsel");

    private final String name;

    DP_Register(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
