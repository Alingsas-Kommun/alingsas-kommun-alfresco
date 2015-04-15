package se.alingsas.alfresco.repo.admin.patch;

import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import se.alingsas.alfresco.repo.model.AkWfModel;

public class CloseRunningWorkflowreceiptsPatch extends AbstractPatch {

  private WorkflowService workflowService;
  private static final String MSG_SUCCESS = "patch.cancelRunningWorkflowReceipts.result";

  private static final Log logger = LogFactory.getLog(CloseRunningWorkflowreceiptsPatch.class);

  @Override
  protected String applyInternal() throws Exception {
    //namespaceService.registerNamespace(AkWfModel.AKWF_URI, AkWfModel.AKWF_SHORT);
    logger.debug("Cancel workflows: " + AkWfModel.TYPE_AKWF_COMPLETE_DOCUMENT_REVIEW_RESULT_RECEIPT_TASK);
    int count = cancelAllReceiptTasks(AkWfModel.TYPE_AKWF_COMPLETE_DOCUMENT_REVIEW_RESULT_RECEIPT_TASK);

    logger.debug("Cancel workflows: " + AkWfModel.TYPE_AKWF_REVERT_DOCUMENT_REVIEW_RESULT_RECEIPT_TASK);
    count += cancelAllReceiptTasks(AkWfModel.TYPE_AKWF_REVERT_DOCUMENT_REVIEW_RESULT_RECEIPT_TASK);
    // build the result message
    String msg = I18NUtil.getMessage(MSG_SUCCESS, count);
    return msg;
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
  private int cancelAllReceiptTasks(QName taskQName) {
    int counter = 0;
    WorkflowTaskQuery query = new WorkflowTaskQuery();
    query.setTaskName(taskQName);
    query.setActive(true);
    // query for invite workflow task associate
    List<WorkflowTask> tasks = workflowService.queryTasks(query);

    // should also be 0 or 1
    if (tasks.size() == 1) {
      // task not found - can't do anything
    } else {
      for (int i = 0; i < tasks.size(); i++) {
        WorkflowTask task = tasks.get(i);
        String workflowId = task.getPath().getInstance().getId();
        logger.debug("Cancel workflow with id: " + workflowId);
        
        workflowService.cancelWorkflow(workflowId);
        counter++;
      }
    }
    return counter;
  }
}
