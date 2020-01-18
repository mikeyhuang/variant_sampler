package org.mulinlab.variantsampler.database;

import org.junit.Test;
import org.mulinlab.variantsampler.utils.DBSource;
import org.mulinlab.variantsampler.utils.Pair;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.gz.MyBlockCompressedOutputStream;
import org.mulinlab.varnote.utils.gz.MyEndianOutputStream;

import java.io.IOException;

public class LDComputerTest {

    DBSource dbSource = new DBSource("src/main/resources/db.ini");

    public LDComputerTest() {
    }

    @Test
    public void compute() {
        try {
            LDComputer computer = new LDComputer(dbSource.getVal(DBSource.BITFILE));
//            Pair<Integer[], Pair<Integer, Integer>[]> pair = computer.compute("9", 5453460, "G", "A");
//            System.out.println(pair.getKey());


            Pair<Integer[], Pair<Integer, Integer>[]> pair1 = computer.compute("1", 10177, "A", "AC");
            System.out.println(pair1.getKey());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void genecodeFilter() throws IOException {
        final String FILTER = "gene";
        final String GENCODE_GTF = "/Users/hdd/Desktop/vanno/random/hg19/gencode.v32lift37.annotation.gtf.gz";
        final NoFilterIterator reader = new NoFilterIterator(GENCODE_GTF, FileType.GZ);

        final MyEndianOutputStream out = new MyEndianOutputStream(new MyBlockCompressedOutputStream(GENCODE_GTF.replace(".gtf.gz", ".gene.gtf.gz")));

        String[] token;
        String line, tss;

        while (reader.hasNext()) {
            line = reader.next();
            if(line.startsWith("#")) continue;
            token = line.split("\t");

            if(token[2].trim().equals(FILTER)) {
                out.writeBytes( line + "\n");
            }
        }

        reader.close();
        out.close();
    }
}