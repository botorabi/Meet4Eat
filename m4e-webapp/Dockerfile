FROM airhacks/glassfish:v5

COPY ./target/m4e.war ${DEPLOYMENT_DIR}

ENTRYPOINT asadmin start-database && asadmin start-domain --verbose ${DOMAIN_NAME}
