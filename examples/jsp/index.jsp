<%@ page import="org.alicebot.server.core.responder.*" %>
	
<% 
   SmartResponder responder = new SmartResponder(request, response); 
   responder.doResponse();
%>
