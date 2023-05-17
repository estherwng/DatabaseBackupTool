# DatabaseBackupTool
This Java project is a Database backup tool that uses JDBC, thread concurrency, and File I/O. This project ask for a user inputted sql file path and takes the file to back up into a .txt file, which is currently defaulted to be called backup.txt and is able to execute the sql file in a database.

Please note:

This project uses MySQL. Please provide the correct JDBC_URL, USERNAME, PASSWORD credential. The database you provide in the JDBC_URL will be where the results of the sql queries are stored.

If you don't change the BACKUP_FILE after a backup, it will replace the content inside.
