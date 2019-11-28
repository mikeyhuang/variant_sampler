package org.mulinlab.variantsampler.utils.enumset;

import org.mulinlab.variantsampler.utils.GP;

public enum LD {
    GT1(0, "ld>0.1"), GT2(1, "ld>0.2"), GT3(2, "ld>0.3"), GT4(3, "ld>0.4"),
    GT5(4, "ld>0.5"), GT6(5, "ld>0.6"), GT7(6, "ld>0.7"), GT8(7, "ld>0.8"),
    GT9(8, "ld>0.9");

    private final int idx;
    private final String title;

    LD(final int idx, final String title) {
        this.idx = idx;
        this.title = title;
    }

    public int getIdx() {
        return idx;
    }

    public String getTitle() {
        return title;
    }

    public static LD getVal(final int index) {
        return  LD.values()[index];
    }

    public static int getIdx(final LD ld) {
        if (ld == null) {
            return GP.DEFAULT_GENE_LD.getIdx();
        } else {
            for (LD ld1:LD.values()) {
                if(ld1 == ld) {
                    return ld1.getIdx();
                }
            }
            return GP.DEFAULT_GENE_LD.getIdx();
        }
    }
}
