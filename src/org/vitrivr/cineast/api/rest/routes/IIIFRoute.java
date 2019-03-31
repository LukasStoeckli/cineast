package org.vitrivr.cineast.api.rest.routes;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.vitrivr.cineast.api.rest.exceptions.MethodNotSupportedException;
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
    System.out.println("body: " + request.body());

    response.type("application/json");
    switch(request.requestMethod()) {
      case "GET":
        return getHelp();
      case "POST":
        return handleRequest(request);
      default:
        throw new MethodNotSupportedException(request);
    }
  }



  private String handleRequest(Request request) {
    JsonObject json = JsonObject.readFrom(request.body());
    JsonObject response = new JsonObject();

    // validate & sanitize input
    int errors = 0;
    String errorMsg = null;
    String institution = "";
    String iiifEndpoint = "";
    JsonArray content = new JsonArray();

    // institution
    try {
      institution = json.get("institution").asString();
      if (institution.equals("")) { throw new NullPointerException(); }
    } catch (NullPointerException e) {
      errorMsg = "missing value for institution";
      errors += 1;
    }
    // iiifEndpoint
    try {
      iiifEndpoint = json.get("iiifEndpoint").asString();
      if (iiifEndpoint.equals("")) { throw new NullPointerException(); }
    } catch (NullPointerException e) {
      errorMsg = "missing value for iiifEndpoint";
      errors += 1;
    } // content
    try {
      content = json.get("content").asArray();
      if (content.size() == 0) { throw new UnsupportedOperationException(); }
      // also check if content elements are correct!!!
      for (int i = 0; i < content.size(); i++) {
        // TO DO
      }
    } catch (NullPointerException e) {
      errorMsg = "missing value for content";
      errors += 1;
    } catch (UnsupportedOperationException e) {
      errorMsg = "content must be nonempty array";
      errors += 1;
    }

    // invalid request
    if (errors != 0) {
      response.add("status","400");
      response.add("errorMsg", errorMsg);
      return response.toString();
    }



    // ---------------------------------
    // VALID NONEMPTY PARAMS
    //        |
    //        |
    //        V






    // process content
    // figure out how response looks like when not everything is checked yes
    // extraction in new thread, maybe tell result somehow


    System.out.println("contentSize: " + content.size());
    for (int i = 0; i < content.size(); i++) {
      System.out.println(i + "th elem: " + content.get(i));
    }










    response.add("status","200");
    response.add("institution", institution);
    response.add("numberOfResources", content.size());


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
    return help.toString();
  }
}
