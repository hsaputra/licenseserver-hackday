package com.jivesoftware.licenseserver;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import net.oauth.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import org.apache.cxf.jaxrs.impl.*;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.shindig.auth.OAuthConstants;
import org.apache.shindig.auth.OAuthUtil;
import org.apache.shindig.common.uri.UriBuilder;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class SignedFetchInterceptor extends AbstractPhaseInterceptor<Message> {
  enum GatewayOAuthHeader {
    ORIG_HTTP_METHOD("X-Orig-Http-method"),
    ORIGINAL_URI("X-Jive-Orig-URI"),
    INSTANCE_ID("X-Jive-Instance-Id"),
    APP_OAUTH_SERVICE("X-OAuth-Service"),
    APP_ID("X-Jive-App-Id"),
    OAUTH_TOKEN_SECRET("X-OAuth-Token-Secret"),
    OAUTH2_AZN_STATE("X-OAuth2-State"),
    OAUTH_RECEIVED_CALLBACK("X-OAUTH-RECEIVED_CALLBACK"),
    FORWARD_OAUTH("X-Forwarded-OAuth"),
    FORWARDED_FOR("X-Forwarded-For"),
    SIG_RQST_URL("X-Jive-Sig-URL");

    private String value;

    GatewayOAuthHeader(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  private static final Logger LOG = Logger.getLogger(SignedFetchInterceptor.class.toString());

  @Inject
  public SignedFetchInterceptor() {
    super(Phase.SETUP);
  }

  public String getSignedData(String url, String body, Map<String, String> context) throws OAuthException {
    return getSignedData(url, body, null, context);
  }

  public String getSignedData(String url, String body, String opensocialParams,
                              Map<String, String> context) throws OAuthException {
    List<OAuth.Parameter> params = Lists.newArrayList();
    return getSignedData(body == null ? "GET" : "POST", url, body, params, opensocialParams,
        ParameterStyle.QUERY_STRING, context);
  }

  public String getSignedData(String method, String url, String body, Collection<OAuth.Parameter> params,
                              String opensocialParams, Map<String, String> context) throws OAuthException {
    return getSignedData(method, url, body, params, opensocialParams, ParameterStyle.QUERY_STRING, context);
  }

  /**
   * @param url              URL that should should participate in the signing process
   * @param body             body of POST message
   * @param opensocialParams @See com.jivesoftware.community.opensocial.util.ParametersProvider
   * @param style            what kind of return value is needed
   *
   * @return Returns the signed parameters concatenated and ready for use in the style specified by style
   *
   * @throws OAuthException
   */
  public String getSignedData(String method, String url, String body, Collection<OAuth.Parameter> params, String opensocialParams,
                              ParameterStyle style, Map<String, String> context) throws OAuthException {
    if (params == null) params = Lists.newArrayList();
    OAuthMessage message = sign(method, url, body, params, opensocialParams, context);

    if (message == null) return null;
    String retVal = null;
    try {
      switch (style) {
        case QUERY_STRING:
          // params includes query params, so remove query params in the url
          if (url.indexOf('?') >= 0) url = url.substring(0, url.indexOf('?'));
          retVal = OAuth.addParameters(url, message.getParameters());
          break;
        case BODY:
          if (body == null) body = "";
          retVal = OAuth.addParameters("", message.getParameters());
          retVal = body + retVal.substring(1);
          break;
        case AUTHORIZATION_HEADER:
        default:
          retVal = message.getAuthorizationHeader(null);
          break;
      }
    } catch (IOException e) {
      throw new OAuthException(e);
    }

    return retVal;
  }

  protected OAuthMessage sign(String method, String uri, String postBody,
      Collection<OAuth.Parameter> params, String opensocialParams, Map<String, String> context) throws OAuthException {
    if (uri == null) return null;

    UriBuilder target = UriBuilder.parse(uri);
    String query = target.getQuery();
    target.setQuery(null);
    params.addAll(OAuth.decodeForm(query));
    if (opensocialParams != null) params.addAll(OAuth.decodeForm(opensocialParams));

    OAuthUtil.SignatureType signatureType = OAuthUtil.SignatureType.URL_ONLY;
    if (postBody != null) signatureType = OAuthUtil.SignatureType.URL_AND_FORM_PARAMS;

    switch (signatureType) {
      case URL_ONLY:
        LOG.info("url_only");
        break;
      case URL_AND_FORM_PARAMS:
        try {
          params.addAll(OAuth.decodeForm(postBody));
        } catch (IllegalArgumentException e) {
          // Occurs if OAuth.decodeForm finds an invalid URL to decode.
          throw new OAuthException("Could not decode body", e);
        }
        break;
      case URL_AND_BODY_HASH:
        try {
          byte[] body = IOUtils.toByteArray(postBody);
          byte[] hash = DigestUtils.sha(body);
          String b64 = new String(Base64.encodeBase64(hash), Charsets.UTF_8.name());
          params.add(new OAuth.Parameter(OAuthConstants.OAUTH_BODY_HASH, b64));
        } catch (IOException e) {
          throw new OAuthException("Error taking body hash", e);
        }
        break;
      default:
        LOG.info("default");
        break;
    }

    // authParams are parameters prefixed with 'xoauth' 'oauth' or 'opensocial',
    // trusted parameters have ability to override these parameters.
    List<OAuth.Parameter> authParams = Lists.newArrayList();
    params.addAll(authParams);

    OAuthMessage message;
    try {
      String consumerKey = context.get("applicationConsumerKey");
      String consumerSecret = context.get("applicationConsumerSecret");

      if (consumerKey == null || consumerSecret == null) {
        throw new Exception("Consumer key/secret not available, this Jive installation hasn't been properly registered with apps market");
      }
      OAuthAccessor accessor = new OAuthAccessor(new OAuthConsumer(null, consumerKey, consumerSecret, null));

      int index = uri.indexOf("?");
      if (index != -1) {
        uri = uri.substring(0, index);
      }
      message = accessor.newRequestMessage(method == null ? "GET" : method, uri, params);
    } catch (Exception e) {
      throw new OAuthException(e);
    }

    return message;
  }

  public void handleMessage(final Message message) throws Fault {
    MetadataMap map = (MetadataMap) message.get("jaxrs.template.parameters");
    String jiveInstanceId = (String) map.get("jiveInstanceId").get(0);
    String appUUID = (String) map.get("appUUID").get(0);
    String userId = (String) map.get("userId").get(0);

    Collection<OAuth.Parameter> params = null;
    if (OAuth.isFormEncoded((String) message.get(Message.CONTENT_TYPE))) {
      params = new ArrayList<OAuth.Parameter>();
      boolean useFormParams = true;
      List<Object> contentsList = MessageContentsList.getContentsList(message);
      if (contentsList != null && contentsList.size() == 1) // OAuth doesn't support multi-part content signing
      {
        for (Object contentListEntry : contentsList) {
          if (contentListEntry instanceof Map) {
            for (Object contentListItem : ((Map) contentListEntry).keySet()) {
              if (contentListItem instanceof Map) {
                for (Object key : ((Map) contentListItem).keySet()) {
                  String value = (String) ((Map) contentListItem).get(key);
                  params.add(new OAuth.Parameter((String) key, value));
                }
              } else {
                LOG.severe("Unexpected content type: " + contentListItem.getClass().getName()
                    + " while processing message: " + message);
                useFormParams = false;
                break;
              }
            }
          } else {
            LOG.severe("Unexpected content type: " + contentListEntry.getClass().getName()
                + " while processing message: " + message);
            useFormParams = false;
            break;
          }
        }

        if (!useFormParams) params = null;
      }
    }

    try {
      String method = (String) message.get(Message.HTTP_REQUEST_METHOD);
      String requestQueryString = (String) message.get(Message.QUERY_STRING);
      if (requestQueryString == null) requestQueryString = "";
      String requestUrl = message.get(Message.REQUEST_URI) + (requestQueryString.length() > 0 ? ('?' + requestQueryString) : "");
      Map<String, String> context = new HashMap<String, String>();

      context.put("applicationConsumerKey", "");
      context.put("applicationConsumerSecret", "");

      String signedUri = getSignedData(method, requestUrl, null, params, null, context);
      if (signedUri == null) throw new OAuthException("Problem with signing request " + requestUrl);

      int delimiterPos = signedUri.indexOf('?');
      if (delimiterPos < 0) throw new OAuthException("Failed signing request " + requestUrl);

      String newQueryString = signedUri.substring(delimiterPos + 1);
      message.put(Message.QUERY_STRING, newQueryString);

      int delimiterPos1 = requestUrl.indexOf('?');
      if (delimiterPos1 > -1) {
        message.put(Message.REQUEST_URI, requestUrl.substring(0, delimiterPos1));
        message.put("org.apache.cxf.message.Message.ENDPOINT_ADDRESS", requestUrl.substring(0, delimiterPos1));
      }

      Object headers = message.get(Message.PROTOCOL_HEADERS);
      if (headers instanceof MultivaluedMap) {
        //The app
        ((MultivaluedMap<String, String>) headers).putSingle(GatewayOAuthHeader.APP_ID.toString(), appUUID);
        ((MultivaluedMap<String, String>) headers).putSingle(GatewayOAuthHeader.SIG_RQST_URL.toString(), requestUrl);
      } else {
        LOG.severe(
            Message.PROTOCOL_HEADERS + " is not a MultivaluedMap: " + headers.getClass().getCanonicalName()
                + " while processing message: " + message);
      }
    } catch (OAuthException e) {
      throw new Fault(e);
    }
  }
}

