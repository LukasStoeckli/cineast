package org.vitrivr.cineast.api.rest.routes;



import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.vitrivr.cineast.api.rest.exceptions.MethodNotSupportedException;
import org.vitrivr.cineast.api.rest.iiif.IIIFRequest;
import spark.Request;
import spark.Response;
import spark.Route;


public class IIIFRoute implements Route {

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
    JsonObject json = JsonObject.readFrom(request.body());
    IIIFRequest requestObject = validate(json);
    JsonObject response = new JsonObject();
    if (requestObject.getRequestError() != null) {
      response = requestObject.getRequestError();
    } else {
      // start process here

      response.add("status","200");
      response.add("institution", requestObject.getInstitution());
      response.add("numberOfResources", requestObject.getContent().size());
    }






    return response.toString();
  }



  private String getHelp() {
    JsonObject help = new JsonObject();
    help.add("title","IIIF content registration");
    help.add("description","cineast api to register content for retrieval.");
    help.add("endpoint", "/iiif/register");
    help.add("method","POST");
    help.add("type", "application/json");
    // add definition of fields
    // add example request
    // add example responses
    return "<h1>IIIF Endpoint</h1><p>Add API specification here</p>";//help.toString();
  }




  public IIIFRequest validate(JsonObject json) {
    String institution = "";
    String iiifEndpoint = "";
    JsonArray content = new JsonArray();

    int errors = 0;
    JsonArray errorMsg = new JsonArray();

    IIIFRequest requestObject = new IIIFRequest();

    // institution
    try {
      institution = json.get("institution").asString();
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
      iiifEndpoint = json.get("iiifEndpoint").asString();
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
      content = json.get("content").asArray();
      if (content.size() == 0) {
        errorMsg.add("content must be nonempty array");
        errors += 1;
      }
      for (int i = 0; i < content.size(); i++) {
        System.out.println(i + "th elem: " + content.get(i));

        String project;
        String image;
        String meta;

        /*
        try {

        } catch () {
          errorMsg.add("");
          errors += 1;
          continue;
        }
        */


        //requestObject.addContent(project, image, meta);
      }
    } catch (NullPointerException e) {
      errorMsg.add("missing value for content");
      errors += 1;
    }





    // invalid request
    if (errors != 0) {
      JsonObject requestError = new JsonObject();
      requestError.add("status","400");
      requestError.add("errors", errors);
      requestError.add("errorMessage", errorMsg);
      requestObject.setRequestError(requestError);
    }





    return requestObject;
  }


}
