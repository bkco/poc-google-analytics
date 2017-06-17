package co.bk.poc.googleanalytics;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analytics.model.RealtimeData;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;


import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Proof of concept that Google Realtime Analytics API:
 * 1. detected "events" where each event had a label was a proprietary booking ID.
 * 2. as developers we could query for the "eventLabel" and retrieve the bookings made in the last few minutes.
 * 3. with (1) and (2) leverage API to test analytics as part of CI build.
 *
 * @author bkco
 */
public class RealtimeAnalytics {

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

  private static final String KEY_FILE_NAME = "key-service-account-permissions-from-google.json";

  private static final String VIEW_ID = "12341234";

  public static void main(String[] args) {
    try {

      Analytics analyticsClient = initializeAnalyticsClient();

      String metrics = "rt:totalEvents";
      String dimensions = "rt:eventLabel,rt:minutesAgo";
      RealtimeData realtimeData = queryAnalyticsReportingAPI(analyticsClient, VIEW_ID, metrics, dimensions);

      parseRealtimeData(realtimeData);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Initializes an Analytics Reporting API V4 service object.
   *
   * @return An authorized Analytics Reporting API V4 service object.
   * @throws IOException
   * @throws GeneralSecurityException
   */
  private static AnalyticsReporting initializeAnalyticsReporting() throws GeneralSecurityException, IOException {

    InputStream is = RealtimeAnalytics.class.getClassLoader().getResourceAsStream(KEY_FILE_NAME);
    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    GoogleCredential credential = GoogleCredential
        .fromStream(is)
        .createScoped(AnalyticsReportingScopes.all());

    return new AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential).build();
  }

  /**
   * Realtime API query
   *
   * @param analyticsClient An authorized Analytics Reporting API V4 service object.
   * @param viewId the id of the google analytics view being queried
   * @param metricExpression for example "ga:transactions" or "ga:sessions"
   * @param dimensionExpression for example "ga:transactionId"
   *
   * @return GetReportResponse The Analytics Reporting API V4 response.
   * @throws IOException
   */
  protected static RealtimeData queryAnalyticsReportingAPI(Analytics analyticsClient, final String viewId,
          final String metricExpression, final String dimensionExpression)
      throws IOException {

    RealtimeData realtimeData = null;

    try {

      Analytics.Data.Realtime.Get realtimeRequest = analyticsClient.data().realtime()
            .get("ga:" + viewId, metricExpression)
            .setDimensions(dimensionExpression);

      realtimeData = realtimeRequest.execute();

    } catch (IOException e) {
      e.printStackTrace();
    }
    return realtimeData;
  }

  /**
   * Parse the response.
   *
   * @param realtimeData data from the realtime API
   */
  private static void parseRealtimeData(RealtimeData realtimeData) {

    List<List<String>> rows = realtimeData.getRows();

    if (rows != null && rows.size() > 0) {
      for (List<String> row: rows) {
        System.out.println("bookingId: " + row.get(0));
      }
    }
  }

  private static Analytics initializeAnalyticsClient() throws Exception {

    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    InputStream is = RealtimeAnalytics.class.getClassLoader().getResourceAsStream(KEY_FILE_NAME);
    GoogleCredential credential = GoogleCredential
            .fromStream(is)
            .createScoped(AnalyticsScopes.all());
    return new Analytics.Builder(httpTransport, JSON_FACTORY, credential).build();
  }
}
