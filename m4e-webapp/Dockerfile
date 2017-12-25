FROM airhacks/glassfish:v5


RUN asadmin start-database && \
  asadmin start-domain && \
  asadmin create-jdbc-connection-pool --datasourceclassname org.apache.derby.jdbc.ClientDataSource --restype javax.sql.XADataSource --property portNumber=1527:password=APP:user=APP:serverName=localhost:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true Meet4Eat && \
  asadmin create-jdbc-resource --connectionpoolid Meet4Eat jdbc/Meet4Eat && \
  asadmin set-log-levels net.m4e=FINEST && \
  asadmin stop-domain && \
  asadmin stop-database


COPY ./target/m4e.war ${DEPLOYMENT_DIR}
RUN ln -s ${GLASSFISH_HOME}/domains/domain1/logs /var/logs

ENTRYPOINT asadmin start-database && asadmin start-domain --verbose ${DOMAIN_NAME}
HEALTHCHECK CMD curl --fail http://localhost:8080/m4e/webresources/rest/appinfo || exit 1
