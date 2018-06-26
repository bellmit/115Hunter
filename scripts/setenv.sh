#!/bin/sh
#
export http_proxy=http://proxy.pal.sap.corp:8080
export https_proxy=http://proxy.pal.sap.corp:8080
export no_proxy=localhost,127.0.0.1,10.*
unset JAVA_HOME

