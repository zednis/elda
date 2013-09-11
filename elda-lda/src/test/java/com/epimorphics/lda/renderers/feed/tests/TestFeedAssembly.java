package com.epimorphics.lda.renderers.feed.tests;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;
import org.w3c.dom.Document;

import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.renderers.FeedRenderer;
import com.epimorphics.lda.renderers.FeedRendererFactory;
import com.epimorphics.lda.renderers.FeedRenderer.FeedResults;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.lda.vocabularies.SKOSstub;
import static com.epimorphics.util.CollectionUtils.list;

import com.epimorphics.util.DOMUtils;
import com.epimorphics.util.MediaType;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

import org.custommonkey.xmlunit.*;
import org.custommonkey.xmlunit.exceptions.XpathException;

public class TestFeedAssembly {
	
	protected ShortnameService sns = new SNS("");
	
	protected final MediaType fakeMediaType = new MediaType( "media", "type" );
	
	protected Model configModel = ModelFactory.createDefaultModel();
	
	protected final Resource config = configModel.createResource( "eh:/root" );
	
	static final List<Property> defaultLabelProperties = list
		( API.label
		, SKOSstub.prefLabel
		, RDFS.label
		);
	
	static final List<Property> defaultRightsProperties = list
		( DCTerms.rights
		);
	
	static final List<Property> defaultDateProperties = list
		( DCTerms.modified
		, DCTerms.date
		, DCTerms.dateAccepted
		, DCTerms.dateSubmitted
		, DCTerms.created
		);
	
	@Test public void testRetainsMediaType() {
		FeedRenderer retains_fr = new FeedRenderer( fakeMediaType, config, sns );
		assertSame( fakeMediaType, retains_fr.getMediaType( null ) );
	}
	
	@Test public void testDefaultLabelProperties() {
		FeedRenderer fr = makeFeedRenderer( config );
		List<Property> properties = fr.getLabelProperties(config);
		assertEquals( defaultLabelProperties, properties );
	}
	
	@Test public void testDefaultDateProperties() {
		FeedRenderer fr = makeFeedRenderer( config );
		assertEquals( defaultDateProperties, fr.getDateProperties(config) );
	}
	
	// use the default date properties as a convenient set of examples
	@Test public void testConfiguredSingleDateProperties() {
		for (Property p: defaultDateProperties) {
			assertEquals( list(p), makeDateFeedRenderer(p).getDateProperties(config));			
		}
	}	
	
	// assumes there are at least two default date properties
	@Test public void testConfiguredMultiDateProperties() {
		for (int i = 1; i < defaultDateProperties.size(); i += 1) {
			Property a = defaultDateProperties.get(i-1), b = defaultDateProperties.get(i);
			assertEquals( list(a, b), makeDateFeedRenderer(a, b).getDateProperties(config));			
		}
	}
	
	private FeedRenderer makeDateFeedRenderer(Property ...properties) {
		RDFList l = configModel.createList( properties );
		config.addProperty( EXTRAS.feedDateProperties, l );
		return makeFeedRenderer(config);
	}

	// use the default label properties as a convenient set of examples
	@Test public void testConfiguredSingleLabelProperties() {
		for (Property p: defaultLabelProperties) {
			assertEquals( list(p), makeDateFeedRenderer(p).getDateProperties(config));			
		}
	}
	
	// assumes there are at least two default date properties
	@Test public void testConfiguredMultiLabelProperties() {
		for (int i = 1; i < defaultLabelProperties.size(); i += 1) {
			Property a = defaultLabelProperties.get(i-1), b = defaultLabelProperties.get(i);
			assertEquals( list(a, b), makeDateFeedRenderer(a, b).getDateProperties(config));			
		}
	}
	
	@Test public void testDefaultRightsProperties() {
		FeedRenderer fr = makeFeedRenderer( config );
		assertEquals( defaultRightsProperties, fr.getRightsProperties() );
	}
	
	@Test public void testDeConfiguredRightsProperties() {
		Property a = configModel.createProperty( "eh:/a" );
		Property b = configModel.createProperty( "eh:/b" );
		Property c = configModel.createProperty( "eh:/c" );
		Property[] properties = new Property[] {a, b, c};
		RDFList l = configModel.createList( properties );
		config.addProperty( EXTRAS.feedRightsProperties, l );
		FeedRenderer fr = makeFeedRenderer( config );
		assertEquals( Arrays.asList( properties ), fr.getRightsProperties() );
	}
	
	@Test public void testDefaultFeedRights() {
		FeedRenderer fr = makeFeedRenderer( config );
		assertNull( fr.getFeedRights() );
	}
	
	@Test public void testConfiguredFeedRights() {
		String rightsString = "© copyright 1066";
		config.addProperty( EXTRAS.feedRights, rightsString );
		FeedRenderer fr = makeFeedRenderer( config );
		assertEquals( rightsString, fr.getFeedRights() );
	}
	
	@Test public void testDefaultNamespace() {
		FeedRenderer fr = makeFeedRenderer( config );
		assertEquals( EXTRAS.getURI(), fr.getNamespace() );
	}
	
