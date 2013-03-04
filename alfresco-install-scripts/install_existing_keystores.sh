#! /bin/sh
# Please edit the variables below to suit your installation
# Note: for an installation created by the Alfresco installer, you only need to edit ALFRESCO_HOME

# Alfresco installation directory
ALFRESCO_HOME=/opt/alfresco
# The directory containing the alfresco keystores, as referenced by keystoreFile and truststoreFile attributes in tomcat/conf/server.xml
ALFRESCO_KEYSTORE_HOME=$ALFRESCO_HOME/alf_data/keystore
# SOLR installation directory
SOLR_HOME=$ALFRESCO_HOME/solr
# Java installation directory
JAVA_HOME=/usr/lib/jvm/jdk
# Location in which new keystore files will be generated
CERTIFICATE_HOME=$ALFRESCO_HOME/alf_data/alfresco-generated-certificates

echo "Back up old files"
# Back up old files
cp "$ALFRESCO_KEYSTORE_HOME/ssl.keystore" "$ALFRESCO_KEYSTORE_HOME/ssl.keystore.old"
cp "$ALFRESCO_KEYSTORE_HOME/ssl.truststore" "$ALFRESCO_KEYSTORE_HOME/ssl.truststore.old"
cp "$ALFRESCO_KEYSTORE_HOME/browser.p12" "$ALFRESCO_KEYSTORE_HOME/browser.p12.old"
cp "$SOLR_HOME/workspace-SpacesStore/conf/ssl.repo.client.keystore" "$SOLR_HOME/workspace-SpacesStore/conf/ssl.repo.client.keystore.old"
cp "$SOLR_HOME/workspace-SpacesStore/conf/ssl.repo.client.truststore" "$SOLR_HOME/workspace-SpacesStore/conf/ssl.repo.client.truststore.old"
cp "$SOLR_HOME/archive-SpacesStore/conf/ssl.repo.client.keystore" "$SOLR_HOME/archive-SpacesStore/conf/ssl.repo.client.keystore.old"
cp "$SOLR_HOME/archive-SpacesStore/conf/ssl.repo.client.truststore" "$SOLR_HOME/archive-SpacesStore/conf/ssl.repo.client.truststore.old"
cp "$SOLR_HOME/templates/test/conf/ssl.repo.client.keystore" "$SOLR_HOME/templates/test/conf/ssl.repo.client.keystore.old"
cp "$SOLR_HOME/templates/test/conf/ssl.repo.client.truststore" "$SOLR_HOME/templates/test/conf/ssl.repo.client.truststore.old"
cp "$SOLR_HOME/templates/store/conf/ssl.repo.client.keystore" "$SOLR_HOME/templates/store/conf/ssl.repo.client.keystore.old"
cp "$SOLR_HOME/templates/store/conf/ssl.repo.client.truststore" "$SOLR_HOME/templates/store/conf/ssl.repo.client.truststore.old"

# Backup property files
cp "$SOLR_HOME/workspace-SpacesStore/conf/ssl-keystore-passwords.properties" "$SOLR_HOME/workspace-SpacesStore/conf/ssl-keystore-passwords.properties.old"
cp "$SOLR_HOME/workspace-SpacesStore/conf/ssl-truststore-passwords.properties" "$SOLR_HOME/workspace-SpacesStore/conf/ssl-truststore-passwords.properties.old"
cp "$SOLR_HOME/archive-SpacesStore/conf/ssl-keystore-passwords.properties" "$SOLR_HOME/archive-SpacesStore/conf/ssl-keystore-passwords.properties.old"
cp "$SOLR_HOME/archive-SpacesStore/conf/ssl-truststore-passwords.properties" "$SOLR_HOME/archive-SpacesStore/conf/ssl-truststore-passwords.properties.old"
cp "$SOLR_HOME/templates/test/conf/ssl-keystore-passwords.properties" "$SOLR_HOME/templates/test/conf/ssl-keystore-passwords.properties.old"
cp "$SOLR_HOME/templates/test/conf/ssl-truststore-passwords.properties" "$SOLR_HOME/templates/test/conf/ssl-truststore-passwords.properties.old"
cp "$SOLR_HOME/templates/store/conf/ssl-keystore-passwords.properties" "$SOLR_HOME/templates/store/ssl-keystore-passwords.properties.old"
cp "$SOLR_HOME/templates/store/conf/ssl-truststore-passwords.properties" "$SOLR_HOME/templates/store/ssl-truststore-passwords.properties.old"

