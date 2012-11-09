package com.jivesoftware.licenseserver;

public class UserRequest {
  public String appUUID;
  public String jiveInstanceId;
  public long userId;

  public UserRequest(long userId, String appUUID, String jiveInstanceId) {
    this.userId = userId;
    this.appUUID = appUUID;
    this.jiveInstanceId = jiveInstanceId;
  }
}
