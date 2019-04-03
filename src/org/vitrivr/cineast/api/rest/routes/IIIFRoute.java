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
    String institution = "";
    String iiifEndpoint = "";
    JsonNode content;

    int errors = 0;
    ArrayNode errorMsg = mapper.createArrayNode();

    IIIFRequest requestObject = new IIIFRequest();

    // institution
    //if (json.has("isnstitution"))
    try {
      institution = json.get("institution").asText();
      if (institution.equals("")) {
        errorMsg.add("empty value for institution");
        errors += 1;
      }
      requestObject.setInstitution(institution);
    } catch (NullPointerException e) {
      errorMsg.add("missing value for institution");
      errors += 1;
    }

    // iiifEndpoint
    try {
      iiifEndpoint = json.get("iiifEndpoint").asText();
      if (iiifEndpoint.equals("")) {
        errorMsg.add("empty value for iiifEndpoint");
        errors += 1;
      }
      requestObject.setEndpoint(iiifEndpoint);
    } catch (NullPointerException e) {
      errorMsg.add("missing value for iiifEndpoint");
      errors += 1;
    }

    // content
    try {
      content = json.get("content");
      if (content.size() == 0) {
        errorMsg.add("content must be nonempty array");
        errors += 1;
      }
      for (int i = 0; i < content.size(); i++) {
        System.out.println(i + "th elem: " + content.get(i));

        String project;
        String image;
        String meta;






        //requestObject.addContent(project, image, meta);
      }
    } catch (NullPointerException e) {
      errorMsg.add("missing value for content");
      errors += 1;
    }





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
