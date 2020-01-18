package org.mulinlab.variantsampler.query;


import org.mulinlab.variantsampler.utils.QueryParam;
import org.mulinlab.variantsampler.utils.SamplerWrite;
import org.mulinlab.variantsampler.utils.node.Node;
import org.mulinlab.varnote.config.anno.databse.VCFParser;
import org.mulinlab.varnote.operations.readers.query.AbstractFileReader;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.enumset.FormatType;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.headerparser.HeaderFormatReader;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Query {

    protected DatabaseEngine dbEngine;
    protected InputStack inputStack;

    protected int count = 0;
    protected QueryParam queryParam;
    protected SamplerWrite samplerWrite;

    public Query(final String queryFile, final String dbFile) {
        this(queryFile, dbFile, QueryParam.defaultQueryParam(), null);
    }

    public Query(final String queryFile, final Format format, final String dbFile) {
        this(queryFile, dbFile, QueryParam.defaultQueryParam(format), null);
    }

    public Query(final String queryFile, final String dbFile, final QueryParam queryParam) {
        this(queryFile, dbFile, queryParam, null);
    }
    public Query(final String queryFile, final String dbFile, final QueryParam queryParam, String outputDir) {
        this.queryParam = queryParam;

        try {
            samplerWrite = new SamplerWrite(queryFile, dbFile, queryParam, outputDir);

            FileType fileType = VannoUtils.checkFileType(queryFile);
            checkFormat(queryParam.getFormat(), queryFile, fileType);
            AbstractFileReader reader = VannoUtils.getReader(queryFile, fileType, queryParam.getFormat());

            inputStack = new InputStack(reader.getFilterIterator());
            dbEngine = new DatabaseEngine(dbFile, inputStack, queryParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkFormat(Format format, String file, FileType fileType) {
        if(format.type == FormatType.VCF) {
            new VCFParser(file);
        } else {
            if (format.isHasHeader()) {
                format = HeaderFormatReader.readDefaultHeader(file, fileType, format, true);
            } else {
                format = HeaderFormatReader.readDefaultHeader(file, fileType, format, false);
                HeaderFormatReader.checkDataIsValid(format, format.getDataStr().split("\t"));
            }
        }
    }

    public void close() throws IOException {
        dbEngine.close();
        samplerWrite.close();
    }

    public void doQuery() throws IOException {
        List<LocFeature> locFeatures;

        if(queryParam.isCrossChr()) {
            locFeatures = inputStack.getLocFeatureForAll(new ArrayList<>());
            Map<Integer, List<Node>> map = dbEngine.convertQuery(locFeatures);
            locFeatures = null;
            proceeMap(map);
        } else {
            for (String chr: inputStack.getChrList()) {
                locFeatures = inputStack.getLocFeatureForChr(new ArrayList<>(), chr);
                Map<Integer, List<Node>> map = dbEngine.convertQuery(locFeatures);
                locFeatures = null;
                proceeMap(map);
            }
        }
    }

    public void proceeMap(Map<Integer, List<Node>> map) throws IOException {
        for (Integer maf: map.keySet()) {
            for (Node node: map.get(maf)) {
                processLoc(node);
                node = null;
            }
        }
    }

    public void addRecordCount() {
        count++;
        if(count % 1000 == 0) System.out.println(count);
    }

    public void processLoc(final Node node) throws IOException {
        addRecordCount();
        dbEngine.findNode(node);
        samplerWrite.printNode(node);
    }

    public DatabaseEngine getDbEngine() {
        return dbEngine;
    }

    public InputStack getInputStack() {
        return inputStack;
    }

    public int getCount() {
        return count;
    }

    public QueryParam getQueryParam() {
        return queryParam;
    }

    public SamplerWrite getSamplerWrite() {
        return samplerWrite;
    }
}
