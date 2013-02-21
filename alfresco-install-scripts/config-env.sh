#!/bin/bash
# -------
# Script for setting the correct environment variables during installation
# -------

# this is the directory where the webapps/alfresco.war should be reside in
export ALFRESCO_TOMCAT_HOME_REPO="/opt/alfresco/4.1.1.3/tomcat-repo"
# this is the directory where the webapps/share.war should be reside in
export ALFRESCO_TOMCAT_HOME_SHARE="/opt/alfresco/4.1.1.3/tomcat-share"

# this is the directory where the pristine, unmodified, original and clean alfresco.war and share.war are located
export ALFRESCO_ORIGINALS_DIR="/opt/alfresco/4.1.1.3/original"
# this is the directory which is uses as a backup directory for the alfresco* and share* from the webapps path
export ALFRESCO_BACKUP_DIR="/opt/alfresco/4.1.1.3/backup"

# this is where the repo amp is copeid to
export ALFRESCO_REPO_AMPS="amps"
# this is where the share amp is copied to
export ALFRESCO_SHARE_AMPS="amps_share"

# this is the path to the amp module tool
export ALFRESCO_MMT="bin/alfresco-mmt.jar"

# this is the user who will own the alfresco and tomcat files
export ALFRESCO_TOMCAT_USER="alfresco"
# this is the group who will own the alfresco and tomcat files
export ALFRESCO_TOMCAT_GROUP="alfresco"

# this is the script used to start tomcat
export ALFRESCO_SHARE_START_SCRIPT="sudo /etc/init.d/alfresco-share start"
export ALFRESCO_SHARE_STOP_SCRIPT="sudo /etc/init.d/alfresco-share stop"
export ALFRESCO_REPO_START_SCRIPT="sudo /etc/init.d/alfresco-repo start"
export ALFRESCO_REPO_STOP_SCRIPT="sudo /etc/init.d/alfresco-repo stop"

