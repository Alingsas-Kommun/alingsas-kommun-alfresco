#!/bin/bash
# -------
# Script for downloading amp files
# -------

REPO_NAME="ak-repo-dm"
SHARE_NAME="ak-share-dm"

VERSION="1.0.0"

REPO_AMP="$REPO_NAME-$VERSION.amp"
SHARE_AMP="$SHARE_NAME-$VERSION.amp"

REPO_AMP_PATH=$ALFRESCO_REPO_AMPS
SHARE_AMP_PATH=$ALFRESCO_SHARE_AMPS

SERVER_URL="https://<alfresco-instance>/alfresco/service/cmis"
SERVER_PATH="Sites/alfresco/documentLibrary/Installation"

REPO_DOWNLOAD_URL="$SERVER_URL/p/$SERVER_PATH/$REPO_AMP/content?a=true"
SHARE_DOWNLOAD_URL="$SERVER_URL/p/$SERVER_PATH/$SHARE_AMP/content?a=true"

echo "Username:"
read USERNAME
echo "Password:"
read -s PASSWORD

getHTTPCode () {
    echo $(curl --write-out %{http_code} --silent --user $USERNAME:$PASSWORD $1 -o "$2")
}

response=$(getHTTPCode $REPO_DOWNLOAD_URL $ALFRESCO_REPO_AMPS/$REPO_AMP)

if [ $response -ne 200 ]; then
   echo "Error while downloading the Repo AMP, error code $response, from url $REPO_DOWNLOAD_URL"
   exit 1
fi

response=$(getHTTPCode $SHARE_DOWNLOAD_URL $ALFRESCO_SHARE_AMPS/$SHARE_AMP)

if [ $response -ne 200 ]; then
   echo "Error while downloading the Share AMP, error code $response, from url SHARE_DOWNLOAD_URL"
   exit 1
fi
