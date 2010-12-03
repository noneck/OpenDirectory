package gov.nysenate.opendirectory.servlets;

import gov.nysenate.opendirectory.ldap.Ldap;
import gov.nysenate.opendirectory.models.Person;
import gov.nysenate.opendirectory.servlets.utils.BaseServlet;
import gov.nysenate.opendirectory.servlets.utils.Request;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.solr.client.solrj.SolrServerException;


@SuppressWarnings("serial")
public class UserServlet extends BaseServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Request self = new Request(this,request,response);
		
		String command = urls.getCommand(request);
	    if(command != null) {
	    	if(command.equals("login")) {
	    		String uid = (String)self.httpSession.getAttribute("uid"); 
	    		if( uid == null) {
	    			self.render("login.jsp");
	    		} else {	    			
	    			self.redirect(urls.url("person",uid));
	    		}
	    		
	    	} else if (command.equals("logout")) {
    			self.httpSession.removeAttribute("uid");
	    		self.redirect(urls.url("index"));
	    		
	    	} else if (command.equals("edit")) {
	    		self.httpRequest.setAttribute("person", self.user);
	    		self.render("EditProfile.jsp");
	    		
	    	} else if (command.equals("bookmarks")) {
	    		Vector<String> args = urls.getArgs(request);
	    		if(args.isEmpty()) {
	    			self.render("bookmarks.jsp");
	    			
	    		} else {
	    			if(args.size()==2) {
		    			if(args.get(0).equals("add")) {
		    	    		try {
		    		    		self.user.getBookmarks().add(self.solrSession.loadPersonByUid(args.get(1)));
		    					self.solrSession.savePerson(self.user);
		    					self.redirect(urls.url("person",args.get(1)));
		    				} catch (SolrServerException e) {
		    					// TODO Auto-generated catch block
		    					e.printStackTrace();
		    				}
		    				
			    		} else if(args.get(0).equals("remove")) {
			    			try {
			    				ArrayList<Person> bookmarks = self.user.getBookmarks();
				    			for(Person p : bookmarks)
				    				if(p.getUid().equals(args.get(1))) {
				    					bookmarks.remove(p);
				    					System.out.println("Now only "+bookmarks.size()+" bookmarks");
				    					break;
				    				}
		    					self.solrSession.savePerson(self.user);
		    					self.redirect(urls.url("user","bookmarks"));
		    				} catch (SolrServerException e) {
		    					// TODO Auto-generated catch block
		    					e.printStackTrace();
		    				}
			    		} else {
			    			//Exception!!!!
			    			System.out.println("Bad bookmarks command: "+args.get(0));
			    		}
	    			} else {
	    				//Improper number of arguments
	    				System.out.println("Improper number of arguments: "+args.size());
	    			}
	    		}
	    	}
	    }
		
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Request self = new Request(this,request,response);
		
	    String command = urls.getCommand(request);
	    if(command != null) {
	    	if(command.equals("login")) {
	    		String cred = (String)request.getParameter("name");
	    		String pass = (String)request.getParameter("password");
	    		
	    		//Check to see if they are already logged in
	    		if(self.httpSession.getAttribute("uid") != null ) {
	    			self.redirect(urls.url("person",cred));
	    			
    			// Check to see if they are in our database
	    		} else if (self.solrSession.loadPersonByUid(cred) != null) {
		    		try {
		    			//Try to connect
		    			if ( (cred.equalsIgnoreCase("opendirectory") && pass.equals("senbook2010")) || Ldap.authenticate(cred,pass)) {
		    				self.httpSession.setAttribute("uid",cred);
		    				self.redirect(urls.url("person",cred));
		    			} else {
			    			request.setAttribute("errorMessage", "Username and/or password were incorrect.");
			    			self.render("/login.jsp");
		    			}
		    			
		    		} catch (NamingException e) {
		    			//LDAP is down or something?
		    			e.printStackTrace();
		    			request.setAttribute("errorMessage", "The ldap server is down and you can't be authenticated. Please try again later.");
		    			self.render("login.jsp");
		    		}
	    		
	    		//Person is not in our database
	    		} else {
	    			request.setAttribute("errorMessage", "The user you have specified `"+cred+"` is invalid for the system.");
	    			self.render("login.jsp");
	    		}
	    	} else if (command.equals("edit")) {
	    		self.httpRequest.setAttribute("person", self.user);
	    		self.httpRequest.setAttribute("message", "Changes Saved. <a href=\""+urls.url("person",self.user.getUid())+"\">View your profile.</a>");
	    		
	    		Enumeration<?> parameters = self.httpRequest.getParameterNames();
	    		while(parameters.hasMoreElements()) {
	    			String key = (String)parameters.nextElement();
	    			String value = (String)self.httpRequest.getParameter(key);
	    			if(key.startsWith("radio_")) {
	    				key = key.substring(6);
	    				System.out.println("Key "+key+":\t\tValue: "+value);
	    				self.user.getPermissions().put(key, new TreeSet<String>(Arrays.asList(value.toLowerCase())));
	    			} else {
	    				setFieldValue(self.user,key,value);
	    			}
	    		}


	    		
	    		try {
	    			ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
	    			ArrayList<FileItem> files = (ArrayList<FileItem>)upload.parseRequest(request);
					for(FileItem item : files) {
						if(!item.isFormField()) {
							try {
								String filename = self.user.getUid()+item.getName().substring(item.getName().lastIndexOf('.'));
								String fullname = "/home/opendirectory/OpenDirectory/src/main/webapp/img/avatars/"+filename;
								System.out.println("Writing to: "+fullname);
								File file = new File(fullname);
								item.write(file);
								self.user.setPicture("img/avatars/"+filename);
								self.solrSession.savePerson(self.user);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				} catch (FileUploadException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	    		
	    		try {
					self.solrSession.savePerson(self.user);
				} catch (SolrServerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		self.render("EditProfile.jsp");
	    		
	    	} else {
	    		self.redirect(urls.url("index"));
	    	}
	    }
	}
	
	private void setFieldValue(Person person, String fieldname, String fieldvalue) {
		//Switch in the fieldname for speed
		if(fieldname.equals("bio"))
			person.setBio((String)fieldvalue);
		else if(fieldname.equals("department"))
			person.setDepartment((String)fieldvalue);
		else if(fieldname.equals("email"))
			person.setEmail((String)fieldvalue);
		else if(fieldname.equals("email2"))
			person.setEmail2((String)fieldvalue);
		else if(fieldname.equals("facebook"))
			person.setFacebook((String)fieldvalue);
		else if(fieldname.equals("firstName"))
			person.setFirstName((String)fieldvalue);
		else if(fieldname.equals("fullName"))
			person.setFullName((String)fieldvalue);
		else if(fieldname.equals("interests"))
			person.setInterests(loadStringSet((String)fieldvalue));
		else if(fieldname.equals("irc"))
			person.setIrc((String)fieldvalue);
		else if(fieldname.equals("lastName"))
			person.setLastName((String)fieldvalue);
		else if(fieldname.equals("linkedin"))
			person.setLinkedin((String)fieldvalue);
		else if(fieldname.equals("location"))
			person.setLocation((String)fieldvalue);
		else if(fieldname.equals("phone"))
			person.setPhone((String)fieldvalue);
		else if(fieldname.equals("phone2"))
			person.setPhone2((String)fieldvalue);
		else if(fieldname.equals("picture"))
			person.setPicture((String)fieldvalue);
		else if(fieldname.equals("skills"))
			person.setSkills(loadStringSet((String)fieldvalue));
		else if(fieldname.equals("state"))
			person.setState((String)fieldvalue);
		else if(fieldname.equals("title"))
			person.setTitle((String)fieldvalue);
		else if(fieldname.equals("twitter"))
			person.setTwitter((String)fieldvalue);
	}
	
	public HashMap<String,String> loadStringHash(String str) {
		HashMap<String,String> map = new HashMap<String,String>();
		if(str!=null) {
			String[] parts = str.split(":");
			for(int i=0; i<parts.length-1; i+=2) {
				map.put(parts[i],parts[i+1]);
			}
		}
		return map;
	}
	
	public HashMap<String,TreeSet<String>> loadSetHash(String str) {
		HashMap<String,TreeSet<String>> map = new HashMap<String,TreeSet<String>>();
		if(str!=null) {
			String[] parts = str.split(":");
			for(int i=0; i<parts.length-1; i+=2)
				map.put(parts[i], loadStringSet(parts[i+1]));
		}
		return map;
	}
	
	public TreeSet<String> loadStringSet(String str) {
		if(str==null)
			return new TreeSet<String>();
		return new TreeSet<String>(Arrays.asList(str.split(", ")));
	}
}
