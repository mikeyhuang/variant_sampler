package org.mulinlab.variantsampler.utils;

public final class MafAddress {
    private final int maf;
    private long mafAddress;
    private long[] geneDistanceAddress;
    private long[] geneLDAddress;
    private long[] ldBuddiesAddress;

    private long[] roadmapAnno;

    public MafAddress(final int maf) {
        this.maf = maf;
        geneDistanceAddress = new long[10];
        geneLDAddress = new long[9];
        ldBuddiesAddress = new long[9];
        roadmapAnno = new long[6];
    }

    public long getMafAddress() {
        return mafAddress;
    }

    public void setMafAddress(long mafAddress) {
        this.mafAddress = mafAddress;
    }

    public long[] getGeneDistanceAddress() {
        return geneDistanceAddress;
    }

    public void setGeneDistanceAddress(long geneDistanceAddress, final int index) {
        this.geneDistanceAddress[index] = geneDistanceAddress;
    }

    public long[] getGeneLDAddress() {
        return geneLDAddress;
    }

    public void setGeneLDAddress(long geneLDAddress, final int index) {
        this.geneLDAddress[index] = geneLDAddress;
    }

    public long[] getLdBuddiesAddress() {
        return ldBuddiesAddress;
    }

    public void setLdBuddiesAddress(long ldBuddiesAddress, final int index) {
        this.ldBuddiesAddress[index] = ldBuddiesAddress;
    }

    public long[] getRoadmapAnno() {
        return roadmapAnno;
    }

    public void setRoadmapAnno(long roadmapAnno, final int index) {
        this.roadmapAnno[index] = roadmapAnno;
    }

    public int getMaf() {
        return maf;
    }
}
