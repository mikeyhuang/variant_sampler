package org.mulinlab.variantsampler.query;

import org.mulinlab.variantsampler.utils.node.Node;
import org.mulinlab.variantsampler.utils.sort.BegSort;
import org.mulinlab.varnote.filters.iterator.LineFilterIterator;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.util.*;

public class InputStack {

    private List<String> chrList;
    private Map<String, Integer> chr2idMap;
    private Map<String, Map<Integer, Short>> inputs;
    private Map<String, Map<Integer, List<Short>>> inputsMultiple;

    private int count = 0;

    public InputStack(final LineFilterIterator iterator) {

        chrList = new ArrayList<>();
        inputs = new HashMap<>();
        inputsMultiple = new HashMap<>();

        Map<Integer, Short> chrMap;
        Map<Integer, List<Short>> chrMultiMap;

        LocFeature locFeature = null;

        while (iterator.hasNext()) {
            locFeature = iterator.next();

            if(locFeature != null) {
                count++;

                if(inputs.get(locFeature.chr) == null) {
                    chrMap = new HashMap<>();
                    chrMultiMap = new HashMap<>();
                    chrList.add(locFeature.chr);
                } else {
                    chrMap = inputs.get(locFeature.chr);
                    chrMultiMap = inputsMultiple.get(locFeature.chr);
                }

                if(chrMap.get(locFeature.beg) == null) {
                    chrMap.put(locFeature.beg, (short)(locFeature.end - locFeature.beg));
                } else {
                    List<Short> ends = chrMultiMap.get(locFeature.beg);
                    if(ends == null) {
                        ends = new ArrayList<>();
                        ends.add((short)(locFeature.end - locFeature.beg));
                    }
                    chrMultiMap.put(locFeature.beg, ends);
                }
                inputs.put(locFeature.chr, chrMap);
                inputsMultiple.put(locFeature.chr, chrMultiMap);
            }
        }
        Collections.sort(chrList);

        chr2idMap = new HashMap<>();
        for (int i = 0; i < chrList.size(); i++) {
            chr2idMap.put(chrList.get(i), i);
        }
        iterator.close();
    }

    public int getCount() {
        return count;
    }

    public List<LocFeature> getLocFeatureForChr(final List<LocFeature> alllist, final String chr) {
        Map<Integer, Short> locs = inputs.get(chr);
        Map<Integer, List<Short>> mlocs = inputsMultiple.get(chr);

        List<LocFeature> list = new ArrayList<>();
        for (Integer loc: locs.keySet()) {
            list.add(new LocFeature(loc, loc + locs.get(loc), chr));
        }

        for (Integer loc: mlocs.keySet()) {
            for (Short end: mlocs.get(loc)) {
                list.add(new LocFeature(loc, loc + end, chr));
            }
        }

        Collections.sort(list, new BegSort());
        for (LocFeature locFeature: list) {
            alllist.add(locFeature);
        }
        return list;
    }

    public List<LocFeature> getLocFeatureForAll(final List<LocFeature> list) {
        for (String chr: inputs.keySet()) {
            getLocFeatureForChr(list, chr);
        }

        return list;
    }

    public List<String> getChrList() {
        return chrList;
    }

    public boolean isNodeInQuery(Node node) {
        LocFeature locFeature = node.getLocFeature();
        Map<Integer, Short> chrMap = inputs.get(locFeature.chr);
        Map<Integer, List<Short>> chrMultiMap = inputsMultiple.get(locFeature.chr);
        if(chrMap.get(locFeature.beg) != null && chrMap.get(locFeature.beg) == (locFeature.end - locFeature.beg)) {
            return true;
        } else {
            List<Short> ends = chrMultiMap.get(locFeature.beg);
            if(ends != null) {
                for (Short end: ends) {
                    if(end == (locFeature.end - locFeature.beg)) {
                        return true;
                    }
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public int chr2id(final String chr) {
        return chr2idMap.get(chr);
    }
}
