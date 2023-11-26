package de.nomagic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestSaleaeDigitalChannel {

    @Test
    void testSaleaDigitalChannel_NULL()
    {
        SaleaDigitalChannel cut = new SaleaDigitalChannel(null);
        assert(false == cut.isValid());
    }



}
