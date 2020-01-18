package org.mulinlab.variantsampler.query;

import org.mulinlab.variantsampler.utils.GP;
import org.mulinlab.variantsampler.utils.node.Node;
import org.mulinlab.variantsampler.utils.QueryParam;
import org.mulinlab.variantsampler.utils.sort.PosSort;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.operations.decode.TABLocCodec;
import org.mulinlab.varnote.operations.readers.db.VannoMixReader;
import org.mulinlab.varnote.operations.readers.db.VannoReader;
import org.mulinlab.varnote.utils.database.DatabaseFactory;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.IOException;
import java.util.*;

public final class DatabaseEngine {
    private static final Random r = new Random();

    private static final int LESS = 1;
    private static final int IN = 2;
    private static final int GREATER = 3;

    private static final int NO_MAF = -1;
    private final MafReader mafReader;
    private final VannoReader reader;

    private final QueryParam queryParam;
    private final TABLocCodec tabLocCodec;

    private Map<Integer, Map<Integer, List<Node>>> chrMafMap;
    private boolean hasRefAlt = false;
    private InputStack inputStack;

    public DatabaseEngine(final String dbPath, final InputStack inputStack, final QueryParam queryParam) throws IOException {
        final DBParam dbParam = new DBParam(dbPath);
        dbParam.setIntersect(IntersectType.EXACT);

        this.queryParam = queryParam;
        this.hasRefAlt = queryParam.getFormat().isRefAndAltExsit();

        this.reader = new VannoMixReader(DatabaseFactory.readDatabase(dbParam));
        this.mafReader = new MafReader(dbPath);

        this.tabLocCodec = GP.getDefaultDecode( false);
        this.chrMafMap = new HashMap<>();
        this.inputStack = inputStack;
    }

    public void close() throws IOException {
        this.reader.close();
        this.mafReader.close();
    }

