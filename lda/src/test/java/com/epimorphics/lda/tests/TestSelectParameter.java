/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static com.epimorphics.util.CollectionUtils.set;
import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.core.APIQuery;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.Term;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestSelectParameter 
    {
	static final PrefixMapping noPrefixes = PrefixMapping.Factory.create().lock();
    
	@Test public void testMe()
        {    
        ShortnameService sns = makeSNS();
        APIQuery q = new APIQuery(sns);
        APIQuery.Param _select = APIQuery.Param.make("_select" );
        String theSelectQuery = "this is a select query";
        q.addFilterFromQuery( _select, set(theSelectQuery) );
        assertEquals( theSelectQuery, q.assembleSelectQuery( noPrefixes) );
        }
    
    @Test public void testCloneIncludesFixedQuery()
        {    
        ShortnameService sns = makeSNS();
        APIQuery q = new APIQuery(sns);
        APIQuery.Param _select = APIQuery.Param.make("_select" );
        String theSelectQuery = "this is a select query";
        q.addFilterFromQuery( _select, set(theSelectQuery) );
        APIQuery cloned = q.clone();
        assertEquals( theSelectQuery, cloned.assembleSelectQuery( noPrefixes ) );
        }

    public static ShortnameService makeSNS() 
        {
    	return new ShortnameService() 
    		{
			@Override public String shorten(String u) 
				{ throw new RuntimeException( "I wasn't expecting to be called." );	}
			
			@Override public String normalizeValue(String val)
				{ throw new RuntimeException( "I wasn't expecting to be called." );	}
			
			@Override public Resource normalizeResource(String s)
				{ throw new RuntimeException( "I wasn't expecting to be called." );	}
			
			@Override public Resource normalizeResource(RDFNode r) 
				{ throw new RuntimeException( "I wasn't expecting to be called." );	}
			
			@Override public Resource normalizeResource(Term r) 
				{ throw new RuntimeException( "I wasn't expecting to be called." );	}
			
			@Override public String normalizeNodeToString(String prop, String val) 
				{ throw new RuntimeException( "I wasn't expecting to be called." );	}
			
			@Override public String expand(String s)
				{ throw new RuntimeException( "I wasn't expecting to be called." );	}
			
			@Override public Context asContext() 
		 		{ throw new RuntimeException( "I wasn't expecting to be called." );	}

			@Override public String normalizeNodeToString(String prop, String val, String language)
				{ throw new RuntimeException( "I wasn't expecting to be called." ); }

			@Override public String normalizeValue(String val, String language) 
				{ throw new RuntimeException( "I wasn't expecting to be called." ); }

			@Override public Any normalizeNodeToRDFQ(String prop, String val, String language) 
				{ throw new RuntimeException( "I wasn't expecting to be called." ); }

    		};
        }
    }
