package se.alingsas.alfresco.repo.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CloseRunningWorkflowReceiptsJob implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    ClusteredExecuter expireInvitationReceipts = (ClusteredExecuter) context.getJobDetail().getJobDataMap().get("closeRunningWorkflowReceipts");

    expireInvitationReceipts.execute();
  }

}
