#!/bin/sh

CURRENT_DIR=$(pwd)
TARGET="root@138.197.34.190"

ssh $TARGET mysqldump -h 127.0.0.1 -u root -pmysql mifostenant-default > /tmp/mifostenant-default.sql
#ssh $TARGET mysqldump -h 127.0.0.1 -u root -pmysql mifosplatform-tenants > /tmp/mifosplatform-tenants.sql
scp $TARGET:/tmp/mifostenant-default.sql ~/Dropbox/Public/
#scp $TARGET:/tmp/mifosplatform-tenants.sql ~/Dropbox/Public/
