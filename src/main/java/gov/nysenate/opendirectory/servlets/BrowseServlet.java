package gov.nysenate.opendirectory.servlets;

import gov.nysenate.opendirectory.models.Person;
import gov.nysenate.opendirectory.servlets.utils.BaseServlet;
import gov.nysenate.opendirectory.servlets.utils.Request;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class BrowseServlet extends BaseServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Request self = new Request(this,request,response);
		
		try{
			String command = urls.getCommand(request);
			System.out.println(command);
		    if (command != null) {
		    	if ( command.equals("department")) {
		    		long start = System.nanoTime();
					request.setAttribute(
							"people",
							GetPeopleSortedByString(self,
									Person.class.getMethod("getDepartment"),
									new Person.ByDepartment()
								)
						);
					System.out.println("Sort by Department: "+(System.nanoTime()-start)/1000000f+" milliseconds");
					self.render("dept.jsp");
					
		    	} else if ( command.equals("firstname") ) {
		    		long start = System.nanoTime();
					request.setAttribute(
							"people",
							GetPeopleSortedByChar(self,
									Person.class.getMethod("getFirstName"),
									new Person.ByFirstName()
								)
						);
					System.out.println("Sort by Firstname: "+(System.nanoTime()-start)/1000000f+" milliseconds");
					self.render("first.jsp");
					
		    	} else if ( command.equals("lastname") ) {
		    		long start = System.nanoTime();
					request.setAttribute("people",
							GetPeopleSortedByChar(self,
								Person.class.getMethod("getLastName"),
								new Person.ByLastName())
						);
					System.out.println("Sort by LastName: "+(System.nanoTime()-start)/1000000f+" milliseconds");
		    		self.render("last.jsp");
		    		
		    	} else if ( command.equals("location") ) {
		    		long start = System.nanoTime();
					request.setAttribute(
							"people",
							GetPeopleSortedByString(self,
									Person.class.getMethod("getLocation"),
									new Person.ByLocation()
								)
						);
					System.out.println("Sort by Location: "+(System.nanoTime()-start)/1000000f+" milliseconds");
					self.render("loc.jsp");
					
		    	} else if ( command.equals("all") ) {
		    		request.setAttribute("people", self.solrSession.loadPeople());
		    		self.render("/showall.jsp");
		    	}
		    } else {
		    	self.render("index.jsp");
		    }
		} catch (NoSuchMethodException e) {
			//If the method by name doesn't exist
			System.out.println(e);
		} catch (SecurityException e) {
			//If the method isn't gettable I think? not sure what `security` means
			System.out.println(e);
		}
	}
		
	private HashMap<String,TreeSet<Person>> GetPeopleSortedByChar(Request self, Method method, Comparator<Person> comparator) {
		try {
			long start = System.nanoTime();
			ArrayList<Person> people = self.solrSession.loadPeople();
			System.out.println("loading people in: "+(System.nanoTime()-start)/1000000f+" milliseconds");
			HashMap<String,TreeSet<Person>> data = new HashMap<String,TreeSet<Person>>();
			for(Person p : people) {
				try {
					String value = (String)method.invoke(p);
					if (value!=null && value.length()!=0) {
						value = value.toUpperCase();
						TreeSet<Person> plist = data.get(String.valueOf(value.charAt(0)));
						if (plist == null) {
							plist = new TreeSet<Person>(comparator);
							data.put((String.valueOf(value.charAt(0))), plist);
						}
						plist.add(p);
					}
				} catch (NullPointerException e) {
					//Person doesn't have a `value`
					e.printStackTrace();
				}
			}
			return data;
		} catch (InvocationTargetException e) {
			//Person is not a valid target for the method
			System.out.println(e);
		} catch (IllegalAccessException e) {
			//Method is private and not accessible
			System.out.println(e);
		}
		return null;
	}

	private HashMap<String,TreeSet<Person>> GetPeopleSortedByString(Request self,Method method, Comparator<Person> comparator) {
		try {
			long start = System.nanoTime();
			ArrayList<Person> people = self.solrSession.loadPeople();
			System.out.println((System.nanoTime()-start)/1000000f+" ms - Load People");
			HashMap<String,TreeSet<Person>> data = new HashMap<String,TreeSet<Person>>();
			for(Person p : people) {
				try {
					String value = (String)method.invoke(p);
					if (value!=null && value.length()!=0) {
						TreeSet<Person> plist = data.get(value);
						if (plist == null) {
							plist = new TreeSet<Person>(comparator);
							data.put(value, plist);
						}
						plist.add(p);
					}
				} catch (NullPointerException e) {
					//Person doesn't have a `value`
					e.printStackTrace();
				}
			}
			return data;
		} catch (InvocationTargetException e) {
			//Person is not a valid target for the method
			System.out.println(e);
		} catch (IllegalAccessException e) {
			//Method is private and not accessible
			System.out.println(e);
		}
		return null;
	}
}