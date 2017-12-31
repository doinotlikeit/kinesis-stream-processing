#FROM ubuntu:16.04
#FROM amazonlinux
FROM centos

RUN yum -y install less
RUN yum -y install –y https://s3.amazonaws.com/streaming-data-agent/aws-kinesis-agent-latest.amzn1.noarch.rpm
RUN chkconfig aws-kinesis-agent on
COPY agent.json /etc/aws-kinesis/agent.json

CMD [ "sh", "-c", "start-aws-kinesis-agent" ]


#RUN apt-get update
#RUN apt-get install -y curl
#RUN apt-get install -y git
#
#COPY installAgent.sh /
#RUN find / -name "*.sh"  -exec chmod 777 {} \; -print
#COPY agent.json /
#
### JAVA
#ARG JAVA_ARCHIVE=http://download.oracle.com/otn-pub/java/jdk/8u152-b16/aa0333dd3019491ca4f6ddbe78cdb6d0/jdk-8u152-linux-x64.tar.gz
#ENV JAVA_HOME /usr/local/jdk1.8.0_152
#ENV PATH $PATH:$JAVA_HOME/bin
#RUN curl -sL --retry 3 --insecure \
#  --header "Cookie: oraclelicense=accept-securebackup-cookie;" $JAVA_ARCHIVE \
#  | tar -xz -C /usr/local/ && ln -s $JAVA_HOME /usr/local/java
#
#
#RUN git clone https://github.com/awslabs/amazon-kinesis-agent.git
#
#CMD [ "sh", "-c", "service aws-kinesis-agent start && tail -f /dev/null" ]

