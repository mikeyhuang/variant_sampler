package org.mulinlab.variantsampler.query;

import org.junit.Test;

import static org.junit.Assert.*;

public class DatabaseEngineTest {

    @Test
    public void randomSelect() {

        int index = 2324237;
        int maf = 15;
        int chr = 16;

        long addr = (((long)index << 16) | (maf << 8) | chr);


        System.out.println((int)(addr >> 16 & 0xFFFFFFFFFFFFL));
        System.out.println((int) addr >> 8 & 0xff);
        System.out.println((int)addr & 0xff);
    }
}