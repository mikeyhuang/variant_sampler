package org.mulinlab.variantsampler.index;

import org.junit.Test;
import org.mulinlab.variantsampler.utils.enumset.CellType;
import org.mulinlab.variantsampler.utils.node.DBNode;
import org.mulinlab.varnote.operations.readers.itf.BGZReader;

import java.io.IOException;
import java.util.BitSet;


public class IndexMafTest {

    @Test
    public void getMafList() throws IOException {
        IndexMaf indexMaf = new IndexMaf("/hg19/EUR.gz");

    }

    @Test
    public void read() throws Exception {
        BGZReader reader = new BGZReader("/hg19/EUR1.gz");
        String s;
        String[] token;

        while ((s = reader.readLine()) != null) {
            token = s.split("\t");

            BitSet bitSet = BitSet.valueOf(token[34].getBytes());

            for (int i = 0; i < CellType.values().length; i++)
            {
                System.out.print((bitSet.get(i) ? 1 : 0 )+ " ") ;
            }

            System.out.println(s);
        }
        reader.closeReader();

    }
}