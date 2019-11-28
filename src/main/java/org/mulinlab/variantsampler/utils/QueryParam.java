package org.mulinlab.variantsampler.utils;

import org.mulinlab.variantsampler.utils.enumset.MAFRange;

public final class QueryParam {
    private double[] mafOriRange;
    private int[] mafRange;
    private int[] disRange;
    private int[] geneDisRange;
    private int[] geneLDRange;
    private int[] ldBuddiesRange;

    private int geneDisIndex = GP.NO_GENE_DIS;
    private int geneLDIndex = GP.NO_GENE_LD;
    private int ldIndex = GP.NO_LD_BUDDIES;
    private int markerIndex = GP.NO_MARKER;
    private int cellIdx = GP.NO_CELL_TYPE;

    public static QueryParam defaultQueryParam() {
        return new QueryParam(GP.DEFAULT_GENE_DIS.getIdx(), GP.NO_GENE_LD,
                GP.DEFAULT_LD_BUDDIES.getIdx(), 1, 4, GP.DEFAULT_MAF_RANGE, GP.DEFAULT_DIS_RANGE,
                GP.DEFAULT_GENE_DIS_RANGE, GP.DEFAULT_GENE_LD_RANGE, GP.DEFAULT_LD_BUDDIES_RANGE);
    }

    public QueryParam(int geneDisIndex, int geneLDIndex, int ldIndex, int markerIndex, int cellIdx, MAFRange mafRangeEnum, int disRange,
                      int geneDisRange, int geneLDRange, int ldBuddiesRange) {
        this.geneDisIndex = geneDisIndex;
        this.geneLDIndex = geneLDIndex;
        this.ldIndex = ldIndex;
        this.markerIndex = markerIndex;
        this.cellIdx = cellIdx;

        mafOriRange = new double[2];
        mafOriRange[0] = -mafRangeEnum.getVal();
        mafOriRange[1] = mafRangeEnum.getVal();

        mafRange = new int[2];
        mafRange[0] = - (int)(mafOriRange[0] * 100);
        mafRange[1] = (int)(mafOriRange[1] * 100);

        this.disRange = new int[2];
        this.disRange[0] = -disRange;
        this.disRange[1] = disRange;

        this.geneDisRange = new int[2];
        this.geneDisRange[0] = - geneDisRange;
        this.geneDisRange[1] = geneDisRange;

        this.geneLDRange = new int[2];
        this.geneLDRange[0] = - geneLDRange;
        this.geneLDRange[1] = geneLDRange;

        this.ldBuddiesRange = new int[2];
        this.ldBuddiesRange[0] = - ldBuddiesRange;
        this.ldBuddiesRange[1] = ldBuddiesRange;
    }

    public boolean hasCellMarker() {
        return markerIndex != GP.NO_MARKER && cellIdx != GP.NO_CELL_TYPE;
    }

    public void setMafRange(int[] mafRange) {
        this.mafRange = mafRange;
    }

    public void setDisRange(int[] disRange) {
        this.disRange = disRange;
    }

    public int getGeneDisIndex() {
        return geneDisIndex;
    }

    public int getGeneLDIndex() {
        return geneLDIndex;
    }

    public int getLdIndex() {
        return ldIndex;
    }

    public int getMarkerIndex() {
        return markerIndex;
    }

    public int getCellIdx() {
        return cellIdx;
    }

    public int getMafRangeMin(final int maf) {
        return (maf + mafRange[0]) >= 1 ? (maf + mafRange[0]) : 1;
    }

    public double getMafOriRangeMin(final double maf) {
        return (maf + mafOriRange[0]) >= 0 ? (maf + mafOriRange[0]) : 0;
    }

    public int getMafRangeMax(final int maf) {
        return (maf + mafRange[1]) <= 50 ? (maf + mafRange[1]) : 50;
    }

    public double getMafOriRangeMax(final double maf) {
        return (maf + mafOriRange[1]) <= 50 ? (maf + mafOriRange[1]) : 50;
    }

    public int getDisRangeMin(final int distance) {
        return (distance + disRange[0]) >= 1 ? (distance + disRange[0]) : 1;
    }

    public int getDisRangeMax(final int distance) {
        return distance + disRange[1];
    }

    public int getGeneDisRangeMin(final int geneDis) {
        return (geneDis + geneDisRange[0]) >= 0 ? (geneDis + geneDisRange[0]) : 0;
    }
    public int getGeneDisRangeMax(final int geneDis) {
        return geneDis + geneDisRange[1];
    }

    public int getGeneLDRangeMin(final int geneLD) {
        return (geneLD + geneLDRange[0]) >= 0 ? (geneLD + geneLDRange[0]) : 0;
    }
    public int getGeneLDRangeMax(final int geneLD) {
        return geneLD + geneLDRange[1];
    }

    public int getGeneBuddiesRangeMin(final int ldBuddies) {
        return (ldBuddies + ldBuddiesRange[0]) >= 0 ? (ldBuddies + ldBuddiesRange[0]) : 0;
    }
    public int getGeneBuddiesRangeMax(final int ldBuddies) {
        return ldBuddies + ldBuddiesRange[1];
    }
}
