#!/bin/bash
# -------
# Script for downloading amp files
# -------

REPO_NAME="ak-repo-dm"
SHARE_NAME="ak-share-dm"
VTI_NAME="alfresco-vti"

VERSION="1.1.0.1"
VTI_VERSION="4.1.1.3"

REPO_AMP="$REPO_NAME-$VERSION.amp"
SHARE_AMP="$SHARE_NAME-$VERSION.amp"
VTI_AMP="$VTI_NAME-$VTI_VERSION-patched.amp"

REPO_AMP_PATH=$ALFRESCO_REPO_AMPS
SHARE_AMP_PATH=$ALFRESCO_SHARE_AMPS

SERVER_URL="https://<alfresco-instance>/alfresco/service/cmis"
SERVER_PATH="Sites/alfresco/documentLibrary/Installation"

REPO_DOWNLOAD_URL="$SERVER_URL/p/$SERVER_PATH/$REPO_AMP/content?a=true"
SHARE_DOWNLOAD_URL="$SERVER_URL/p/$SERVER_PATH/$SHARE_AMP/content?a=true"
REPO_DOWNLOAD_URL="$SERVER_URL/p/$SERVER_PATH/$REPO_AMP/content?a=true"
SHARE_DOWNLOAD_URL="$SERVER_URL/p/$SERVER_PATH/$SHARE_AMP/content?a=true"
VTI_DOWNLOAD_URL="$SERVER_URL/p/$SERVER_PATH/$VTI_AMP/content?a=true"
echo "Username:"
read USERNAME
echo "Password:"
read -s PASSWORD

getHTTPCode () {
    echo $(curl --write-out %{http_code} --silent --user $USERNAME:$PASSWORD $1 -o "$2")
}

#Comment out the following lines to disable download of the Repository customizations module
response=$(getHTTPCode $REPO_DOWNLOAD_URL $ALFRESCO_REPO_AMPS/$REPO_AMP)

if [ $response -ne 200 ]; then
   echo "Error while downloading the Repo AMP, error code $response, from url $REPO_DOWNLOAD_URL"
   exit 1
fi

#Comment out the following lines to disable download of the Share customizations module
response=$(getHTTPCode $SHARE_DOWNLOAD_URL $ALFRESCO_SHARE_AMPS/$SHARE_AMP)

if [ $response -ne 200 ]; then
   echo "Error while downloading the Share AMP, error code $response, from url SHARE_DOWNLOAD_URL"
   exit 1
fi

#Comment out the following lines to disable download of VTI module
response=$(getHTTPCode $VTI_DOWNLOAD_URL $ALFRESCO_REPO_AMPS/$VTI_AMP)

if [ $response -ne 200 ]; then
   echo "Error while downloading the VTI AMP, error code $response, from url $VTI_DOWNLOAD_URL"
   exit 1
fi

