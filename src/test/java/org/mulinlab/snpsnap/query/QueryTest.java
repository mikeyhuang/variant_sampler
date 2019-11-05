package org.mulinlab.snpsnap.query;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class QueryTest {

    @Test
    public void doQuery() {
        try {
            Query query = new Query("/Users/hdd/Desktop/vanno/random/query.sort.txt");
            query.doQuery();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}