package org.mulinlab.variantsampler.utils.node;

import org.mulinlab.variantsampler.database.RoadmapAnnotation;
import org.mulinlab.variantsampler.database.TissueAnnotation;
import org.mulinlab.variantsampler.utils.GP;
import org.mulinlab.variantsampler.utils.QueryParam;
import org.mulinlab.variantsampler.utils.enumset.CellType;
import org.mulinlab.variantsampler.utils.enumset.Marker;
import org.mulinlab.variantsampler.utils.enumset.VariantRegion;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.util.ArrayList;
import java.util.List;

public final class Node extends AbstractNode {

    private int hasAnno = 1;
    private List<Node> results;

    private int geneDis;
    private int geneLD;
    private int ldBuddies;
    private boolean roadmap;
    private byte cat;
    private boolean tissue;
    private int gc;

    private boolean isIndel = false;
    private boolean isInQuery = false;
    private int poolSize;

    public Node() {
    }

    public Node(final LocFeature locFeature) {
        this.locFeature = locFeature;
        this.hasAnno = 0;
    }

    public Node(final LocFeature locFeature, final QueryParam queryParam) {
        this.locFeature = locFeature.clone();
        this.isIndel = (this.locFeature.ref.length() != this.locFeature.alt.length());

        setOthers(locFeature.origStr.split("\t"), queryParam);
        results = new ArrayList<>();
    }

    public void setLocFeature(LocFeature locFeature) {
        super.setLocFeature(locFeature);
        this.isIndel = (this.locFeature.ref.length() != this.locFeature.alt.length());
    }

    public void setOthers(String[] tokens, QueryParam queryParam) {
        this.mafOrg = Double.parseDouble(tokens[GP.MAF_IDX]);
        maf = (int)(this.mafOrg * 100);
        dtct = Integer.parseInt(tokens[GP.DIS_IDX]);

        if(queryParam.getGeneDisIndex() != GP.NO_GENE_DIS) {
            geneDis = Integer.parseInt(tokens[GP.GENE_DIS_START + queryParam.getGeneDisIndex()]);
        }

        if(queryParam.getGeneLDIndex() != GP.NO_GENE_LD) {
            geneLD = Integer.parseInt(tokens[GP.GENE_LD_START + queryParam.getGeneLDIndex()]);
        }

        if(queryParam.getLdIndex() != GP.NO_LD_BUDDIES) {
            ldBuddies = Integer.parseInt(tokens[GP.LD_BUDDIES_START + queryParam.getLdIndex()]);
        }

        if(queryParam.hasCellMarker()) {
            if(tokens[ GP.ROADMAP_START + queryParam.getMarkerIndex()].trim().length() > 0) {
                roadmap = RoadmapAnnotation.getValOfIndex(GP.string2LongArr(tokens[queryParam.getMarkerIndex() + GP.ROADMAP_START].split(",")), queryParam.getCellIdx());
            } else {
                roadmap = false;
            }
        }

        if(queryParam.isVariantTypeMatch()) {
            cat = (byte)Integer.parseInt(tokens[GP.CAT_START]);
        }

        if(queryParam.getTissueIdx() != GP.NO_TISSUE) {
            if(tokens[GP.TISSUE_START].trim().length() > 0) {
                tissue = TissueAnnotation.getValOfIndex(new long[]{Long.parseLong(tokens[GP.TISSUE_START])}, queryParam.getTissueIdx());
            } else {
                tissue = false;
            }
        }

        if(queryParam.getGcIdx() != GP.NO_GC) {
            gc = GP.string2IntegerArr(tokens[GP.GC_START].split(","))[queryParam.getGcIdx()];
        }

        locFeature.parts = null;
        locFeature.origStr = null;
    }

    public String briefQuery(boolean isRefAlt) {
        if(isRefAlt) {
            return String.format("%s:%d:%s:%s", locFeature.chr, locFeature.beg + 1, locFeature.ref, locFeature.alt);
        } else {
            return String.format("%s:%d", locFeature.chr, locFeature.beg + 1);
        }
    }

    public String briefAnno() {
        if(hasAnno == 0) {
            return "-";
        } else {
            return String.format("%s:%d:%s:%s", locFeature.chr, locFeature.beg + 1, locFeature.ref, locFeature.alt);
        }
    }

    public String toString(final QueryParam queryParam) {
        String s;
        if(maf < 0) {
            s = String.format("%s\t%d\t%s\t%s\t%s", locFeature.chr, locFeature.beg + 1, locFeature.ref == null ? "-" : locFeature.ref,
                    locFeature.alt == null ? "-" : locFeature.alt, "-");
        } else {
            s= String.format("%s\t%d\t%s\t%s\t%f", locFeature.chr, locFeature.beg + 1, locFeature.ref, locFeature.alt, mafOrg);
        }

        if( queryParam.getDisRange() != null) s += "\t" + ((hasAnno == 0) ? "-" : dtct);
        if( queryParam.getGeneDisIndex() != GP.NO_GENE_DIS) s += "\t" + ((hasAnno == 0)? "-" : geneDis);
        if( queryParam.getGeneLDIndex() != GP.NO_GENE_LD) s += "\t" + ((hasAnno == 0) ? "-" : geneLD);
        if( queryParam.getLdIndex() != GP.NO_LD_BUDDIES) s += "\t" + ((hasAnno == 0) ? "-" : ldBuddies);
        if(queryParam.getGcIdx() != GP.NO_GC) {
            s += "\t" + ((hasAnno == 0) ? "-" : gc + "%");
        }
        if(queryParam.isVariantTypeMatch()) {
            s += "\t" + ((hasAnno == 0) ? "-" : VariantRegion.getVal(cat));
        }
        if( queryParam.hasCellMarker()) s += "\t" + ((hasAnno == 0) ? "-" : roadmap);
        if(queryParam.getTissueIdx() != GP.NO_TISSUE) {
            s += "\t" + ((hasAnno == 0) ? "-" : tissue);
        }
        return s;
    }

    public void setGeneDis(int geneDis) {
        this.geneDis = geneDis;
    }

    public void setGeneLD(int geneLD) {
        this.geneLD = geneLD;
    }

    public void setLdBuddies(int ldBuddies) {
        this.ldBuddies = ldBuddies;
    }

    public List<Node> getResults() {
        return results;
    }

    public void addResult(Node result) {
        this.results.add(result);
    }

    public int getGeneDis() {
        return geneDis;
    }

    public int getGeneLD() {
        return geneLD;
    }

    public int getLdBuddies() {
        return ldBuddies;
    }

    public boolean getRoadmap() {
        return roadmap;
    }

    public void setRoadmap(boolean roadmap) {
        this.roadmap = roadmap;
    }

    public int isHasAnno() {
        return hasAnno;
    }

    public void setHasAnno(int hasAnno) {
        this.hasAnno = hasAnno;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public boolean isIndel() {
        return isIndel;
    }

    public void setIndel(boolean indel) {
        isIndel = indel;
    }

    public boolean isInQuery() {
        return isInQuery;
    }

    public void setInQuery(boolean inQuery) {
        isInQuery = inQuery;
    }

    public byte getCat() {
        return cat;
    }

    public void setCat(byte cat) {
        this.cat = cat;
    }

    public boolean isTissue() {
        return tissue;
    }

    public void setTissue(boolean tissue) {
        this.tissue = tissue;
    }

    public int getGc() {
        return gc;
    }

    public void setGc(int gc) {
        this.gc = gc;
    }
}
