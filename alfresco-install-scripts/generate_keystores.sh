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
# The repository server certificate subject name, as specified in tomcat/conf/tomcat-users.xml with roles="repository"
REPO_CERT_DNAME="CN=Alfresco Repository, OU=Unknown, O=Alfresco Software Ltd., L=Maidenhead, ST=UK, C=GB"
# The SOLR client certificate subject name, as specified in tomcat/conf/tomcat-users.xml with roles="repoclient"
SOLR_CLIENT_CERT_DNAME="CN=Alfresco Repository Client, OU=Unknown, O=Alfresco Software Ltd., L=Maidenhead, ST=UK, C=GB"
# The number of days before the certificate expires
CERTIFICATE_VALIDITY=36525
STORE_PASS="alfresco"
DEST_STORE_PASS="alfresco"


# Stop alfresco
#"$ALFRESCO_HOME/alfresco.sh" stop

# Ensure certificate output dir exists
mkdir -p "$CERTIFICATE_HOME"

# Remove old output files (note they are backed up elsewhere)
if [ -f "$CERTIFICATE_HOME/ssl.keystore" ]; then rm "$CERTIFICATE_HOME/ssl.keystore"; fi
if [ -f "$CERTIFICATE_HOME/ssl.truststore" ]; then rm "$CERTIFICATE_HOME/ssl.truststore"; fi
if [ -f "$CERTIFICATE_HOME/browser.p12" ]; then rm "$CERTIFICATE_HOME/browser.p12"; fi
if [ -f "$CERTIFICATE_HOME/ssl.repo.client.keystore" ]; then rm "$CERTIFICATE_HOME/ssl.repo.client.keystore"; fi
if [ -f "$CERTIFICATE_HOME/ssl.repo.client.truststore" ]; then rm "$CERTIFICATE_HOME/ssl.repo.client.truststore"; fi

echo "Generate new self-signed certificates"
# Generate new self-signed certificates for the repository and solr
"$JAVA_HOME/bin/keytool" -genkeypair -keyalg RSA -dname "$REPO_CERT_DNAME" -validity $CERTIFICATE_VALIDITY -alias ssl.repo -keypass $STORE_PASS -keystore "$CERTIFICATE_HOME/ssl.keystore" -storetype JCEKS -storepass $STORE_PASS
"$JAVA_HOME/bin/keytool" -exportcert -alias ssl.repo -file "$CERTIFICATE_HOME/ssl.repo.crt" -keystore "$CERTIFICATE_HOME/ssl.keystore" -storetype JCEKS -storepass $STORE_PASS
"$JAVA_HOME/bin/keytool" -genkeypair -keyalg RSA -dname "$SOLR_CLIENT_CERT_DNAME" -validity $CERTIFICATE_VALIDITY -alias ssl.repo.client -keypass $STORE_PASS -keystore "$CERTIFICATE_HOME/ssl.repo.client.keystore" -storetype JCEKS -storepass $STORE_PASS
"$JAVA_HOME/bin/keytool" -exportcert -alias ssl.repo.client -file "$CERTIFICATE_HOME/ssl.repo.client.crt" -keystore "$CERTIFICATE_HOME/ssl.repo.client.keystore" -storetype JCEKS -storepass $STORE_PASS

echo "Create trust relationships"
# Create trust relationship between repository and solr
"$JAVA_HOME/bin/keytool" -importcert -noprompt -alias ssl.repo.client -file "$CERTIFICATE_HOME/ssl.repo.client.crt" -keystore "$CERTIFICATE_HOME/ssl.truststore" -storetype JCEKS -storepass $STORE_PASS
# Create trust relationship between repository and itself - used for searches
"$JAVA_HOME/bin/keytool" -importcert -noprompt -alias ssl.repo -file "$CERTIFICATE_HOME/ssl.repo.crt" -keystore "$CERTIFICATE_HOME/ssl.truststore" -storetype JCEKS -storepass $STORE_PASS
# Create trust relationship between solr and repository
"$JAVA_HOME/bin/keytool" -importcert -noprompt -alias ssl.repo -file "$CERTIFICATE_HOME/ssl.repo.crt" -keystore "$CERTIFICATE_HOME/ssl.repo.client.truststore" -storetype JCEKS -storepass $STORE_PASS
# Export repository keystore to pkcs12 format for browser compatibility
"$JAVA_HOME/bin/keytool" -importkeystore -srckeystore "$CERTIFICATE_HOME/ssl.keystore" -srcstorepass $STORE_PASS -srcstoretype JCEKS -srcalias ssl.repo -srckeypass $STORE_PASS -destkeystore "$CERTIFICATE_HOME/browser.p12" -deststoretype pkcs12 -deststorepass $DEST_STORE_PASS -destalias ssl.repo -destkeypass $DEST_STORE_PASS


