package com.jivesoftware.licenseserver;

import com.jivesoftware.activitystreams.v1.rest.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LicenseServerMessage {
  private Map<String, String> actor;
  private String id;
  private String title;
  private String body;
  private String permalinkUrl;
  private Date postedTime;

  private HashMap context = new HashMap();

  //A list of verbs, describing the intent of the message.
  private String[] verb = {"post"};

  //The object being acted upon. This value is required.
  //
  //In the case of invoices, this will represent the invoice.
  private ObjectRepresentation object;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getPermalinkUrl() {
    return permalinkUrl;
  }

  public void setPermalinkUrl(String permalinkUrl) {
    this.permalinkUrl = permalinkUrl;
  }

  public Date getPostedTime() {
    return postedTime;
  }

  public void setPostedTime(Date postedTime) {
    this.postedTime = postedTime;
  }

  public String[] getVerb() {
    return verb;
  }

  public void setVerb(String[] verb) {
    this.verb = verb;
  }

  public ObjectRepresentation getObject() {
    return object;
  }

  public void setObject(ObjectRepresentation object) {
    this.object = object;
  }

  public HashMap getContext() {
    return context;
  }

  public Map<String, String> getActor() {
    return actor;
  }

  public void setActor(Map<String, String> actor) {
    this.actor = actor;
  }
}
