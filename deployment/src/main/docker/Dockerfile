FROM ${cnn.core.base.image}
USER root
COPY config/ ${LANIT_HOME}/
RUN yum install -y dos2unix && dos2unix ${LANIT_HOME}/* && chmod +x ${LANIT_HOME}/start.sh
COPY cnn-core-${project.version}.jar ${LANIT_HOME}
COPY tools/*.service /etc/systemd/system/
RUN chown lanit -R /opt/lanit
RUN systemctl enable cnn_core.service
