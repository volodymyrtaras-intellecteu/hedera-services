########################################################################################################################
#
# Define Global Build Arguments
#
########################################################################################################################
ARG IMAGE_PREFIX="gcr.io/swirlds-registry/"
ARG IMAGE_TAG="latest"

FROM ${IMAGE_PREFIX}network-node-base:${IMAGE_TAG}

# Define Environment Variables
ENV JAVA_HEAP_MIN=""
ENV JAVA_HEAP_MAX=""
ENV JAVA_OPTS=""

# Add SDK components
ADD sdk/data/apps/* /opt/hgcapp/services-hedera/HapiApp2.0/data/apps/
ADD sdk/data/lib/* /opt/hgcapp/services-hedera/HapiApp2.0/data/lib/

# Add Diagnostic Utilities
RUN apt-get install -y net-tools

# Add the entrypoint script
ADD entrypoint.sh /opt/hgcapp/services-hedera/HapiApp2.0/

# Ensure JRS specific folder & files exist
RUN mkdir -p "/opt/hgcapp/services-hedera/HapiApp2.0/data/lifecycle" && \
    mkdir -p "/opt/hgcapp/services-hedera/HapiApp2.0/data/jdb" && \
    mkdir -p "/opt/hgcapp/services-hedera/HapiApp2.0/logs" && \
    touch "/opt/hgcapp/services-hedera/HapiApp2.0/gc.log"

# Ensure proper file permissions
RUN chmod -R +x /opt/hgcapp/services-hedera/HapiApp2.0/entrypoint.sh && \
    chown -R 2000:2000 /opt/hgcapp/services-hedera/HapiApp2.0

# Set Final Working Directory, User, and Entrypoint
USER 2000
WORKDIR "/opt/hgcapp"
ENTRYPOINT ["/opt/hgcapp/services-hedera/HapiApp2.0/entrypoint.sh"]
