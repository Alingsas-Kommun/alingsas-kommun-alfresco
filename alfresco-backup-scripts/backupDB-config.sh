#!/bin/bash
DATETIMESTAMP=$(date +"%Y%m%d%H%M%S")
BACKUP_TARGET_DIR=/opt/alfresco/alf_data/dbBackup
STORE_OLD_BACKUPS=3
DB_HOST=localhost
DB_PORT=5432
DB_USER=alfresco
DB_PASSWORD=alfresco
DB_NAME=alfresco

