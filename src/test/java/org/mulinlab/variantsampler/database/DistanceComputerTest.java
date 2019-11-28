package org.mulinlab.variantsampler.database;

import htsjdk.samtools.util.Log;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mulinlab.variantsampler.utils.DBSource;
import org.mulinlab.variantsampler.utils.Pair;
import org.mulinlab.varnote.utils.LoggingUtils;

import java.io.IOException;

public class DistanceComputerTest {

    DBSource dbSource = new DBSource("src/main/resources/db.ini");

    public DistanceComputerTest() throws MissingArgumentException {
    }

    @Test
    public void computeDTCT() throws IOException {
        LoggingUtils.setLoggingLevel(Log.LogLevel.ERROR);

        DistanceComputer distanceComputer = new DistanceComputer(dbSource.getDatabasePath(DBSource.GENCODE_GENE));
        distanceComputer.computeDTCT("9", 5453460);
        distanceComputer.computeDTCT("7", 88660988);
        distanceComputer.computeDTCT("1", 201688955);

        Integer[] r = distanceComputer.computeGeneInDistance("9", 5453460);
        System.out.println(StringUtils.join(r, ", "));
        r = distanceComputer.computeGeneInDistance("4", 59511935);
        System.out.println(StringUtils.join(r, ", "));
        r = distanceComputer.computeGeneInDistance("1", 10177);
        System.out.println(StringUtils.join(r, ", "));


        LDComputer computer = new LDComputer(dbSource.getDatabasePath(DBSource.EUR_LD_DB));
        Pair<Integer[], Pair<Integer, Integer>[]> pair = computer.compute("1", 10177, "A,AC");

        System.out.println(StringUtils.join(pair.getKey(), ", "));
        for (Pair<Integer, Integer> p: pair.getValue()) {
            System.out.println(p.getKey() + ", " + p.getValue());
        }

        pair = computer.compute("7", 88660988, "T,C");
        System.out.println(StringUtils.join(pair.getKey(), ", "));
        for (Pair<Integer, Integer> p: pair.getValue()) {
            System.out.println(p.getKey() + ", " + p.getValue());
        }

        pair = computer.compute("9", 5453460, "G,A");
        System.out.println(StringUtils.join(pair.getKey(), ", "));
        for (Pair<Integer, Integer> p: pair.getValue()) {
            System.out.println(p.getKey() + ", " + p.getValue());
        }
    }
}