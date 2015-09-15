#!/bin/bash
# -------
# Script for upgradring Solr
# -------

# this is the home of the alfresco installation files and Solr installation files
ALFRESCO_HOME=.
# this is the solr home
SOLR_HOME=$ALFRESCO_HOME/solr4
# dirname created out of todays date and time
DIRNAME=$(date +%Y%m%d%H%M)
# the backup dir to use in the upgrade
BACKUPDIR=$ALFRESCO_HOME/backup/solr-$DIRNAME

if [[ ! ( -d $1 ) ]]; then
    echo "Alfresco Enterprise Solr ZIP file passed as argument does not exist..."
    exit 1
fi

# stop solr
./alfresco-solr stop

# move the current solr home to the backup location
mv $SOLR_HOME $BACKUPDIR

# unzip the passed solr zip file and overwrite all files if the exist
#unzip -q -o $1 -d $SOLR_HOME
cp -rf $1 $SOLR_HOME

# diff the solrcore with the old and new
echo ""
diff $BACKUPDIR/workspace-SpacesStore/conf/solrcore.properties $SOLR_HOME/workspace-SpacesStore/conf/solrcore.properties

echo ""
read -p "Note the difference and edit the '$SOLR_HOME/workspace-SpacesStore/conf/solrcore.properties' file, press any key to continue..."
echo ""

# lanuch nano to actually edit the file and fix the diffs
nano -w $SOLR_HOME/workspace-SpacesStore/conf/solrcore.properties

# diff the solrcore with the old and new
echo ""
diff $BACKUPDIR/archive-SpacesStore/conf/solrcore.properties $SOLR_HOME/archive-SpacesStore/conf/solrcore.properties

echo ""
read -p "Note the difference and later edit the '$SOLR_HOME/archive-SpacesStore/conf/solrcore.properties' file, press any key to continue..."
echo ""

# lanuch nano to actually edit the file and fix the diffs
nano -w $SOLR_HOME/archive-SpacesStore/conf/solrcore.properties

# copy all the cached alfresco models from the backup to the new solr home
cp -R $BACKUPDIR/workspace-SpacesStore/alfrescoModels $SOLR_HOME/workspace-SpacesStore/alfrescoModels
# copy all the cached alfresco models from the backup to the new solr home
cp -R $BACKUPDIR/archive-SpacesStore/alfrescoModels $SOLR_HOME/archive-SpacesStore/alfrescoModels
cd keystore
./install_existing_keystores.sh
cd ..

# change ownership of the solr home to be owned by the tomcat6 user
chown -R mars $SOLR_HOME

# start alfresco-solr again
echo ""
./alfresco-solr start
echo ""

echo ""
echo "Tailing the alfresco-solr log..."
echo ""

tail -f ./solr.log
