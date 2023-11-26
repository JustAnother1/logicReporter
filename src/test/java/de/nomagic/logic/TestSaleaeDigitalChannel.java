package de.nomagic.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

class TestSaleaeDigitalChannel {

    @Test
    void testSaleaDigitalChannel_NULL()
    {
        SaleaDigitalChannel cut = new SaleaDigitalChannel(null);
        assertFalse(cut.isValid());
    }

    @Test
    void testSaleaDigitalChannel_OK()
    {
        FileInputStream fin;
        try
        {
            fin = new FileInputStream("ref/digital_5.bin");
            SaleaDigitalChannel cut = new SaleaDigitalChannel(fin);
            assertTrue(cut.isValid());
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    void test_getNumberEdges_Low()
    {
        FileInputStream fin;
        try
        {
            fin = new FileInputStream("ref/digital_5.bin");
            SaleaDigitalChannel cut = new SaleaDigitalChannel(fin);
            assertTrue(cut.isValid());
            assertEquals(1950, cut.getNumberEdges());
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    void test_isHighAt_zero() throws IOException
    {
        FileInputStream fin;
        try
        {
            fin = new FileInputStream("ref/digital_5.bin");
            SaleaDigitalChannel cut = new SaleaDigitalChannel(fin);
            assertTrue(cut.isValid());
            assertTrue(cut.isHighAt(0.0));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        try
        {
            fin = new FileInputStream("ref/digital_4.bin");
            SaleaDigitalChannel cut = new SaleaDigitalChannel(fin);
            assertTrue(cut.isValid());
            assertFalse(cut.isHighAt(0.0));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    void test_getTimeOfEdgeAfter_Zero() throws IOException
    {
        FileInputStream fin;
        try
        {
            fin = new FileInputStream("ref/digital_5.bin");
            SaleaDigitalChannel cut = new SaleaDigitalChannel(fin);
            assertTrue(cut.isValid());
            assertEquals(2.114134240, cut.getTimeOfEdgeAfter(0.0));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

}
