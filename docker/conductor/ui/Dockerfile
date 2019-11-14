FROM alpine AS builder

ARG conductor_version=v2.18.0

RUN apk add --no-cache --update \
    build-base \
    git \
    perl \
    openssl \
    ca-certificates

# Clone the project directly onto the image
RUN git clone https://github.com/Netflix/conductor.git \
    && cd /conductor \
    && git checkout ${conductor_version}

FROM node:9-alpine

# Install the required packages for the node build
# to run on alpine
RUN apk update && apk add \
  tzdata \
  autoconf \
  automake \
  libtool \
  build-base \
  libstdc++ \
  gcc \
  abuild \
  binutils \
  nasm \
  libpng \
  libpng-dev \
  libjpeg-turbo \
  libjpeg-turbo-dev \
  python \
  git

# Make app folders
RUN mkdir -p /app/ui

# Copy the ui files onto the image
COPY --from=builder /conductor/docker/ui/bin /app
COPY --from=builder /conductor/ui /app/ui

# Copy the files for the server into the app folders
RUN chmod +x /app/startup.sh

# Get and install conductor UI
RUN cd /app/ui \
  && npm install \
  && npm run build --server

EXPOSE 5000

CMD [ "/app/startup.sh" ]
ENTRYPOINT ["/bin/sh"]