	@Test public void testRendersFeedRightsIfPresent() {

		String rightsString = "© copyright 1066";
		config.addProperty( EXTRAS.feedRights, rightsString );
		FeedRenderer fr = makeFeedRenderer( config );
		
		Document  d = DOMUtils.newDocument();
		Model dataModel = ModelFactory.createDefaultModel();
		List<Resource> items = new ArrayList<Resource>();
		Resource item = dataModel.createResource( "eh:/item" );
		items.add( item );
		
		FeedResults results = new FeedResults( config, items, new MergedModels( dataModel ) );

		fr.renderFeedIntoDocument( d, new HashMap<String, String>(), results );
		Times t = new Times();
		PrefixMapping pm = PrefixMapping.Factory.create();
		String rendering = DOMUtils.renderNodeToString( t, d, pm );
		
		if (!rendering.contains( "<rights>" + rightsString + "</rights>" )) {
			fail( "rendering\n" + rendering + "\nshould contain rights \n" + rightsString );
		}
	}
	
	@Test public void testRendersEntryRightsIfPresent() throws XpathException {

		String rightsString = "© copyright 1066";
		FeedRenderer fr = makeFeedRenderer( config );
		
		Document  d = DOMUtils.newDocument();
		Model dataModel = ModelFactory.createDefaultModel();
		List<Resource> items = new ArrayList<Resource>();
		Resource item = dataModel.createResource( "eh:/item" );
		
		item.addProperty( DCTerms.rights, rightsString );
		items.add( item );
		
		FeedResults results = new FeedResults( config, items, new MergedModels( dataModel ) );

		fr.renderFeedIntoDocument( d, new HashMap<String, String>(), results );
		Times t = new Times();
		PrefixMapping pm = PrefixMapping.Factory.create();
		String rendering = DOMUtils.renderNodeToString( t, d, pm );
		
		XMLAssert.assertXpathExists( "feed/entry/rights", d );
		
		if (!rendering.contains( "<rigxhts>" + rightsString + "</rights>" )) {
			fail( "rendering\n" + rendering + "\nshould contain rights \n" + rightsString );
		}
	}
	
	@Test public void testConfiguredNamespace() {
		String explicit = "eh:/explicitNamespace";
		config.addProperty( EXTRAS.feedNamespace, explicit );
		FeedRenderer fr = makeFeedRenderer( config );
		assertEquals( explicit, fr.getNamespace() );
	//
		Document  d = DOMUtils.newDocument();
		Model dataModel = ModelFactory.createDefaultModel();
		List<Resource> items = new ArrayList<Resource>();
	//
		Resource item = dataModel.createResource( "eh:/item" );
		item.addProperty( RDFS.label, "Please Look After This Bear" );
		items.add( item );
	//
		FeedResults results = new FeedResults( config, items, new MergedModels( dataModel ) );
		fr.renderFeedIntoDocument( d, new HashMap<String, String>(), results );
	//
		String namespace = explicit;
		Times t = new Times();
		PrefixMapping pm = PrefixMapping.Factory.create();
		String rendering = DOMUtils.renderNodeToString( t, d, pm );
	//
		if (!rendering.contains( "<content xmlns=\"" + namespace + "\"" ))
			fail( "rendering \n" + rendering + "\n does not contain correct content namespace\n" + namespace );
	}
	
	@Test public void testNamespaceEmbedsInContent() {
		
	}
	
	@Test public void testSingleItemXMLrendering() throws XpathException {
		FeedRenderer fr = makeFeedRenderer( config );
		Map<String, String> termBindings = new HashMap<String, String>();
		Document  d = DOMUtils.newDocument();
	//
		Model dataModel = ModelFactory.createDefaultModel();
		List<Resource> items = new ArrayList<Resource>();
	//
		Resource item = dataModel.createResource( "eh:/item" );
		item.addProperty( RDFS.label, "Please Look After This Bear" );
		items.add( item );
	//
		FeedResults results = new FeedResults( config, items, new MergedModels( dataModel ) );
		fr.renderFeedIntoDocument( d, termBindings, results );
	//
		XMLAssert.assertXpathExists( "feed", d );
		XMLAssert.assertXpathExists( "feed/title", d );
		XMLAssert.assertXpathExists( "feed/link", d );
		XMLAssert.assertXpathExists( "feed/id", d );
		XMLAssert.assertXpathExists( "feed/updated", d );
		XMLAssert.assertXpathExists( "feed/entry", d );
	//
		XMLAssert.assertXpathExists( "feed/entry/title", d );
		XMLAssert.assertXpathExists( "feed/entry/updated", d );
		XMLAssert.assertXpathExists( "feed/entry/id", d );
		XMLAssert.assertXpathExists( "feed/entry/content", d );
	//
		XMLAssert.assertXpathExists( "feed/entry/content/label", d );
	//
		// TODO check that shortname translated to correct namespace.
		XMLAssert.assertXpathExists( "feed/entry/content/label", d );
		
		String namespace = "http://www.epimorphics.com/vocabularies/lda#";
		Times t = new Times();
		PrefixMapping pm = PrefixMapping.Factory.create();
		String rendering = DOMUtils.renderNodeToString( t, d, pm );
	//
		if (!rendering.contains( "<content xmlns=\"" + namespace + "\"" ))
			fail( "rendering \n" + rendering + "\n does not contain correct content namespace\n" + namespace );
	}
	
	protected FeedRenderer makeFeedRenderer( Resource config ) {
		return new FeedRenderer( FeedRendererFactory.atom, config, sns );
	}

}