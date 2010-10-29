package gov.nysenate.opendirectory.solr;

import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
//import java.lang.reflect.Field;

import gov.nysenate.opendirectory.models.Person;

public class SolrSession {

	private SecureLoader loader;
	private Solr solr;
	
	public SolrSession(Person user, Solr solr) {
		this.loader = new SecureLoader(user);
		this.solr = solr;
	}
	
	public Person loadPersonByName(String name) {
		
		//Do the query
		QueryResponse results = solr.query("fullname:"+name);
		SolrDocumentList profiles = results.getResults();
		
		//Return null on no results
		if( profiles.getNumFound() == 0 ) {
			return null;
			
		//Load a person from the profile if 1 result
		} else if ( profiles.getNumFound() == 1 ) {
			return loader.loadPerson(profiles.get(0));
			
		//Throw some sort of exception on multiple matches
		} else {
			//Too many people
			//Throw some kind of error
			return null;
		}
		
	}
	
	//Could use addBean function that comes with solrj but we need to 
	//put the credentials (hashmap) into solr field.
	public void savePerson(Person person) throws SolrServerException, IOException {
		SolrInputDocument solr_person = new SolrInputDocument();
		
		solr_person.addField("firstName", person.getFirstName(), 1.0f);
		solr_person.addField("lastName", person.getLastName(), 1.0f);
		solr_person.addField("Title", person.getTitle(), 1.0f);
		solr_person.addField("id", person.getUid(), 1.0f);
		solr_person.addField("fullName", person.getFullName(), 1.0f);
		solr_person.addField("state", person.getState(), 1.0f);
		solr_person.addField("location", person.getLocation(), 1.0f);
		solr_person.addField("department", person.getDepartment(), 1.0f);
		solr_person.addField("phone", person.getPhone(), 1.0f);
		solr_person.addField("email", person.getEmail(), 1.0f);
		
		//String credentials = new String();
	
		solr.server.add(solr_person);
	}
	
/*	To be figured out... need to figure out annotations with Graylin
 * public AnnotatedField getAnnotatedField(Field field) {
		org.apache.solr.client.solrj.beans.Field lf = field.getAnnotation(org.apache.solr.client.solrj.beans.Field.class);
		if(lf != null) {
			return new AnnotatedField(lf);
		}
		return null;		
	}
*/
	
	public String Credentials(HashMap<String,TreeSet<String>> permissions)
	{
		String credentials = new String();
		
		//permissions.get(key)--> iterate through the TreeSet
		//write to credentials
		//permissions.get("uid").iterator()
		
		return credentials;
		
	}
}
