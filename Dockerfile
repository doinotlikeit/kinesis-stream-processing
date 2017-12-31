FROM centos

RUN yum -y install less
RUN yum -y install –y https://s3.amazonaws.com/streaming-data-agent/aws-kinesis-agent-latest.amzn1.noarch.rpm
RUN chkconfig aws-kinesis-agent on
COPY agent.json /etc/aws-kinesis/agent.json

CMD [ "sh", "-c", "start-aws-kinesis-agent" ]