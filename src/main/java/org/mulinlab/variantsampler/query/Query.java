package org.mulinlab.variantsampler.query;


import org.mulinlab.variantsampler.utils.GP;
import org.mulinlab.variantsampler.utils.QueryParam;
import org.mulinlab.varnote.filters.iterator.LineFilterIterator;
import org.mulinlab.varnote.operations.readers.query.TABFileReader;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Query {
    private DatabaseEngine dbEngine;
    private final TABFileReader reader;


    public Query(final String queryFile, final String dbFile) {
        this(queryFile, dbFile, QueryParam.defaultQueryParam(), null);
    }

    public Query(final String queryFile, final String dbFile, final QueryParam queryParam, String output) {
        if(output == null) output = getDefaultOutput(queryFile);
        try {
            dbEngine = new DatabaseEngine(dbFile, queryParam, output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Format format = Format.newTAB();
        format.sequenceColumn = 1;
        format.startPositionColumn = 2;
        format.endPositionColumn = 2;

        reader = new TABFileReader(queryFile, format);
    }

    public void close() throws IOException {
        dbEngine.close();
    }

    public static String getDefaultOutput(final String queryFile) {
        return queryFile + GP.DEFAULT_OUT_SUFFIX;
    }

    public void doQuery() throws IOException {
        LineFilterIterator iterator = reader.getFilterIterator();

        String seqName = "";

        LocFeature locFeature;
        List<LocFeature> querys = new ArrayList<>();
        int count = 0;
        while (iterator.hasNext()) {
            locFeature = iterator.next();
            if(locFeature != null) {
                if(!locFeature.chr.equals(seqName)) {
                    if(querys.size() > 0) {
                        dbEngine.findList(dbEngine.convertQuery(querys));
                        querys = new ArrayList<>();
                    }

                    seqName = locFeature.getContig();
                }

                count++;
                if(count % 1000 == 0) System.out.println(count);
                querys.add(locFeature.clone());
            }
        }
        iterator.close();
    }
}
