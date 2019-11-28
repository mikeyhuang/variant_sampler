package org.mulinlab.variantsampler.query;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedInputStream;
import org.mulinlab.variantsampler.utils.GP;
import org.mulinlab.variantsampler.utils.node.Node;
import org.mulinlab.variantsampler.utils.QueryParam;
import org.mulinlab.variantsampler.utils.sort.DTCTSort;
import org.mulinlab.variantsampler.utils.sort.PosSort;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.operations.readers.db.VannoMixReader;
import org.mulinlab.varnote.operations.readers.db.VannoReader;
import org.mulinlab.varnote.utils.database.DatabaseFactory;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import org.mulinlab.varnote.utils.node.LocFeature;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public final class DatabaseEngine {
    private static final Random r = new Random();

    private static final int LESS = 1;
    private static final int IN = 2;
    private static final int GREATER = 3;

    private static final int NO_MAF = -1;
    private static final String NO_RESULT = "No data";

    private final MafReader mafReader;
    private final VannoReader reader;
    private final QueryParam queryParam;
    private BlockCompressedInputStream dbFile;

    private final BufferedWriter output;

    private Map<String, Map<Integer, List<Node>>> chrMafMap;

    public DatabaseEngine(final String dbPath, final QueryParam queryParam, final String outputPath) throws IOException {
        final DBParam dbParam = new DBParam(dbPath);
        dbParam.setIntersect(IntersectType.EXACT);

        this.queryParam = queryParam;
        this.reader = new VannoMixReader(DatabaseFactory.readDatabase(dbParam));
        this.mafReader = new MafReader(dbPath);
        this.dbFile = new BlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(
                SeekableStreamFactory.getInstance().getStreamFor(dbPath)));

        this.output = new BufferedWriter(new FileWriter(new File(outputPath)));
        this.output.write(Node.getHeader(queryParam));
        this.output.newLine();

        this.chrMafMap = new HashMap<>();
    }

    public void close() throws IOException {
        this.reader.close();
        this.mafReader.close();
        this.dbFile.close();
        this.output.close();
    }

    public Map<Integer, List<Node>> convertQuery(final List<LocFeature> querys) throws IOException{
        Map<Integer, List<Node>> map = new HashMap<>();
        List<Node> mafList;

        Node node;
        int maf = NO_MAF;

        for (LocFeature locFeature: querys) {
            reader.query(locFeature);

            if(reader.getResults().size() > 0) {
                node = new Node(reader.getResults().get(0), queryParam);
                maf = node.getMaf();
            } else {
                node = new Node(locFeature);
                maf = NO_MAF;
            }

            if(map.get(maf) == null) {
                mafList = new ArrayList<>();
            } else {
                mafList = map.get(node.getMaf());
            }
            mafList.add(node);
            map.put(maf, mafList);
        }

//        for (Integer key: map.keySet()) {
//            Collections.sort(map.get(key), new DTCTSort());
//        }

        return map;
    }

    public void findList(final Map<Integer, List<Node>> querys) throws IOException{
        List<Node> queryNodes = new ArrayList<>();

        for (Integer maf: querys.keySet()) {
            for (Node node: querys.get(maf)) {
                findNode(node);
                queryNodes.add(node);
            }
        }

        Collections.sort(queryNodes, new PosSort());
        for (Node node: queryNodes) {
            printResults(node.toString(queryParam), node.getResult());
        }
    }

    public void findNode(final Node node) throws IOException{
        final int maf = node.getMaf();

        List<Long> addressList = new ArrayList<>();

        if(maf < 0) {
            node.setResult(NO_RESULT);
        } else {
            removeUselessChr(node.getChr());  //todo

            Map<Integer, List<Node>> mafMap = chrMafMap.get(node.getChr());
            if(mafMap == null) mafMap = new HashMap<>();

            removeUselessMaf(node.getMaf());

            List<Node> nodes;
            boolean match;
            for (int i = queryParam.getMafRangeMin(maf); i <=  queryParam.getMafRangeMax(maf); i++) {

                if(mafMap.get(i) == null) {
                    mafMap.put(i, mafReader.loadMAF(node.getChr(), i, queryParam));
                }
                addressList.clear();
                nodes = mafMap.get(i);
                if(nodes != null) {
                    for (Node obj: nodes) {
                        match = true;
                        if(isInRangeMAFOri(node.getMafOrg(), obj.getMafOrg()) == IN && isInRangeDistance(node.getDtct(), obj.getDtct()) == IN) {
                            if(queryParam.getGeneDisIndex() != GP.NO_GENE_DIS && isInRangeGeneDis(node.getGeneDis(), obj.getGeneDis()) != IN) {
                                match = false;
                            }
                            if(queryParam.getGeneLDIndex() != GP.NO_GENE_LD && isInRangeGeneLD(node.getGeneLD(), obj.getGeneLD()) != IN) {
                                match = false;
                            }
                            if(queryParam.getLdIndex() != GP.NO_LD_BUDDIES && isInRangeLDBuddies(node.getLdBuddies(), obj.getLdBuddies()) != IN) {
                                match = false;
                            }

                            if(queryParam.hasCellMarker() && (node.getRoadmap() != obj.getRoadmap())) {
                                match = false;
                            }

                            if(match) addressList.add(obj.getAddress());
                        }
                    }
                }
            }

            chrMafMap.put(node.getChr(), mafMap);
            node.setResult(randomSelect(addressList));
        }
    }

    public void printResults(final String query, final String result) throws IOException {
        output.write("query\t" + query);
        output.newLine();

        output.write("output\t" + result);
        output.newLine();
    }

    public String randomSelect(List<Long> addressList) throws IOException {
        if(addressList.size() > 0)  {
            final int index = r.nextInt(addressList.size());
            dbFile.seek(addressList.get(index));
            return new Node(dbFile.readLine(), queryParam).toString(queryParam);
        } else {
            return NO_RESULT;
        }
    }

    public void removeUselessChr(final String chr) {
        for (String key: chrMafMap.keySet()) {
            if(!key.toUpperCase().equals(chr.toUpperCase())) {
                chrMafMap.put(key, null);
            }
        }
    }

    public void removeUselessMaf(final int maf) {
        Map<Integer, List<Node>> mafMap;

        for (String chr: chrMafMap.keySet()) {

            mafMap = chrMafMap.get(chr);
            if(mafMap != null) {
                Iterator<Map.Entry<Integer, List<Node>>> iter = mafMap.entrySet().iterator();

                while (iter.hasNext()) {
                    Map.Entry<Integer, List<Node>> entry = iter.next();
                    if(isInRangeMAF(maf, entry.getKey()) != IN) {
                        iter.remove();
                    }
                }
            }
        }
    }

    public int isInRangeMAFOri(final double maf, final double dbmaf) {
        if(dbmaf < queryParam.getMafOriRangeMin(maf)) {
            return LESS;
        } else if(dbmaf > queryParam.getMafOriRangeMax(maf)) {
            return GREATER;
        } else {
            return IN;
        }
    }

    public int isInRangeMAF(final Integer nodeMaf, final Integer dbmaf) {
        if(dbmaf < queryParam.getMafRangeMin(nodeMaf)) {
            return LESS;
        } else if(dbmaf > queryParam.getMafRangeMax(nodeMaf)) {
            return GREATER;
        } else {
            return IN;
        }
    }

    public int isInRangeDistance(final Integer nodeDistance, final Integer dbDistance) {
        if(dbDistance < queryParam.getDisRangeMin(nodeDistance)) {
            return LESS;
        } else if(dbDistance > queryParam.getDisRangeMax(nodeDistance)) {
            return GREATER;
        } else {
            return IN;
        }
    }

    public int isInRangeGeneDis(final Integer geneInDis, final Integer dbGeneInDis) {
        if(dbGeneInDis < queryParam.getGeneDisRangeMin(geneInDis)) {
            return LESS;
        } else if(dbGeneInDis > queryParam.getGeneDisRangeMax(geneInDis)) {
            return GREATER;
        } else {
            return IN;
        }
    }

    public int isInRangeGeneLD(final Integer geneInLD, final Integer dbGeneInLD) {
        if(dbGeneInLD < queryParam.getGeneLDRangeMin(geneInLD)) {
            return LESS;
        } else if(dbGeneInLD > queryParam.getGeneLDRangeMax(geneInLD)) {
            return GREATER;
        } else {
            return IN;
        }
    }

    public int isInRangeLDBuddies(final Integer ldBuddies, final Integer dbLDBuddies) {
        if(dbLDBuddies < queryParam.getGeneBuddiesRangeMin(ldBuddies)) {
            return LESS;
        } else if(dbLDBuddies > queryParam.getGeneBuddiesRangeMax(ldBuddies)) {
            return GREATER;
        } else {
            return IN;
        }
    }
}
