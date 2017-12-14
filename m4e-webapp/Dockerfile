FROM airhacks/glassfish:v5

COPY ./target/m4e.war ${DEPLOYMENT_DIR}
COPY ./init_gf.sh ./
ENTRYPOINT sh ./init_gf.sh
