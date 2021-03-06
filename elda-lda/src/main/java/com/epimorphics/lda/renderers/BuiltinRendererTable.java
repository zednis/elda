/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers;

import java.util.HashMap;
import java.util.Map;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.shortnames.CompleteContext;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    The built-in table of renderers, by name. Includes those defined
    by the LDA spec.
    
 	@author chris
*/
public class BuiltinRendererTable {

	/**
	    Any subclass of DoingWith has withRoot and withMediaType methods
	    that do nothing, returning their receiver. (Most renderer factories
	    don't need, or can't have, specialisations with different media
	    types or config resources; it's the XSLT renderer that does.)
	*/
	abstract static class DoingWith implements RendererFactory {
		
		@Override public RendererFactory withRoot( Resource r ) {
			return this;
		}
		
		@Override public RendererFactory withMediaType( MediaType mt ) {
			return this;
		}
	}
	
	static private Factories factoryTable = new Factories();
	
	static private Resource Empty = ModelFactory.createDefaultModel().createResource();
	
	static private Map<Resource, RendererFactory> builtins = new HashMap<Resource, RendererFactory>();
	
	static void putFactory( String name, Resource type, MediaType mt, RendererFactory rf ) {
		factoryTable.putFactory( name, null, mt, rf );
		builtins.put( type, rf );
	}
	
	static void putDefaultFactory( String name, Resource type, MediaType mt, RendererFactory rf ) {
		factoryTable.putFactory( name, null, mt, rf, true );
		builtins.put( type, rf );
	}
	
	public static RendererFactory getFactory( Resource type ) {
		return builtins.get( type );
	}
	
	static {
		putFactory( "text", API.RdfXmlFormatter, MediaType.TEXT_PLAIN, new DoingWith() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new JSONRenderer( CompleteContext.Mode.PreferLocalnames, ep, MediaType.TEXT_PLAIN );
			}
			} );
		
		putFactory( "ttl", API.TurtleFormatter, MediaType.TEXT_TURTLE, new DoingWith() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new TurtleRenderer();
			}
			} );
		
		 putFactory( "rdf", API.RdfXmlFormatter, MediaType.APPLICATION_RDF_XML, new DoingWith() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new RDFXMLRenderer();
			}
			} );
		
		putDefaultFactory( "json", API.JsonFormatter, MediaType.APPLICATION_JSON, new JSONRendererFactory( MediaType.APPLICATION_JSON ) );
		
		putFactory( "_jsonp", API.JsonFormatter, MediaType.TEXT_JAVASCRIPT, new JSONRendererFactory( MediaType.TEXT_JAVASCRIPT ) );
		
		putFactory( "_jsonp", API.JsonFormatter, MediaType.APPLICATION_JAVASCRIPT, new JSONRendererFactory( MediaType.APPLICATION_JAVASCRIPT ) );
		
		putFactory( "xml", API.XmlFormatter, MediaType.TEXT_XML, new DoingWith() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new XMLRenderer( CompleteContext.Mode.PreferLocalnames, sns, MediaType.TEXT_XML, null );
			}
			} );
		
		putFactory( "xml", API.XmlFormatter, MediaType.APPLICATION_XML, new XMLRendererFactory() );
		
		putFactory( "_velocity", EXTRAS.VelocityFormatter, MediaType.NONE, new VelocityRendererFactory() );
		
		putFactory( "_atom", EXTRAS.FeedFormatter, MediaType.NONE, new FeedRendererFactory() );
		
		putFactory( "_xslt", API.XsltFormatter, MediaType.NONE, new XSLT_RendererFactory( Empty, MediaType.NONE ) );
		
		putFactory( "html", API.HtmlFormatter, MediaType.TEXT_HTML, new DoingWith() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new HTMLRenderer();
			}
			} );
	}

	public static Factories getBuiltinRenderers() {
		return factoryTable.copy();
	}
	
	public static boolean isRendererType( Resource r ) {
		return builtins.get( r ) != null;
	}

}
