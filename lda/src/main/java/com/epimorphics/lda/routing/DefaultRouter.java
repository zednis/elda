/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.routing;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.bindings.Lookup;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.util.URIUtils;

/**
    The default router is a wrapper around a MatchSearcher, qv.
    The adaption is to return a Match object and to uplift the bindings.
    The Router also tracks the URI templates associated with ItemTemplates.
  
  	@author eh
*/
public class DefaultRouter implements Router {
	
	final MatchSearcher<APIEndpoint> self = new MatchSearcher<APIEndpoint>();

	/**
	    Answer the (endpoint, bindings) Match for the given path,
	    or null if there isn't one.
	*/
	@Override public Match getMatch( String path, MultiMap<String, String> queryParams ) {
        Map<String, String> bindings = new HashMap<String, String>();
        APIEndpoint e = self.lookup( bindings, path, queryParams );
        return e == null ? null : new Match( e, bindings );
	}
	
	/**
		Struct holding an api:base value and a URI template.
	*/
	static class BaseAndTemplate {
		final String base;
		final String template;
		
		BaseAndTemplate( String base, String template ) { 
			this.base = base; this.template = template; 
		}
		
		@Override public String toString() {
			return "<base: " + base + "; template: " + template + ">";
			
		}
	}
	
	protected MatchSearcher<BaseAndTemplate> ms = new MatchSearcher<BaseAndTemplate>();
	
	// map from registered uri template to corresponding item template path
	protected Map<String, String> inverseMap = new HashMap<String, String>();
	
	/**
	    Register the endpoint ep associated with the URI template ut.
	    Also record the association between the item template (if any)
	    and that URI template, for use in findItemURIPath.
	*/
	@Override public void register( String context, String ut, APIEndpoint ep ) {
		self.register( ut, ep );
		String it = ep.getSpec().getItemTemplate();
		if (it != null) {
			String apiBase = ep.getSpec().getAPISpec().getBase();
			String path = removeBase( apiBase, it );
			inverseMap.put( ut, path );
			ms.register( path, new BaseAndTemplate( apiBase, ep.getURITemplate() ) );
		}
	}	
	
	@Override public void unregister( String context, String ut ) {
		String it = inverseMap.get( ut );
		self.unregister( ut );
		if (it != null) ms.unregister( it );
	}
	
	/**
	    Answer the filled-in URI template associated with the given
	    item path, or null if there isn't one.
	*/
	@Override public String findItemURIPath( URI requestURI, String path ) {
		Map<String, String> bindings = new HashMap<String, String>();
		BaseAndTemplate bt = ms.lookup( bindings, path, null );
		if (bt != null) {
			String et = Bindings.expandVariables( Lookup.Util.asLookup( bindings ), bt.template );
			// return resolvePath( bt.base, et );
			if (bt.base == null) return et;
			return URIUtils.resolveAgainstBase( requestURI, URIUtils.newURI( bt.base ), et ).toString();
		}
		return null;
	}
	
	/**
		Remove the base from the uri. If the uri starts
		with the base, replace the base in the uri with "/",
		otherwise remove the scheme and authority parts
		of the uri and replace them with "/". 
	*/
	private String removeBase( String base, String uri ) {
		return base == null || !uri.startsWith( base ) 
			? replaceLike(base, uri)
			: "/" + uri.substring( base.length() )
			;
	}

	private String replaceLike(String base, String uri) {
		String uriTail = uri.replaceFirst( "https?://[^/]*/", "/" );
		if (base == null) return uriTail;
		String baseTail = base.replaceFirst( "https?://[^/]*/", "/" );
		return uriTail.startsWith( baseTail ) ? "/" + uriTail.substring( baseTail.length() ) : uriTail;
	}

	@Override public List<String> templates() {
		return self.templates();
	}
}