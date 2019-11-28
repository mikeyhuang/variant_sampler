package org.mulinlab.variantsampler.utils.node;

import org.mulinlab.varnote.utils.node.LocFeature;

public abstract class AbstractNode {
    protected LocFeature locFeature;
    protected String rsid;
    protected int maf = -1;
    protected double mafOrg = -1;
    protected int dtct;

    public LocFeature getLocFeature() {
        return locFeature;
    }

    public void setLocFeature(LocFeature locFeature) {
        this.locFeature = locFeature;
    }

    public String getRsid() {
        return rsid;
    }

    public void setRsid(String rsid) {
        this.rsid = rsid;
    }

    public int getMaf() {
        return maf;
    }

    public void setMaf(int maf) {
        this.maf = maf;
    }

    public int getDtct() {
        return dtct;
    }

    public void setDtct(int dtct) {
        this.dtct = dtct;
    }

    public double getMafOrg() {
        return mafOrg;
    }

    public void setMafOrg(double mafOrg) {
        this.mafOrg = mafOrg;
    }

    public String getChr() {
        return locFeature.chr;
    }
    public int getPos() {
        return locFeature.beg;
    }
}
