package org.mulinlab.snpsnap.utils;

public final class Node {
    final private String chr;
    final private long bp;
    final private String rsid;
    final private int maf;
    final private String mafs;
    final private int distance;

    public Node(final String[] tokens) {
        chr = tokens[0];
        bp = Long.parseLong(tokens[1]);
        rsid = tokens[2];
        maf = Integer.parseInt(tokens[3]);
        mafs = tokens[4];
        distance = Integer.parseInt(tokens[5]);
    }

    public String getChr() {
        return chr;
    }

    public long getBp() {
        return bp;
    }

    public String getRsid() {
        return rsid;
    }

    public int getMaf() {
        return maf;
    }

    public int getDistance() {
        return distance;
    }

    public String getMafs() {
        return mafs;
    }
}
