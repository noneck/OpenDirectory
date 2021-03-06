package gov.nysenate.opendirectory.solr;

import gov.nysenate.opendirectory.models.Person;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
//import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
//import org.apache.solr.core.CoreContainer;

public class Solr {
	
	protected SolrServer server;
	protected SolrQuery query;
	
	public Solr() {
		server = null;
		query = new SolrQuery();
	}

	public Solr connect() {
		try {
			/*
			try {
				//Could use EmbeddedSolrServer(), depends on how we are setting up the environments?
				System.setProperty("solr.solr.home", "/usr/local/solr");
				server = (SolrServer)new EmbeddedSolrServer(new CoreContainer.Initializer().initialize(),"");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			*/
			server = (SolrServer)new CommonsHttpSolrServer("http://localhost:8080/solr/");
			
			return this;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public SolrSession newSession(Person user) {
		return new SolrSession(user,this);
	}

	public QueryResponse query(String term) {
		return query(term,Integer.MAX_VALUE);
	}
	
	public QueryResponse query(String term, int results) {
		try {
			query.setQuery(term);
			query.setRows(results);
			
			query.setFields("*","score");
			
			System.out.println(query.getQuery());
			return server.query(query);
			
		} catch (SolrServerException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Solr deleteAll() throws IOException, SolrServerException {
		server.deleteByQuery("*:*");
		return this;
	}
	
	
	public Solr delete(String query) throws IOException, SolrServerException {
		server.deleteByQuery(query);
		server.commit();
		return this;
	}
}