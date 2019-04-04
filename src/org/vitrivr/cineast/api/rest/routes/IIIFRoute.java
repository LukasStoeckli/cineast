package org.vitrivr.cineast.api.rest.routes;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.vitrivr.cineast.api.rest.exceptions.MethodNotSupportedException;
import org.vitrivr.cineast.api.rest.iiif.IIIFRequest;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;


public class IIIFRoute implements Route {


  private static final Logger LOGGER = LogManager.getLogger();

  ObjectMapper mapper = new ObjectMapper();



  @Override
  public Object handle(Request request, Response response) throws Exception {
    System.out.println("\n- * - * - * - * - * -");
    System.out.println("client ip: " + request.ip());
    System.out.println("client user-agent: " + request.userAgent());
    System.out.println("method: " + request.requestMethod());
    System.out.println("type: " + request.contentType());
    System.out.println("protocol: " + request.protocol());
    System.out.println("headers: " + request.headers());
    System.out.println("body: " + request.body());


    switch(request.requestMethod()) {
      case "GET":
        response.type("text/html");
        return getHelp();
      case "POST":
        response.type("application/json");
        return handleRequest(request);
      default:
        throw new MethodNotSupportedException(request);
    }
  }



  private String handleRequest(Request request) {
    JsonNode json = null;
    ObjectNode response = mapper.createObjectNode();

    try {
      json = mapper.readTree(request.body());
    } catch (IOException e) {
      response.put("status", 500);
      response.put("errorMessage", "could not read json");
      return response.toString();
    }

    IIIFRequest requestObject = validate(json);
    if (requestObject.getRequestError() != null) {
      response = requestObject.getRequestError();
    } else {
      // start process here

      response.put("status","200");
      response.put("institution", requestObject.getInstitution());
      response.put("numberOfResources", requestObject.getContent().size());
      response.put("identifier", 0);
    }

    return response.toString();
  }



  private String getHelp() {
    ObjectNode help = mapper.createObjectNode();
    help.put("title","IIIF content registration");
    help.put("description","cineast api to register content for retrieval.");
    help.put("endpoint", "/iiif/register");
    help.put("method","POST");
    help.put("type", "application/json");
    // add definition of fields
    // add example request
    // add example responses
    return "<h1>IIIF Endpoint</h1><p>Add API specification here</p>";//help.toString();
  }




  public IIIFRequest validate(JsonNode json) {
    String institution;
    String iiifEndpoint;
    JsonNode content;
    ArrayNode errorMsg = mapper.createArrayNode();
    IIIFRequest requestObject = new IIIFRequest();

    // institution
    if (json.has("institution")) {
      institution = json.get("institution").asText();
      if (institution.equals("")) { errorMsg.add("empty value for institution"); }
      requestObject.setInstitution(institution);
    } else { errorMsg.add("missing value for institution"); }

    // iiifEndpoint
    if (json.has("iiifEndpoint")) {
      iiifEndpoint = json.get("iiifEndpoint").asText();
      if (iiifEndpoint.equals("")) { errorMsg.add("empty value for iiifEndpoint"); }
      requestObject.setEndpoint(iiifEndpoint);
    } else { errorMsg.add("missing value for iiifEndpoint"); }

    // content
    if (json.has("content")) {
      content = json.get("content");
      if (content.size() == 0) { errorMsg.add("content must be nonempty array"); }
      for (int i = 0; i < content.size(); i++) {
        System.out.println(i + "th elem: " + content.get(i));
        if (content.get(i).has("project") && !content.get(i).get("project").equals("")) {
          if (content.get(i).has("image") && !content.get(i).get("image").equals("")) {
            if (content.get(i).has("meta") && !content.get(i).get("meta").equals("")) {
              String project = content.get(i).get("project").asText();
              String image = content.get(i).get("image").asText();
              String meta = content.get(i).get("meta").asText();
              requestObject.addContent(project, image, meta);
            } else { errorMsg.add("missing value for meta in element " + (i+1)); }
          } else { errorMsg.add("missing value for image in element " + (i+1)); }
        } else { errorMsg.add("missing value for project in element " + (i+1)); }
      }
    } else { errorMsg.add("missing value for content"); }

    // invalid request
    if (errorMsg.size() != 0) {
      ObjectNode requestError = mapper.createObjectNode();
      requestError.put("status","400");
      requestError.put("errors", errorMsg.size());
      requestError.put("errorMessage", errorMsg);
      requestObject.setRequestError(requestError);
    }

    return requestObject;
  }
}
