package com.jivesoftware.licenseserver;

import com.jivesoftware.activitystreams.v1.rest.*;

import java.util.Arrays;

public class MessageAdapter {
  /**
   * Converts a LicenseServerMessage object to an ActivityRepresentation
   * @param message The Market Message
   *
   * @return ActivityRepresentation
   */
  public ActivityRepresentation convert(final LicenseServerMessage message, long userId) {
    final ActivityRepresentation activityRepresentation = new ActivityRepresentation();
    activityRepresentation.setID(message.getId());
    activityRepresentation.setTitle(message.getTitle());
    activityRepresentation.setPostedTime(message.getPostedTime());
    activityRepresentation.setBody(message.getBody());

    ObjectRepresentation actor = new ObjectRepresentation();
    // HARDCODE USER ID
    actor.setID("uri:userid:" + userId);
    actor.setTitle("Admin" + " " + "Admin");
    actor.setSummary("Admin" + " " + "Admin");
    activityRepresentation.setActor(actor);

    //This should be the actual invoice information
    ObjectRepresentation object = new ObjectRepresentation();
    object.setID(message.getId());
    object.setTitle(message.getTitle());
    object.setSummary(message.getBody());
    activityRepresentation.setObject(object);

    activityRepresentation.setVerb(Arrays.asList(message.getVerb()));

    return activityRepresentation;
  }
}
