/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.rdfq;

public abstract class Term extends Any 
	{
	public abstract Term replaceBy( String r );
	
	public abstract String spelling();
	
	public StringBuilder renderWrapped( StringBuilder out )
		{ return out.append( asSparqlTerm() ); }
	}