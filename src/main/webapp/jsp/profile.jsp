<%@ page language="java" import="gov.nysenate.opendirectory.models.Person,gov.nysenate.opendirectory.utils.UrlMapper" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%

	UrlMapper urls = (UrlMapper)request.getAttribute("urls");
	Person person = (Person)request.getAttribute("person");
	Person user = (Person)request.getAttribute("user");
	
%><jsp:include page="header.jsp" />
			<div id="main">
				<div id="pic">
					<% if(person.getPicture()!=null && !person.getPicture().isEmpty()) { %>
						<img src="<%=person.getPicture()%>" width="150" height="200">
					<% } else { %>
						<img src="<%=urls.url("img","einstein.jpg")%>" width="150" height="200">
					<% } %>
				</div>
				<div id="top_info" class="right">
				
					<div id="name">
						<b><%=person.getFullName()%></b>
					</div>
					<div id="info">
						<% 	String lineone = "";
							if(person.getTitle()!=null && !person.getTitle().isEmpty())
								lineone+=person.getTitle();
							if(person.getLocation()!=null && !person.getLocation().isEmpty()) {
								if(!lineone.isEmpty())
									lineone+=", ";
								lineone+=person.getLocation();
							}
							if(person.getDepartment()!=null && !person.getDepartment().isEmpty()) {
								if(!lineone.isEmpty())
									lineone+=" - ";
								lineone+=person.getDepartment();
							}
							lineone =  "<p>"+lineone+"</p>";
							out.println(lineone);
							
							String linetwo = "";
							if(person.getPhone()!=null && !person.getPhone().isEmpty())
								linetwo+=person.getPhone();
							if(person.getEmail()!=null && !person.getEmail().isEmpty()) {
								if(!linetwo.isEmpty())
									linetwo+=", ";
								linetwo+=person.getEmail();
							}
							linetwo = "<p>"+linetwo+"</p>";
							out.println(linetwo);
						
						if(user!=null && user.getUid()!=null && !user.getUid().equals(person.getUid())) { 
								if(!user.getBookmarks().contains(person)) {
									%><a href="<%=urls.url("user","bookmarks","add", person.getUid())%>"> Add me to your bookmarks!</a> <br></br><%
								} else {
									%><a href="<%=urls.url("user","bookmarks","remove", person.getUid())%>"> Remove me from your bookmarks!</a><br/><br/><%
								}
						} %>
						<a href="<%=urls.url("person", person.getUid(), "vcard")%>"> Download VCard </a>
						<br></br>
					</div>
					<div class="clear"></div>
				</div>
				<br></br>
				<br></br>
				<div id="bio">
					<% if(person.getBio()!=null && !person.getBio().isEmpty()) { %>
						<b>Biography</b>
						<p><%=person.getBio() %></p>
					<% }  else { %>
						<b>Biography</b>
						<p>Information Not Available</p>
					<% } %>
				</div>
		</div>
				
			<div id="interests">
			<% if(person.getInterests()!=null && !person.getInterests().isEmpty()) { %>
				
					<b>Interests</b>
					<p><%
						String last = person.getInterests().last();
						for( String interest : person.getInterests() ) {
							out.print("<a href=\""+urls.url("search","?query=interests:("+interest+")")+"\">"+interest+"</a>");
							if(!interest.equals(last))
								out.println(", ");
						}
					%></p>
					
			<% } else { %>
					<b>Interests</b>
					<p>Information Not Available</p>
			<% } %>
			</div>
			
			<div id="skills">
			<% if(person.getSkills()!=null && !person.getSkills().isEmpty()) { %>
				
					<b>Skills</b>
					<p><%
						String last = person.getSkills().last();
						for( String skill : person.getSkills() ) {
							out.print("<a href=\""+urls.url("search","?query=skills:("+skill+")")+"\">"+skill+"</a>");
							if(!skill.equals(last))
								out.println(", ");
						}
					%></p>
					
			<% } else { %>
					<b>Skills</b>
					<p>Information Not Available</p>
				
			<% } %>
			</div>
			
			<div id="add_info">
				<b>Additional Information</b>
				
				<% if(person.getEmail2()!= null && !person.getEmail2().isEmpty()) { %>
					<p>E-mail: <%=person.getEmail2() %></p>
				<% } else { %>
					<p>E-mail: Information Not Available</p>
				<% } %>
				
				<% if(person.getPhone2()!= null && !person.getPhone2().isEmpty()) { %>
					<p>Phone: <%=person.getPhone2() %></p>
				<% } else { %>
					<p>Phone: Information Not Available</p>
				<% } %>
			
				<% if(person.getIrc()!= null && !person.getIrc().isEmpty()) { %>
					<p>IRC: <%=person.getIrc() %></p>
				<% } else { %>
					<p>IRC: Information Not Available</p>
				<% } %>
			
				<% if(person.getFacebook()!= null && !person.getFacebook().isEmpty()) { %>
					<a href="<%=person.getFacebook() %>">Facebook</a>
				<% } else { %>
					<p>Facebook: Information Not Available</p>
				<% } %>
				
				<% if(person.getTwitter()!= null && !person.getTwitter().isEmpty()) { %>
					<a href="<%=person.getTwitter() %>">Twitter</a>
				<% } else { %>
					<p>Twitter: Information Not Available</p>
				<% } %>
				
				<% if(person.getLinkedin()!= null && !person.getLinkedin().isEmpty()) { %>
					<a href="<%=person.getLinkedin() %>">LinkedIn</a>
				<% } else { %>
					<p>LinkedIn: Information Not Available</p>
				<% } %>
			
			</div>
			
		</div>
<jsp:include page="footer.jsp"/>