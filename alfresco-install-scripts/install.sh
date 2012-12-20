#!/bin/bash
# -------
# Script for installation
# -------

PWD=$(pwd)

# call the config-env.sh script to set the correct environment variables
echo "Setting correct environment variables . . ."
source "$PWD/config-env.sh"
echo ""

echo "Stopping Tomcat . . ."
$ALFRESCO_TOMCAT_STOP_SCRIPT
echo ""

echo "Move the alfresco and share directories and wars to the backup directory . . ."
DIRNAME=$(date +%Y%m%d%H%M)
mkdir -p $ALFRESCO_BACKUP_DIR/$DIRNAME
mv $ALFRESCO_TOMCAT_HOME_REPO/webapps/alfresco $ALFRESCO_BACKUP_DIR/$DIRNAME
mv $ALFRESCO_TOMCAT_HOME_REPO/webapps/alfresco.war $ALFRESCO_BACKUP_DIR/$DIRNAME
mv $ALFRESCO_TOMCAT_HOME_SHARE/webapps/share $ALFRESCO_BACKUP_DIR/$DIRNAME
mv $ALFRESCO_TOMCAT_HOME_SHARE/webapps/share.war $ALFRESCO_BACKUP_DIR/$DIRNAME
echo ""

echo "Copying fresh alfresco.war and share.war to Tomcat . . ."
cp $ALFRESCO_ORIGINALS_DIR/alfresco.war $ALFRESCO_TOMCAT_HOME_REPO/webapps
cp $ALFRESCO_ORIGINALS_DIR/share.war $ALFRESCO_TOMCAT_HOME_SHARE/webapps
echo ""

read -p "Do you want to download the AMP files? (y/n)? "
[ "$REPLY" != "y" ] || . download-amps.sh $1 $2
echo ""

echo "Applying amps to Alfresco . . ."
java -jar $ALFRESCO_MMT install $ALFRESCO_REPO_AMPS $ALFRESCO_TOMCAT_HOME_REPO/webapps/alfresco.war -directory -force -nobackup
java -jar $ALFRESCO_MMT install $ALFRESCO_SHARE_AMPS $ALFRESCO_TOMCAT_HOME_SHARE/webapps/share.war -directory -force -nobackup
java -jar $ALFRESCO_MMT list $ALFRESCO_TOMCAT_HOME_REPO/webapps/alfresco.war
java -jar $ALFRESCO_MMT list $ALFRESCO_TOMCAT_HOME_SHARE/webapps/share.war
echo ""

echo "Cleaning temporary Alfresco files from Tomcat . . ."
rm -rf $ALFRESCO_TOMCAT_HOME_REPO/work/Catalina/localhost/alfresco
rm -rf $ALFRESCO_TOMCAT_HOME_SHARE/work/Catalina/localhost/share
echo ""

echo "Setting ownership to the tomcat directories to the tomcat user . . ."
chown -R $ALFRESCO_TOMCAT_USER:$ALFRESCO_TOMCAT_GROUP $ALFRESCO_TOMCAT_HOME_REPO
chown -R $ALFRESCO_TOMCAT_USER:$ALFRESCO_TOMCAT_GROUP $ALFRESCO_TOMCAT_HOME_SHARE
echo ""

echo "Starting Tomcat . . ."
$ALFRESCO_TOMCAT_START_SCRIPT
echo ""

echo "Tailing log, waiting for startup of Alfresco . . ."
echo ""
tail -f $ALFRESCO_TOMCAT_HOME_REPO/logs/catalina.out