    public Map<Integer, List<Node>> convertQuery(final List<LocFeature> querys) throws IOException{
        Map<Integer, List<Node>> map = new HashMap<>();
        List<Node> mafList;

        Node node;
        int maf ;

        LocFeature match = null;
        for (LocFeature locFeature: querys) {
            match = null;
            reader.query(locFeature);

            if(reader.getResults().size() > 0) {
                match = getMatchAnno(reader.getResults(), locFeature);
            }

            if(match != null) {
                node = new Node(match, queryParam);
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

        return map;
    }

    public LocFeature getMatchAnno(List<String> results, LocFeature query) {
        LocFeature db;

        for (String r:results) {
            db = tabLocCodec.decode(r);
            if(hasRefAlt && db.beg == query.beg && db.end == query.end) {
                return db;
            } else {
                if(queryParam.getFormat().isPos() && db.beg == query.beg) {
                    return db;
                } else if(db.beg == query.beg && db.end == query.end){
                    return db;
                }
            }
        }
        return null;
    }

    public List<Node> findList(final Map<Integer, List<Node>> querys) throws IOException{
        List<Node> queryNodes = new ArrayList<>();

        for (Integer maf: querys.keySet()) {
            for (Node node: querys.get(maf)) {
                findNode(node);
                queryNodes.add(node);
            }
        }

        Collections.sort(queryNodes, new PosSort());
        return queryNodes;
    }

    public void findNode(final Node node) throws IOException{

        if(node.isHasAnno() > 0) {
            if(!queryParam.isCrossChr()) removeUselessChr(node.getChr());

            ArrayList<Long> addressList = new ArrayList<>();
            if(queryParam.isCrossChr()) {
                for (String key: inputStack.getChrList()) {
                    samplerInChr(key, node, addressList);
                }
            } else {
                samplerInChr(node.getChr(), node, addressList);
            }

            ArrayList addressCopy = new ArrayList();
            for (int i = 0; i < queryParam.getSamplerNumber(); i++) {
                if(addressCopy.size() == 0) {
                    addressCopy = (ArrayList)addressList.clone();
                }
                node.addResult(randomSelect(addressCopy));
            }
            node.setPoolSize( addressList.size());
        }
    }

    public Node randomSelect(List<Long> addressCopy) throws IOException {
        if(addressCopy.size() > 0)  {
            final int index = r.nextInt(addressCopy.size());
            final long addr = addressCopy.get(index);
            addressCopy.remove(index);
            return chrMafMap.get((int)(addr & 0xff)).get((int)((addr >> 8) & 0xff)).get((int)((addr >> 16) & 0xFFFFFFFFFFFFL));
        } else {
            return null;
        }
    }

    public void samplerInChr(String chr, Node node, List<Long> addressList) throws IOException {
        Map<Integer, List<Node>> mafMap = chrMafMap.get(inputStack.chr2id(chr));
        if(mafMap == null) mafMap = new HashMap<>();

        removeUselessMaf(node.getMaf());

        List<Node> nodes;
        boolean match;
        Node obj;

        for (int i = queryParam.getMafRangeMin(node.getMaf()); i <= queryParam.getMafRangeMax(node.getMaf()); i++) {

            if(mafMap.get(i) == null) {
                mafMap.put(i, mafReader.loadMAF(chr, i, queryParam, inputStack));
            }

            nodes = mafMap.get(i);
            if(nodes != null) {
                for (int j = 0; j < nodes.size(); j++) {
                    obj = nodes.get(j);
                    match = true;
                    if(node.isInQuery()) {
                        continue;
                    }

                    if(queryParam.isVariantTypeSpecific() && node.isIndel() != obj.isIndel()) {
                        continue;
                    }

                    if(isInRangeMAFOri(node.getMafOrg(), obj.getMafOrg()) == IN) {
                        if(queryParam.getDisRange() != null && isInRangeDistance(node.getDtct(), obj.getDtct()) != IN) {
                            continue;
                        }
                        if(queryParam.getGeneDisIndex() != GP.NO_GENE_DIS && isInRangeGene(node.getGeneDis(), obj.getGeneDis()) != IN) {
                            continue;
                        }
                        if(queryParam.getGeneLDIndex() != GP.NO_GENE_LD && isInRangeGene(node.getGeneLD(), obj.getGeneLD()) != IN) {
                            continue;
                        }
                        if(queryParam.getLdIndex() != GP.NO_LD_BUDDIES && isInRangeLDBuddies(node.getLdBuddies(), obj.getLdBuddies()) != IN) {
                            continue;
                        }
                        if(queryParam.hasCellMarker() && (node.getRoadmap() != obj.getRoadmap())) {
                            continue;
                        }
                        if(queryParam.isVariantTypeMatch() && (node.getCat() != obj.getCat())) {
                            continue;
                        }
                        if(queryParam.getTissueIdx() != GP.NO_TISSUE && (node.isTissue() != obj.isTissue())) {
                            continue;
                        }
                        if(queryParam.getGcRange() != null && isInRangeGC(node.getGc(), obj.getGc()) != IN) {
                            continue;
                        }
                        if(match) {
                            addressList.add(((long)j << 16) | (i << 8) | inputStack.chr2id(chr));
                        }
                    }
                }
            }
        }

        chrMafMap.put(inputStack.chr2id(chr), mafMap);
    }


    public void removeUselessChr(final String chr) {
        for (Integer key: chrMafMap.keySet()) {
            if(key != inputStack.chr2id(chr)) {
                chrMafMap.put(key, null);
            }
        }
    }

    public void removeUselessMaf(final int maf) {
        Map<Integer, List<Node>> mafMap;

        for (Integer chr: chrMafMap.keySet()) {

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

    public int isInRangeGC(final Integer nodeGC, final Integer dbGC) {
        if(dbGC < queryParam.getGCRangeMin(nodeGC)) {
            return LESS;
        } else if(dbGC > queryParam.getGCRangeMax(nodeGC)) {
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

    public int isInRangeGene(final Integer gene, final Integer dbGene) {
        if(dbGene < queryParam.getGeneRangeMin(gene)) {
            return LESS;
        } else if(dbGene > queryParam.getGeneDisRangeMax(gene)) {
            return GREATER;
        } else {
            return IN;
        }
    }

    public int isInRangeLDBuddies(final Integer ldBuddies, final Integer dbLDBuddies) {
        if(dbLDBuddies < queryParam.getLDBuddiesRangeMin(ldBuddies)) {
            return LESS;
        } else if(dbLDBuddies > queryParam.getLDBuddiesRangeMax(ldBuddies)) {
            return GREATER;
        } else {
            return IN;
        }
    }
}
