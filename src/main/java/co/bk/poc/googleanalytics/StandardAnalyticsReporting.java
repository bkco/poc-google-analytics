package co.bk.poc.googleanalytics;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analytics.model.Accounts;
import com.google.api.services.analytics.model.Profiles;
import com.google.api.services.analytics.model.Webproperties;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.api.services.analyticsreporting.v4.model.ColumnHeader;
import com.google.api.services.analyticsreporting.v4.model.DateRange;
import com.google.api.services.analyticsreporting.v4.model.DateRangeValues;
import com.google.api.services.analyticsreporting.v4.model.Dimension;
import com.google.api.services.analyticsreporting.v4.model.GetReportsRequest;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Metric;
import com.google.api.services.analyticsreporting.v4.model.MetricHeaderEntry;
import com.google.api.services.analyticsreporting.v4.model.Report;
import com.google.api.services.analyticsreporting.v4.model.ReportRequest;
import com.google.api.services.analyticsreporting.v4.model.ReportRow;
import static java.lang.String.format;


import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Proof of concept using standard analytics reporting API.
 * 1. Connect to and query analytics data.
 *
 */
public class StandardAnalyticsReporting {

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

  private static final String KEY_FILE_NAME = "key-service-account-permissions-from-google.json";

  private static final String VIEW_ID = "12341234"; // Analytics View ID

  public static void main(String[] args) {
    try {

      Analytics analytics = initializeAnalytics();
      String profile = getFirstProfileId(analytics);
      System.out.println("First Profile Id: "+ profile);

      AnalyticsReporting service = initializeAnalyticsReporting();
      GetReportsResponse response = queryAnalyticsReportingAPI(service, VIEW_ID, 7, "ga:transactions", "ga:transactionId");
      printResponse(response);
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

    InputStream is = StandardAnalyticsReporting.class.getClassLoader().getResourceAsStream(KEY_FILE_NAME);
    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    GoogleCredential credential = GoogleCredential
        .fromStream(is)
        .createScoped(AnalyticsReportingScopes.all());

    return new AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential).build();
  }

  /**
   * Create a ReportRequest and query the Analytics Reporting API V4.
   *
   * @param service An authorized Analytics Reporting API V4 service object.
   * @param viewId the id of the google analytics view being queried
   * @param dayCount number of days in the past the query should examine
   * @param metricExpression for example "ga:transactions" or "ga:sessions"
   * @param dimensionExpression for example "ga:transactionId"
   *
   * @return GetReportResponse The Analytics Reporting API V4 response.
   * @throws IOException
   */
  protected static GetReportsResponse queryAnalyticsReportingAPI(com.google.api.services.analyticsreporting.v4.AnalyticsReporting service, final String viewId,
          final Integer dayCount,
          final String metricExpression,
          final String dimensionExpression
  ) throws IOException {


    DateRange dateRange = new DateRange();
    dateRange.setStartDate(format("%sDaysAgo", dayCount));
    dateRange.setEndDate("today");

    Metric sessions = new Metric()
            .setExpression(metricExpression)
            .setAlias("transactions");

    Dimension dimension = new Dimension()
            .setName(dimensionExpression);
    List<Dimension> dimensions = new ArrayList<Dimension>();
    dimensions.add(dimension);

    ReportRequest request = new ReportRequest()
            .setViewId(viewId)
            .setDateRanges(Arrays.asList(dateRange))
            .setDimensions(dimensions)
            .setMetrics(Arrays.asList(sessions));

    ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
    requests.add(request);

    GetReportsRequest getReport = new GetReportsRequest()
            .setReportRequests(requests);

    GetReportsResponse response = service.reports().batchGet(getReport).execute();

    return response;
  }

  /**
   * Parses and prints the Analytics Reporting API V4 response.
   *
   * @param response An Analytics Reporting API V4 response.
   */
  private static void printResponse(GetReportsResponse response) {

    for (Report report: response.getReports()) {
      ColumnHeader header = report.getColumnHeader();
      List<String> dimensionHeaders = header.getDimensions();
      List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
      List<ReportRow> rows = report.getData().getRows();

      if (rows == null) {
         System.out.println("No data found for " + VIEW_ID);
         return;
      }

      for (ReportRow row: rows) {
        List<String> dimensions = row.getDimensions();
        List<DateRangeValues> metrics = row.getMetrics();
        for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
          System.out.println(dimensionHeaders.get(i) + ": " + dimensions.get(i));
        }

        for (int j = 0; j < metrics.size(); j++) {
          System.out.print("Date Range (" + j + "): ");
          DateRangeValues values = metrics.get(j);
          for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
            System.out.println(metricHeaders.get(k).getName() + ": " + values.getValues().get(k));
          }
        }
      }
    }
  }

  private static Analytics initializeAnalytics() throws Exception {

    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    InputStream is = StandardAnalyticsReporting.class.getClassLoader().getResourceAsStream(KEY_FILE_NAME);
    GoogleCredential credential = GoogleCredential
            .fromStream(is)
            .createScoped(AnalyticsScopes.all());
    return new Analytics.Builder(httpTransport, JSON_FACTORY, credential).build();
  }

  private static String getFirstProfileId(Analytics analytics) throws IOException {
    // Get the first view (profile) ID for the authorized user.
    String profileId = null;

    // Query for the list of all accounts associated with the service account.
    Accounts accounts = analytics.management().accounts().list().execute();

    if (accounts.getItems().isEmpty()) {
      System.err.println("No accounts found");
    } else {
      String firstAccountId = accounts.getItems().get(0).getId();

      // Query for the list of properties associated with the first account.
      Webproperties properties = analytics.management().webproperties()
              .list(firstAccountId).execute();

      if (properties.getItems().isEmpty()) {
        System.err.println("No Webproperties found");
      } else {
        String firstWebpropertyId = properties.getItems().get(0).getId();

        // Query for the list views (profiles) associated with the property.
        Profiles profiles = analytics.management().profiles()
                .list(firstAccountId, firstWebpropertyId).execute();

        if (profiles.getItems().isEmpty()) {
          System.err.println("No views (profiles) found");
        } else {
          // Return the first (view) profile associated with the property.
          profileId = profiles.getItems().get(0).getId();
        }
      }
    }
    return profileId;
  }
}
