# Trust-Anchor for Jenkins Build #

 1. The Proxy Build requires existing Trust-Anchor entry in the Auth-Server DB.
 2. The Test uses a JWT which contains an accountId.
 3. Using the Account Id, the Auth-Server fetches the account's Trust-Anchor. (can be only 1 TA per account).
 4. The Trust-Anchor must be Enabled, meaning the disabled-time must be null.
 5. The Trust-Anchor Account Id, must be identical to the accountId extracted from the JWT.
## Access the DB on Integration Lab ##
#### Database Properties: ####

1. Start port-forwarding to the remote DB (Amazon's RDS):
```
splatt --skip-version-check ssh_port_forward shared-postgres.cgbmmpkylyyv.eu-west-1.rds.amazonaws.com 5432 -l 46021
```
2. Then Connect using any DB Tool (e.g. pgAdmin) uisng LOCAL connection as follows:
```
Database Name .......: device_based_auth
Database Username ...: device_based_auth
Password ............: Kubernetes Secrets --> device_based_auth-service --> Data/psql.password --> Click on the "Eye" icon to view or copy the password.
Port ................: 46021
```

