# Alfresco - Alingsås Kommun

## Run
Run with `./run.sh build_start` (Mac) `./run.sh build_start` (Linux) or `./run.bat build_start` (Windows) and verify that it

Run script parameters:
 * `build_start`. Build the whole project, recreate the ACS and Share docker images, start the dockerised environment composed by ACS, Share, ASS and 
 PostgreSQL and tail the logs of all the containers.
 * `build_start_it_supported`. Build the whole project including dependencies required for IT execution, recreate the ACS and Share docker images, start the 
 dockerised environment composed by ACS, Share, ASS and PostgreSQL and tail the logs of all the containers.
 * `start`. Start the dockerised environment without building the project and tail the logs of all the containers.
 * `stop`. Stop the dockerised environment.
 * `purge`. Stop the dockerised container and delete all the persistent data (docker volumes).
 * `tail`. Tail the logs of all the containers.
 * `reload_share`. Build the Share module, recreate the Share docker image and restart the Share container.
 * `reload_acs`. Build the ACS module, recreate the ACS docker image and restart the ACS container.
 * `build_test`. Build the whole project, recreate the ACS and Share docker images, start the dockerised environment, execute the integration tests from the
 `integration-tests` module and stop the environment.
 * `test`. Execute the integration tests (the environment must be already started).

## After first run
* Install a license at http://localhost:8080/alfresco/s/enterprise/admin/admin-license
* Enable Share modules at http://localhost:8180/share/page/modules/deploy
* Create a template site with short name "templates"
  * In the template site, create a folder called "Dokument"
  * Upload template documents to this folder

## Main features implemented
* Custom site presets:
  * Public documents - Green alfresco theme
  * Secret documents - Red alfresco theme
  * Template site - Yellow alfresco theme
* Custom document types and templates
  * Site for managing document templates
  * Custom metadata when creating document
  * Select template when creating document
  * Document numbering
* Findwise integration (disabled, but still required due to datamodel)
* Workflows and actions for approval/publishing of documents
* Email links to document

# Developing

## Redeploy workflows
* Deploy workflows using the admin console: http://localhost:8080/alfresco/s/admin/admin-workflowconsole
  * ```deploy activiti alfresco/module/alfresco-alingsas-platform/workflow/ak-revert-document.bpmn```
  * ```deploy activiti alfresco/module/alfresco-alingsas-platform/workflow/ak-complete-document.bpmn```
 
