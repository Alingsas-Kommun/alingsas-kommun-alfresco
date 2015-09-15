package se.alingsas.alfresco.repo.jobs;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import se.alingsas.alfresco.repo.model.AkWfModel;

/**
 * Scheduled job which closes running workflow receipt tasks
 * @author mars
 *
 */
public class CloseRunningWorkflowReceipts extends ClusteredExecuter {

  private WorkflowService workflowService;
  private static final Log logger = LogFactory.getLog(CloseRunningWorkflowReceipts.class);

  @Override
  protected String getJobName() {
    return "Close Running Workflow Receipts";
  }

  @Override
  protected void executeInternal() {
    AuthenticationUtil.runAs(new RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        RetryingTransactionHelper transactionHelper = _transactionService.getRetryingTransactionHelper();
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
          @Override
          public Void execute() throws Throwable {
            logger.debug("End workflow task: " + AkWfModel.TYPE_AKWF_COMPLETE_DOCUMENT_REVIEW_RESULT_RECEIPT_TASK);
            int count = cancelAllReceiptTasks(AkWfModel.TYPE_AKWF_COMPLETE_DOCUMENT_REVIEW_RESULT_RECEIPT_TASK);

            logger.debug("End workflow task: " + AkWfModel.TYPE_AKWF_REVERT_DOCUMENT_REVIEW_RESULT_RECEIPT_TASK);
            count += cancelAllReceiptTasks(AkWfModel.TYPE_AKWF_REVERT_DOCUMENT_REVIEW_RESULT_RECEIPT_TASK);
            // build the result message
            logger.info("Closed " + count + " tasks");
            return null;
          }
        }, false, true);
        return null;
      }
    }, AuthenticationUtil.SYSTEM_USER_NAME);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    Assert.notNull(workflowService);
    logger.info("Initialized " + CloseRunningWorkflowReceipts.class.getName());
  }

  public void setWorkflowService(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public WorkflowService getWorkflowService() {
    return workflowService;
  }

  /**
   * Cancels ongoing workflows
   * 
   * @param taskQName
   */
  @SuppressWarnings("deprecation")
  private int cancelAllReceiptTasks(QName taskQName) {
    int counter = 0;
    WorkflowTaskQuery query = new WorkflowTaskQuery();
    query.setTaskName(taskQName);
    query.setActive(true);
    // query for invite workflow task associate
    List<WorkflowTask> tasks = workflowService.queryTasks(query);

    for (int i = 0; i < tasks.size(); i++) {
      WorkflowTask task = tasks.get(i);

      logger.debug("End task with id: " + task.getId());
      workflowService.endTask(task.getId(), null);
      counter++;
    }

    return counter;
  }
}
