package com.jivesoftware.licenseserver;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Test the ActivityService class to publish to production Gateway
 */
public class ActivityServiceTest {

  public static void main(String[] args) {
    System.out.println("HELLOW");

    ActivityService svc = new ActivityService();
    svc.setActivityStreamFactory(new ActivityStreamFactory());
    svc.setMessageAdapter(new MessageAdapter());

    LicenseServerMessage msg = new LicenseServerMessage();
    msg.setBody("Test Activity Body.");
    msg.setPostedTime(new Date());
    msg.setTitle("Test Message");
    msg.setId("uri:com:jivesoftware:license:123");

    svc.publish("94258a3b-c146-4780-bcb4-90e945302946", 4259,
        "95399606-be84-4ec5-84a7-8276663503c0", msg);

  }
}
