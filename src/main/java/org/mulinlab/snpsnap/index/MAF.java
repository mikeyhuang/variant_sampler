package org.mulinlab.snpsnap.index;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public final class MAF {
    private int maf;
    private long address;
    private List<Pair<Integer, Long>> distances;

    public MAF(final int maf, final long address) {
        this.maf = maf;
        this.address = address;
        this.distances = new ArrayList<>();
    }

    public int getMaf() {
        return maf;
    }

    public long getAddress() {
        return address;
    }

    public void addDistance(final Pair<Integer, Long> distance) {
        distances.add(distance);
    }

    public List<Pair<Integer, Long>> getDistances() {
        return distances;
    }
}
