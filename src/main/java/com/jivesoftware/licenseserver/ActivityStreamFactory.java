package com.jivesoftware.licenseserver;

import com.jivesoftware.activitystreams.v1.rest.*;

public class ActivityStreamFactory {
  public ActivityStreamRepresentation create() {
    final ActivityStreamRepresentation activityStream = new ActivityStreamRepresentation();

    activityStream.setId("http://nexus.jivesoftware.com/communication");
    activityStream.setLanguage("en-US");
    activityStream.setTitle("Jive Nexus License Server Communications");

    return activityStream;
  }
}
