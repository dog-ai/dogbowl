package ai.dog.bowl.client.spark.rest;

import org.apache.http.client.methods.HttpGet;

public class JobStatusRequestSpecificationImpl implements JobStatusRequestSpecification, CanValidateSubmissionId {

    private SparkRestClient sparkRestClient;

    public JobStatusRequestSpecificationImpl(SparkRestClient sparkRestClient) {
        this.sparkRestClient = sparkRestClient;
    }

    /**
     * Gets the status of an existing Driver Application
     * @param submissionId Id of submitted job to request status for.
     * @return State of the application
     * @throws FailedSparkRequestException Request to Spark server failed,
     * or the Spark Server could not retrieve the status of the requested app.
     */
    @Override
    public DriverState withSubmissionId(String submissionId) throws FailedSparkRequestException  {
        assertSubmissionId(submissionId);
        final String url = "http://" + sparkRestClient.getMasterUrl() + "/v1/submissions/status/" + submissionId;
        final JobStatusResponse response = HttpRequestUtil.executeHttpMethodAndGetResponse(sparkRestClient.getClient(), new HttpGet(url),JobStatusResponse.class);
        if (!response.getSuccess()) {
            throw new FailedSparkRequestException("submit was not successful.");
        }
        return response.getDriverState();
    }
}
