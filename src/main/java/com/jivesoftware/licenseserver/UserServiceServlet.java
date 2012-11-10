package com.jivesoftware.licenseserver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet to manage user installing licence servlet
 */
public class UserServiceServlet extends HttpServlet {
  /**
   * Override the GET handler to process user install and uninstall Apps
   * @param req
   * @param resp
   */
  @Override
  protected void doGet(HttpServletRequest req,
      HttpServletResponse resp) throws ServletException, IOException {
    // check reqquest endpoint
    String contextPath = req.getPathInfo();

    if(contextPath.endsWith("/install")) {
      handleUserInstall(req, resp);
    } else if(contextPath.endsWith("/uninstall")) {
      handleUserUninstall(req, resp);
    }
  }

  /**
   * Handle user app installation
   * @param req
   * @param resp
   */
  private void handleUserInstall(HttpServletRequest req, HttpServletResponse resp) {
    // Get the owner info
    String ownerId = req.getParameter("opensocial_owner_id");

    // The value of the 'opensocial_owner_id' parameter will be formatted 'userID@instanceUUID'
    String[] ownerArray = ownerId.split("@");
    String userId = ownerArray[0];

    // Get App UUID
    String appUUID = req.getParameter("opensocial_app_id");

    // Get Jive ID
    String jiveInstanceId = ownerArray[1];

    // Store it to the List
    UserRequest userRequest = new UserRequest(Long.parseLong(userId), appUUID, jiveInstanceId);
    ActivityService.INCOMING_USER_REQUESTS.add(userRequest);
  }

  /**
   * TODO: LATER
   * @param req
   * @param resp
   */
  private void handleUserUninstall(HttpServletRequest req, HttpServletResponse resp) {

  }
}
