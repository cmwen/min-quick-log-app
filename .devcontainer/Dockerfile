FROM mcr.microsoft.com/devcontainers/base:ubuntu-24.04

ARG USERNAME=vscode
ARG CMDLINE_TOOLS_REV=11076708

ENV DEBIAN_FRONTEND=noninteractive
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=${ANDROID_HOME}
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        lib32stdc++6 \
        libc6-i386 \
        libglu1-mesa \
        libpulse0 \
        openjdk-17-jdk \
        unzip \
        wget && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    wget -q "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_REV}_latest.zip" -O /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d /tmp/cmdline-tools && \
    mkdir -p ${ANDROID_HOME}/cmdline-tools/latest && \
    mv /tmp/cmdline-tools/cmdline-tools/* ${ANDROID_HOME}/cmdline-tools/latest/ && \
    rm -rf /tmp/cmdline-tools.zip /tmp/cmdline-tools && \
    chown -R ${USERNAME}:${USERNAME} ${ANDROID_HOME}

RUN runuser -u ${USERNAME} -- bash -lc "mkdir -p \$HOME/.android && touch \$HOME/.android/repositories.cfg"

RUN runuser -u ${USERNAME} -- bash -lc "yes | sdkmanager --sdk_root=${ANDROID_HOME} --licenses" && \
    runuser -u ${USERNAME} -- bash -lc "sdkmanager --sdk_root=${ANDROID_HOME} --install 'platform-tools' 'platforms;android-34' 'build-tools;34.0.0'"
