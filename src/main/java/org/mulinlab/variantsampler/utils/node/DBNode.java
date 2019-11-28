package org.mulinlab.variantsampler.utils.node;

import org.mulinlab.variantsampler.utils.GP;
import org.mulinlab.variantsampler.utils.Pair;
import org.mulinlab.varnote.utils.node.LocFeature;

public final class DBNode extends AbstractNode {
    private Integer[] geneDensity;
    private Integer[] geneInLD;
    private Integer[] ldBuddies;
    private Pair<Long, Long>[] cellMarks;
    private String[] tokens;
    private long address;

    public DBNode(LocFeature locFeature, double maf, int dtct, Integer[] geneDensity, Integer[] geneInLD, Integer[] ldBuddies, Pair<Long, Long>[] cellMarks) {
        this.locFeature = locFeature;
        this.maf = (int)(maf * 100);
        this.dtct = dtct;
        this.geneDensity = geneDensity;
        this.geneInLD = geneInLD;
        this.ldBuddies = ldBuddies;
        this.cellMarks = cellMarks;
    }

    public DBNode(final String[] tokens) {
        final int pos = Integer.parseInt(tokens[GP.BP_IDX]);

        this.locFeature = new LocFeature(pos, pos, tokens[GP.CHR_IDX]);
        this.mafOrg = Double.parseDouble(tokens[GP.MAF_IDX]);
        this.maf = (int)(mafOrg * 100);

        this.dtct = Integer.parseInt(tokens[GP.DIS_IDX]);
        this.tokens = tokens;
    }

    public void decodeOthers() {
        geneDensity = new Integer[GP.GENE_DIS_SIZE];
        geneInLD = new Integer[GP.GENE_LD_SIZE];
        ldBuddies = new Integer[GP.LD_BUDDIES_SIZE];
        cellMarks = new Pair[GP.ROADMAP_SIZE];

        for (int i = 0; i < GP.GENE_DIS_SIZE; i++) {
            geneDensity[i] = Integer.parseInt(tokens[i + GP.GENE_DIS_START]);
        }

        for (int i = 0; i < GP.GENE_LD_SIZE; i++) {
            geneInLD[i] = Integer.parseInt(tokens[i + GP.GENE_LD_START]);
        }

        for (int i = 0; i < GP.LD_BUDDIES_SIZE; i++) {
            ldBuddies[i] = Integer.parseInt(tokens[i + GP.LD_BUDDIES_START]);
        }

        for (int i = 0; i < GP.ROADMAP_SIZE; i++) {
            cellMarks[i] = new Pair<>(Long.parseLong(tokens[GP.ROADMAP_START + i*2]), Long.parseLong(tokens[34 + i*2 + 1]));
        }
    }

    public Integer[] getGeneDensity() {
        return geneDensity;
    }

    public Integer[] getGeneInLD() {
        return geneInLD;
    }

    public Integer[] getLdBuddies() {
        return ldBuddies;
    }

    public Pair<Long, Long>[] getCellMarks() {
        return cellMarks;
    }

    public long getAddress() {
        return address;
    }

    public void setAddress(long address) {
        this.address = address;
    }
}
