package org.mulinlab.variantsampler.utils.sort;

import org.mulinlab.variantsampler.utils.node.AbstractNode;
import java.util.Comparator;

public final class DTCTSort implements Comparator<AbstractNode>  {
    public int compare(AbstractNode a, AbstractNode b)
    {
        return (a.getDtct() < b.getDtct() ? -1 : (a.getDtct() == b.getDtct() ? 0 : 1));
    }
}
