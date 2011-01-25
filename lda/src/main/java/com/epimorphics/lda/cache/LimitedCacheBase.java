/**
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.APIResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

public abstract class LimitedCacheBase implements Cache {

    static Logger log = LoggerFactory.getLogger( LimitedCacheBase.class );
    
    protected abstract boolean exceedsSelectLimit( Map<String, List<Resource>> m) ;
    
    protected abstract boolean exceedsResultSetLimit( Map<String, APIResultSet> m );
    
    private final Map<String, APIResultSet> cd = new HashMap<String, APIResultSet>();
    
    private final Map<String, List<Resource>> cs = new HashMap<String, List<Resource>>();
    
    @Override public APIResultSet getCachedResultSet( List<Resource> results, String view ) { 
        return cd.get( results.toString() + "::" + view );
    }
    
    @Override public List<Resource> getCachedResources( String select ) { 
        return cs.get( select );
    }
    
    @Override public void cacheDescription( List<Resource> results, String view, APIResultSet rs ) {
        log.debug( "caching descriptions for resources " + results );
        cd.put( results.toString() + "::" + view, rs );   
        if (exceedsResultSetLimit( cd )) cd.clear();
    }

	@Override public void cacheSelection( String select, List<Resource> results ) {
        log.debug( "caching resource selection for query " + select );
        cs.put( select, results );        
        if (exceedsSelectLimit( cs )) cs.clear();
    }

	public void clear() {
    	cs.clear();
    	cd.clear();
    }
}