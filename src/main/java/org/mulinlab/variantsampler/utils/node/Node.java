package org.mulinlab.variantsampler.utils.node;

import org.mulinlab.variantsampler.database.RoadmapAnnotation;
import org.mulinlab.variantsampler.utils.GP;
import org.mulinlab.variantsampler.utils.Pair;
import org.mulinlab.variantsampler.utils.QueryParam;
import org.mulinlab.variantsampler.utils.enumset.CellType;
import org.mulinlab.variantsampler.utils.enumset.GeneInDis;
import org.mulinlab.variantsampler.utils.enumset.LD;
import org.mulinlab.variantsampler.utils.enumset.Marker;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.util.BitSet;

public final class Node extends AbstractNode {

    private String query;
    private String result;
    private long address;

    private int geneDis;
    private int geneLD;
    private int ldBuddies;
    private int roadmap = GP.NO_CELL_TYPE;

    public Node() {
    }

    public Node(final LocFeature locFeature) {
        this.locFeature = locFeature;
    }

    public Node(final String query, final QueryParam queryParam) {
        this.query = query;
        String[] tokens = query.split("\t");

        final int pos = Integer.parseInt(tokens[GP.BP_IDX]);
        this.locFeature = new LocFeature(pos, pos, tokens[GP.CHR_IDX]);

        this.locFeature.ref = tokens[GP.REF_IDX];
        this.locFeature.alt = tokens[GP.ALT_IDX];

        this.mafOrg = Double.parseDouble(tokens[GP.MAF_IDX]);
        maf = (int)(this.mafOrg * 100);
        dtct = Integer.parseInt(tokens[GP.DIS_IDX]);

        geneDis = Integer.parseInt(tokens[GP.GENE_DIS_START + queryParam.getGeneDisIndex()]);
        geneLD = Integer.parseInt(tokens[GP.GENE_LD_START + queryParam.getGeneLDIndex()]);
        ldBuddies = Integer.parseInt(tokens[GP.LD_BUDDIES_START + queryParam.getLdIndex()]);

        if(queryParam.getMarkerIndex() != GP.NO_MARKER && queryParam.getCellIdx() != GP.NO_CELL_TYPE) {
            int roadmapidx = GP.ROADMAP_START + queryParam.getMarkerIndex() * 2;
            roadmap = RoadmapAnnotation.getValOfIndex(Long.parseLong(tokens[roadmapidx]), Long.parseLong(tokens[roadmapidx + 1]), queryParam.getCellIdx());
        }

        rsid = tokens[GP.RSID_IDX];
    }

    public static String getHeader(final QueryParam queryParam) {
        String header = " \tCHR\tPOS\tREF\tALT\tMAF\tDTCT";

        if(queryParam.getGeneDisIndex() != GP.NO_GENE_DIS) {
            header += "\tGENE_DIS" + "_" + GeneInDis.getVal(queryParam.getGeneDisIndex());
        }
        if(queryParam.getGeneLDIndex() != GP.NO_GENE_LD) {
            header += "\tGENE_LD" + "_" + LD.getVal(queryParam.getGeneLDIndex());
        }
        if(queryParam.getLdIndex() != GP.NO_LD_BUDDIES) {
            header += "\tLD_BUDDIES" + "_" + LD.getVal(queryParam.getLdIndex());
        }
        if(queryParam.hasCellMarker()) {
            header += "\t" + Marker.getVal(queryParam.getMarkerIndex()).toString() + "_" + CellType.getVal(queryParam.getCellIdx()).toString();
        }
        return header;
    }

    public String toString(final QueryParam queryParam) {
        String s = String.format("%s\t%d\t%s\t%s\t%f\t%d", locFeature.chr, locFeature.beg, locFeature.ref, locFeature.alt, mafOrg, dtct);
        if(queryParam.getGeneDisIndex() != GP.NO_GENE_DIS) {
            s += "\t" + geneDis;
        }
        if(queryParam.getGeneLDIndex() != GP.NO_GENE_LD) {
            s += "\t" + geneLD;
        }
        if(queryParam.getLdIndex() != GP.NO_LD_BUDDIES) {
            s += "\t" + ldBuddies;
        }
        if(roadmap != GP.NO_CELL_TYPE) {
            s += "\t" + roadmap;
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

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public long getAddress() {
        return address;
    }

    public void setAddress(long address) {
        this.address = address;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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

    public int getRoadmap() {
        return roadmap;
    }

    public void setRoadmap(int roadmap) {
        this.roadmap = roadmap;
    }
}
