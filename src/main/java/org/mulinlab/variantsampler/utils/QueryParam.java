package org.mulinlab.variantsampler.utils;

import org.mulinlab.variantsampler.utils.enumset.GCDeviation;
import org.mulinlab.variantsampler.utils.enumset.LD;
import org.mulinlab.variantsampler.utils.enumset.MAFDeviation;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.format.Format;


public final class QueryParam {
    private final Format format;
    private boolean crossChr = false;
    private boolean excludeInput = true;
    private boolean variantTypeSpecific = true;
    private int samplerNumber = 1;
    private int annoNumber = 1;

    private double[] mafOriRange;
    private int[] mafRange;
    private int[] disRange = null;
    private int[] geneRange;
    private int[] ldBuddiesRange;

    private int geneDisIndex = GP.NO_GENE_DIS;
    private int geneLDIndex = GP.NO_GENE_LD;
    private int ldIndex = GP.NO_LD_BUDDIES;
    private int markerIndex = GP.NO_MARKER;
    private int cellIdx = GP.NO_CELL_TYPE;
    private int gcIdx = GP.NO_GC;

    private boolean variantTypeMatch = false;
    private int tissueIdx = GP.NO_TISSUE;
    private double[] gcRange;

    public static QueryParam defaultQueryParam() {
        Format format = Format.newTAB();
        format.sequenceColumn = 1;
        format.startPositionColumn = 2;
        format.endPositionColumn = 2;

        return defaultQueryParam(format);
    }

    public static QueryParam defaultQueryParam(Format format) {
        QueryParam queryParam =  new QueryParam(format, GP.DEFAULT_GENE_DIS.getIdx(), GP.NO_GENE_LD,
                LD.LD1.getIdx(), -1, -1, GP.DEFAULT_MAF_DEVIATION, GP.DEFAULT_DIS_DEVIATION,
                GP.DEFAULT_GENE_DEVIATION, GP.DEFAULT_LD_BUDDIES_DEVIATION, false, GP.NO_TISSUE,  GP.NO_GC,GP.DEFAULT_GC_DEVIATION);

        return queryParam;
    }

    public QueryParam(final Format format, int geneDisIndex, int geneLDIndex, int ldIndex, int markerIndex, int cellIdx,
                      MAFDeviation mafRangeEnum, int disRange, int geneRange, int ldBuddiesRange, boolean variantTypeMatch, int tissueIdx, int gcIdx,
                      GCDeviation gcDeviation) {
        this.format = format;
        this.geneDisIndex = geneDisIndex;
        this.geneLDIndex = geneLDIndex;
        this.ldIndex = ldIndex;
        this.markerIndex = markerIndex;
        this.cellIdx = cellIdx;
        this.gcIdx = gcIdx;
        mafOriRange = new double[2];
        mafOriRange[0] = -mafRangeEnum.getVal();
        mafOriRange[1] = mafRangeEnum.getVal();

        mafRange = new int[2];
        mafRange[0] = - (int)(mafOriRange[0] * 100);
        mafRange[1] = (int)(mafOriRange[1] * 100);

        if(disRange > -1) {
            this.disRange = new int[2];
            this.disRange[0] = -disRange;
            this.disRange[1] = disRange;
        }

        if(geneDisIndex > -1) {
            this.geneRange = new int[2];
            this.geneRange[0] = - geneRange;
            this.geneRange[1] = geneRange;
        }

        if(geneLDIndex > -1) {
            this.geneRange = new int[2];
            this.geneRange[0] = - geneRange;
            this.geneRange[1] = geneRange;
        }

        if(ldIndex > -1) {
            this.ldBuddiesRange = new int[2];
            this.ldBuddiesRange[0] = - ldBuddiesRange;
            this.ldBuddiesRange[1] = ldBuddiesRange;
        }

        if(gcIdx > -1 && gcDeviation != null) {
            this.gcRange = new double[2];
            this.gcRange[0] = - gcDeviation.getVal() * 100;
            this.gcRange[1] = gcDeviation.getVal() * 100;
        }

        this.variantTypeMatch = variantTypeMatch;
        this.tissueIdx = tissueIdx;
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

    public double getGCRangeMin(final int gc) {
        return (gc + gcRange[0]) >= 1 ? (gc + gcRange[0]) : 1;
    }
    public double getGCRangeMax(final int gc) {
        return (gc + gcRange[1]) <= 100 ? (gc + gcRange[1]) : 100;
    }

    public int[] getDisRange() {
        return disRange;
    }

    public int[] getMafRange() {
        return mafRange;
    }

    public int getDisRangeMin(final int distance) {
        return (distance + disRange[0]) >= 1 ? (distance + disRange[0]) : 1;
    }

    public int getDisRangeMax(final int distance) {
        return distance + disRange[1];
    }

    public int getGeneRangeMin(final int geneDis) {
        return (geneDis + geneRange[0]) >= 0 ? (geneDis + geneRange[0]) : 0;
    }
    public int getGeneDisRangeMax(final int geneDis) {
        return geneDis + geneRange[1];
    }

    public int[] getGeneRange() {
        return geneRange;
    }

    public int[] getLdBuddiesRange() {
        return ldBuddiesRange;
    }

    public int getLDBuddiesRangeMin(final int ldBuddies) {
        return (ldBuddies + ldBuddiesRange[0]) >= 0 ? (ldBuddies + ldBuddiesRange[0]) : 0;
    }
    public int getLDBuddiesRangeMax(final int ldBuddies) {
        return ldBuddies + ldBuddiesRange[1];
    }

    public Format getFormat() {
        return format;
    }

    public boolean isCrossChr() {
        return crossChr;
    }

    public void setCrossChr(boolean crossChr) {
        this.crossChr = crossChr;
    }

    public boolean isExcludeInput() {
        return excludeInput;
    }

    public void setExcludeInput(boolean excludeInput) {
        this.excludeInput = excludeInput;
    }

    public boolean isVariantTypeSpecific() {
        return variantTypeSpecific;
    }

    public void setVariantTypeSpecific(boolean variantTypeSpecific) {
        this.variantTypeSpecific = variantTypeSpecific;
    }

    public int getSamplerNumber() {
        return samplerNumber;
    }

    public void setSamplerNumber(int samplerNumber) {
        if(samplerNumber < 1) {
            throw new InvalidArgumentException(String.format("Sampler number should greater than %d.", 0));
        }
        this.samplerNumber = samplerNumber;
    }

    public int getAnnoNumber() {
        return annoNumber;
    }

    public void setAnnoNumber(int annoNumber) {
        if(annoNumber < 1) {
            throw new InvalidArgumentException(String.format("Annotation number should greater than %d.", 0));
        }
        this.annoNumber = annoNumber;
    }

    public boolean isVariantTypeMatch() {
        return variantTypeMatch;
    }

    public void setVariantTypeMatch(boolean variantTypeMatch) {
        this.variantTypeMatch = variantTypeMatch;
    }

    public int getTissueIdx() {
        return tissueIdx;
    }

    public void setTissueIdx(int tissueIdx) {
        this.tissueIdx = tissueIdx;
    }

    public double[] getGcRange() {
        return gcRange;
    }

    public int getGcIdx() {
        return gcIdx;
    }

    public void setGcIdx(int gcIdx, GCDeviation gcDeviation) {
        this.gcIdx = gcIdx;
        this.gcRange = new double[2];
        this.gcRange[0] = - (int)(gcDeviation.getVal() * 100);
        this.gcRange[1] = (int)(gcDeviation.getVal() * 100);
    }
}
