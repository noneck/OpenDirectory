package gov.nysenate.opendirectory.servlets;

import gov.nysenate.opendirectory.ldap.Ldap;
import gov.nysenate.opendirectory.models.Person;
import gov.nysenate.opendirectory.servlets.utils.BaseServlet;
import gov.nysenate.opendirectory.servlets.utils.Request;

import java.io.IOException;
import java.util.TreeSet;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrServerException;

@SuppressWarnings("serial")
public class SolrControllerServlet extends BaseServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Request self = new Request(this,request,response);
		
		try {
			ServletOutputStream out = response.getOutputStream();
			String command = urls.getCommand(request);
		    if (command != null) {
		    	if(command.equals("removeAll")) {
		    		removeAll(self);
		    		out.println("Removed all documents");
		    	} else if (command.equals("indexAll")) {
		    		indexLdap(self);
		    		indexExtras(self);
		    		out.println("Indexed all Documents");
		    	} else if (command.equals("indexExtras")) {
		    		indexExtras(self);
		    		out.println("Indexed all Extras");
		    	} else if (command.equals("reindexAll")) {
		    		removeAll(self);
		    		indexLdap(self);
		    		indexExtras(self);
		    		out.println("Removed and Reindexed All documents");
		    	} else {
		    		out.println("Unknown command: "+command);
		    		out.println("Recognized Commands are: removeAll,indexAll, and reindexAll");
		    	}
		    } else {
		    	out.println("Available commands are: ");
		    	out.println("\tRemoveAll - /solr/removeAll");
		    	out.println("\tIndexAll - /solr/indexAll");
		    	out.println("\tReindexAll - /solr/reindexAll");
		    }
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
	}
	
	private void removeAll(Request self) throws SolrServerException, IOException {
		self.solrSession.deleteAll();
	}
	
	private void indexLdap(Request self) throws SolrServerException, IOException  {
		try{
			self.solrSession.savePeople(new Ldap().connect().getPeople());
			self.solrSession.optimize();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	private void indexExtras(Request self) throws SolrServerException, IOException {
		Person opendirectory = new Person();
		opendirectory.setFullName("OpenDirectory");
		opendirectory.setUid("opendirectory");
		TreeSet<String> cred_default = new TreeSet<String>();
		cred_default.add("public");
				
		opendirectory.setPermissions(Person.getDefaultPermissions());
		opendirectory.setCredentials(cred_default);
		self.solrSession.savePerson(opendirectory);
		self.solrSession.optimize();
	}
}
