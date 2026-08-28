package io.temporal.activity;

import io.temporal.testing.internal.SDKTestOptions;
import io.temporal.testing.internal.SDKTestWorkflowRule;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class ActivityIdOptionTest {

  @Rule
  public SDKTestWorkflowRule testWorkflowRule =
      SDKTestWorkflowRule.newBuilder()
          .setWorkflowTypes(ActivityIdWorkflowImpl.class)
          .setActivityImplementations(new ActivityIdActivityImpl())
          .build();

  @Test
  public void activityIdFromOptionsReachesTheActivity() {
    ActivityIdWorkflow workflow = testWorkflowRule.newWorkflowStub(ActivityIdWorkflow.class);
    Assert.assertEquals("order-42", workflow.execute("order-42"));
  }

  @Test
  public void withoutActivityIdTheIdIsStillAssignedForTheCaller() {
    ActivityIdWorkflow workflow = testWorkflowRule.newWorkflowStub(ActivityIdWorkflow.class);
    String activityId = workflow.execute(null);
    Assert.assertNotNull(activityId);
    Assert.assertFalse(activityId.isEmpty());
    Assert.assertNotEquals("order-42", activityId);
  }

  @WorkflowInterface
  public interface ActivityIdWorkflow {
    @WorkflowMethod
    String execute(String activityId);
  }

  public static class ActivityIdWorkflowImpl implements ActivityIdWorkflow {
    @Override
    public String execute(String activityId) {
      ActivityOptions.Builder options =
          ActivityOptions.newBuilder(SDKTestOptions.newActivityOptions());
      if (activityId != null) {
        options.setActivityId(activityId);
      }
      return Workflow.newActivityStub(ActivityIdActivity.class, options.build()).reportActivityId();
    }
  }

  @ActivityInterface
  public interface ActivityIdActivity {
    String reportActivityId();
  }

  public static class ActivityIdActivityImpl implements ActivityIdActivity {
    @Override
    public String reportActivityId() {
      return Activity.getExecutionContext().getInfo().getActivityId();
    }
  }
}
