#!/usr/bin/env bash

cd amazon-kinesis-agent
./setup --install && cp /agent.json /etc/aws-kinesis/agent.json
