#!/bin/bash
DIR=`dirname $BASH_SOURCE`
. $DIR/backupDB-config.sh
if [[ ! -e $BACKUP_TARGET_DIR ]]; then
        echo "Creating backup directory $BACKUP_TARGET_DIR"
        mkdir $BACKUP_TARGET_DIR
fi

#Remove oldest backup
BACKUP_TARGET_SUBDIR="backup-$STORE_OLD_BACKUPS"
if [[ -e $BACKUP_TARGET_DIR/$BACKUP_TARGET_SUBDIR ]]; then
        echo "Purging old backup directory $BACKUP_TARGET_DIR/$BACKUP_TARGET_SUBDIR"
        rm -rf $BACKUP_TARGET_DIR/$BACKUP_TARGET_SUBDIR
fi

#Rotate old backups
for (( i=$STORE_OLD_BACKUPS; i>0; i-- ))
do
        SOURCE_DIR="backup-$i"
        c=$(($i + 1))
        DEST_DIR="backup-$c"
        if [[ -e $BACKUP_TARGET_DIR/$SOURCE_DIR ]]; then
                echo "Renaming old backup $BACKUP_TARGET_DIR/$SOURCE_DIR to $BACKUP_TARGET_DIR/$DEST_DIR"
                mv $BACKUP_TARGET_DIR/$SOURCE_DIR $BACKUP_TARGET_DIR/$DEST_DIR
        fi
done

#Create backup directory
BACKUP_TARGET_SUBDIR=$BACKUP_TARGET_DIR/backup-1
mkdir $BACKUP_TARGET_SUBDIR

echo "Starting database backup"
export PGPASSWORD="$DB_PASSWORD"
OUTPUT_FILENAME1="$DB_NAME-$DATETIMESTAMP.bin"
OUTPUT_FILENAME2="$DB_NAME-$DATETIMESTAMP.sql"
OUTPUT_FILENAME3="globals-$DATETIMESTAMP.sql"
pg_dump -Fc --host=$DB_HOST --port=$DB_PORT --username=$DB_USER $DB_NAME > $BACKUP_TARGET_SUBDIR/$OUTPUT_FILENAME1
#Normal SQL dump disabled, using custom output format
#pg_dump --host=$DB_HOST --port=$DB_PORT --username=$DB_USER $DB_NAME > $BACKUP_TARGET_SUBDIR/$OUTPUT_FILENAME2
#gzip $BACKUP_TARGET_SUBDIR/$OUTPUT_FILENAME2
pg_dumpall -g --host=$DB_HOST --port=$DB_PORT --username=$DB_USER > $BACKUP_TARGET_SUBDIR/$OUTPUT_FILENAME3
gzip $BACKUP_TARGET_SUBDIR/$OUTPUT_FILENAME3
export PGPASSWORD=""
touch $BACKUP_TARGET_SUBDIR/$OUTPUT_FILENAME
echo "Database backup completed"

