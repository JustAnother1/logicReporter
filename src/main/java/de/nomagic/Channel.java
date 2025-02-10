package de.nomagic;

public enum Channel
{
    // SWD
    SWD_CLK("SWCLK"),
    SWD_IO("SWDIO"),
    // SPI
    SPI_CLK("CLK"),
    SPI_nCS("/CS"),
    SPI_MOSI("MOSI"), // IO 0
    SPI_MISO("MISO"), // IO 1
    // QSPI
    SPI_IO2("IO2"),
    SPI_IO3("IO3");

    public final String name;

    private Channel(String name)
    {
        this.name = name;
    }
}
