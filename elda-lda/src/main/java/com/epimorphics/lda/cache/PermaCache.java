/**
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.cache;

import java.util.List;

import com.epimorphics.lda.core.APIResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    The cache that remembers everything (until the world explodes
    with an OOM) -- a LimitedCache with no limits.
*/
public class PermaCache extends LimitedCacheBase implements Cache {

    public PermaCache( String label ) {
        super(Clock.SystemClock, label);
    }

    @Override protected synchronized boolean exceedsSelectLimit( Cachelet<String, TimedThing<List<Resource>>> m ) {
        return false;
    }

    @Override protected synchronized boolean exceedsResultSetLimit( Cachelet<String, TimedThing<APIResultSet>> m ) {
        return false;
    }
}