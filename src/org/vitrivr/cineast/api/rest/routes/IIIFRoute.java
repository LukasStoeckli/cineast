package org.vitrivr.cineast.api.rest.routes;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.vitrivr.cineast.api.rest.iiif.IIIFProcessor;
import org.vitrivr.cineast.api.rest.iiif.IIIFRequest;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class IIIFRoute implements Route {
  private final String APISPECIFICATION = "./resources/iiif/specification.html";
  private final String APISTATUS = "./resources/iiif/status.html";

  private static final Logger LOGGER = LogManager.getLogger();
  private ObjectMapper mapper = new ObjectMapper();
  private String mode;

  public IIIFRoute(String _mode) {
    mode = _mode;
  }


  @Override
  public Object handle(Request request, Response response) throws Exception {
    String requestLog = "new " + mode + " request (protocol: " + request.protocol() + "ip: " + request.ip();
    requestLog += " ua: " + request.userAgent() + " method: " + request.requestMethod() + ")";
    LOGGER.info(requestLog);

    switch (mode) {
      case "extraction":
        response.type("application/json");
        return handleExtraction(request);
      case "specification":
        response.type("text/html");
        return handleSpecification();
      case "status":
        return handleStatus();
      default:
        throw new UnsupportedOperationException();
    }
  }


  private String handleExtraction(Request request) {
    JsonNode json;
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
      int processID = IIIFProcessor.enqueue(requestObject);
      response.put("status","200");
      response.put("institution", requestObject.getInstitution());
      response.put("numberOfResources", requestObject.getContent().size());
      response.put("processID", processID);
    }

    return response.toString();
  }


  private String handleSpecification() {
    return readHTML(APISPECIFICATION, "specification");
  }

  private String handleStatus() {
    String content, status = readHTML(APISTATUS, "status");
    IIIFRequest[] processes = IIIFProcessor.getProcessQueue();

    if (processes == null) {
      content = "<p>currently no extraction in progress</p>";
    } else {
      content = "<table>";
      content += "<tr>";
      content += "<th style=\"width: 20%\">procID</th>";
      content += "<th style=\"width: 20%\">whatever</th>";
      content += "<th style=\"width: 60%\">something else</th>";
      content += "</tr>";

      for (IIIFRequest proc: processes) {
        content += "<tr>";
        content += "<td>42</td>";
        content += "<td>idk</td>";
        content += "<td>lorem ipsum dolor sit amet</td>";
        content += "</tr>";
      }

      content += "</table>";
    }

    return status.replace("[[CONTENT]]", content);
  }


  private String readHTML(String file, String label) {
    try {
      StringBuilder result = new StringBuilder();
      FileReader fileReader = new FileReader(file);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      String current;
      while ((current = bufferedReader.readLine()) != null) {
        result.append(current + "\n");
      }
      return result.toString();
    } catch (IOException e) {
      LOGGER.error("unable to open file: " + file);
      return "<h1>IIIF Endpoint</h1><p>500: could not find " + label + "</p>";
    }
  }



  private IIIFRequest validate(JsonNode json) {
    String institution;
    JsonNode content;
    ArrayNode errorMsg = mapper.createArrayNode();
    IIIFRequest requestObject = new IIIFRequest();

    // institution
    if (json.has("institution")) {
      institution = json.get("institution").asText();
      if (institution.equals("")) { errorMsg.add("empty value for institution"); }
      requestObject.setInstitution(institution);
    } else { errorMsg.add("missing value for institution"); }

    // content
    if (json.has("content")) {
      content = json.get("content");
      if (content.size() == 0) { errorMsg.add("content must be nonempty array"); }
      for (int i = 0; i < content.size(); i++) {
        if (content.get(i).has("scheme") && !content.get(i).get("scheme").asText().equals("")) {
          if (content.get(i).has("server") && !content.get(i).get("server").asText().equals("")) {
            if (content.get(i).has("prefix") && !content.get(i).get("prefix").asText().equals("")) {
              if (content.get(i).has("identifier") && !content.get(i).get("identifier").asText().equals("")) {
                String scheme = content.get(i).get("scheme").asText();
                String server = content.get(i).get("server").asText();
                String prefix = content.get(i).get("prefix").asText();
                String identifier = content.get(i).get("identifier").asText();
                requestObject.addContent(scheme, server, prefix, identifier);
              } else { errorMsg.add("missing value for identifier in element " + (i+1)); }
            } else { errorMsg.add("missing value for prefix in element " + (i+1)); }
          } else { errorMsg.add("missing value for server in element " + (i+1)); }
        } else { errorMsg.add("missing value for scheme in element " + (i+1)); }
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
