FROM airhacks/glassfish:v5


RUN asadmin start-database && \
  asadmin start-domain && \
  asadmin create-jdbc-connection-pool --datasourceclassname org.apache.derby.jdbc.ClientDataSource --restype javax.sql.XADataSource --property portNumber=1527:password=APP:user=APP:serverName=localhost:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true Meet4Eat && \
  asadmin create-jdbc-resource --connectionpoolid Meet4Eat jdbc/Meet4Eat && \
  asadmin stop-domain && \
  asadmin stop-database


COPY ./target/m4e.war ${DEPLOYMENT_DIR}
ENTRYPOINT asadmin start-database && asadmin start-domain --verbose ${DOMAIN_NAME}
HEALTHCHECK CMD curl --fail http://localhost:8080/m4e/webresources/rest/appinfo || exit 1
