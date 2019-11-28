package org.mulinlab.variantsampler.utils.enumset;

public enum MAFRange {
    R1(0.01), R2(0.02), R3(0.03), R4(0.04), R5(0.05), R6(0.06), R7(0.07), R8(0.08), R9(0.09), R10(0.1);

    private final double value;
    MAFRange(final double value) {
        this.value = value;
    }

    public double getVal() {
        return value;
    }
    public static MAFRange getVal(final int index) {
        return  MAFRange.values()[index];
    }
}
