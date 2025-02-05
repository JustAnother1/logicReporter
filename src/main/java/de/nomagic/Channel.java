package de.nomagic;

public enum Channel
{
    SWD_CLK("SWCLK"),
    SWD_IO("SWDIO"),
    SPI_CLK("CLK"),
    SPI_nCS("/CS"),
    SPI_MISO("MISO"),
    SPI_MOSI("MOSI");

    public final String name;

    private Channel(String name)
    {
        this.name = name;
    }
}
