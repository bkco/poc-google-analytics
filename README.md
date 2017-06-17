# poc-google-analytics

This repo demonstrates google analytics API usage for the standard analytics API and the beta Realtime API.

The realtime API was used to verify working analytics as part of a Continuous Integration build.

A "service account" is recommended as a way of configuring client credentials:
* find service account [setup details here](https://developers.google.com/analytics/devguides/reporting/core/v4/quickstart/service-java).
* and [further details here](https://stackoverflow.com/questions/9932090/google-analytics-api-v3-authorization-to-allow-access-to-my-data).

Activating the standard "analytics" API is required the first time you use the API. Activate it by logging into the [developer console](https://console.developers.google.com/apis/api/analytics.googleapis.com).

Activating the "realtime" analytics API can be achieved by simply submitting a request to google. Please see here for [further details](https://developers.google.com/analytics/devguides/reporting/realtime/v3/).