echo "Install new files"
# Install the new files
cp "$CERTIFICATE_HOME/ssl.keystore" "$ALFRESCO_KEYSTORE_HOME/ssl.keystore"
cp "$CERTIFICATE_HOME/ssl.truststore" "$ALFRESCO_KEYSTORE_HOME/ssl.truststore"
cp "$CERTIFICATE_HOME/browser.p12" "$ALFRESCO_KEYSTORE_HOME/browser.p12"
cp "$CERTIFICATE_HOME/ssl.repo.client.keystore" "$SOLR_HOME/workspace-SpacesStore/conf/ssl.repo.client.keystore"
cp "$CERTIFICATE_HOME/ssl.repo.client.truststore" "$SOLR_HOME/workspace-SpacesStore/conf/ssl.repo.client.truststore"
cp "$CERTIFICATE_HOME/ssl.repo.client.keystore" "$SOLR_HOME/archive-SpacesStore/conf/ssl.repo.client.keystore"
cp "$CERTIFICATE_HOME/ssl.repo.client.truststore" "$SOLR_HOME/archive-SpacesStore/conf/ssl.repo.client.truststore"
cp "$CERTIFICATE_HOME/ssl.repo.client.keystore" "$SOLR_HOME/templates/test/conf/ssl.repo.client.keystore"
cp "$CERTIFICATE_HOME/ssl.repo.client.truststore" "$SOLR_HOME/templates/test/conf/ssl.repo.client.truststore"
cp "$CERTIFICATE_HOME/ssl.repo.client.keystore" "$SOLR_HOME/templates/store/conf/ssl.repo.client.keystore"
cp "$CERTIFICATE_HOME/ssl.repo.client.truststore" "$SOLR_HOME/templates/store/conf/ssl.repo.client.truststore"
# Copy property files
cp "$ALFRESCO_KEYSTORE_HOME/ssl-keystore-passwords.properties" "$SOLR_HOME/workspace-SpacesStore/conf/ssl-keystore-passwords.properties"
cp "$ALFRESCO_KEYSTORE_HOME/ssl-truststore-passwords.properties" "$SOLR_HOME/workspace-SpacesStore/conf/ssl-truststore-passwords.properties"
cp "$ALFRESCO_KEYSTORE_HOME/ssl-keystore-passwords.properties" "$SOLR_HOME/archive-SpacesStore/conf/ssl-keystore-passwords.properties"
cp "$ALFRESCO_KEYSTORE_HOME/ssl-truststore-passwords.properties" "$SOLR_HOME/archive-SpacesStore/conf/ssl-truststore-passwords.properties"
cp "$ALFRESCO_KEYSTORE_HOME/ssl-keystore-passwords.properties" "$SOLR_HOME/templates/test/conf/ssl-keystore-passwords.properties"
cp "$ALFRESCO_KEYSTORE_HOME/ssl-truststore-passwords.properties" "$SOLR_HOME/templates/test/conf/ssl-truststore-passwords.properties"
cp "$ALFRESCO_KEYSTORE_HOME/ssl-keystore-passwords.properties" "$SOLR_HOME/templates/store/ssl-keystore-passwords.properties"
cp "$ALFRESCO_KEYSTORE_HOME/ssl-truststore-passwords.properties" "$SOLR_HOME/templates/store/ssl-truststore-passwords.properties"

echo "Certificate update complete"
echo "Please ensure that you set dir.keystore=$ALFRESCO_KEYSTORE_HOME in alfresco-global.properties and that tomcat/conf/server.xml contains the correct path and password for SSL"

