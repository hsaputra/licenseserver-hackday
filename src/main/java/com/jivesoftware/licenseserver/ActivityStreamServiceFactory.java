package com.jivesoftware.licenseserver;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jivesoftware.activitystreams.v1.services.ActivityStreamService;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import java.util.List;
import java.util.Map;

public class ActivityStreamServiceFactory {

  public ActivityStreamService build() {
    Map<String, String> headers = Maps.newHashMap();
    headers.put("Accept", "application/json");
    headers.put("Content-Type", "application/json");

    List<Interceptor> interceptorList = Lists.newLinkedList();
    interceptorList.add(new SignedFetchInterceptor());

    JAXRSClientFactoryBean sf = new JAXRSClientFactoryBean();
    sf.setResourceClass(ActivityStreamService.class);
    sf.setAddress("https://market-auto.apps.jiveland.com/gateway/api/activity/v1");
    sf.setHeaders(headers);
    sf.setProvider(new JacksonJaxbJsonProvider());
    sf.setOutInterceptors(interceptorList);

    BindingFactoryManager manager = sf.getBus().getExtension(BindingFactoryManager.class);
    JAXRSBindingFactory factory = new JAXRSBindingFactory();
    factory.setBus(sf.getBus());

    manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, factory);
    ActivityStreamService service = sf.create(ActivityStreamService.class);

    return service;
  }
}
