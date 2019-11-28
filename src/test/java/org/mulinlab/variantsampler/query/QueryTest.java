package org.mulinlab.variantsampler.query;

import org.junit.Test;
import java.io.IOException;

public class QueryTest {

    @Test
    public void doQuery() {
        try {
            Query query = new Query("/Users/hdd/Desktop/vanno/random/query.sort.chr.txt", "/Users/hdd/Desktop/vanno/random/hg19/EUR.gz");
            query.doQuery();
            query.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}