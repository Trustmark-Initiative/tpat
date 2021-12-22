#!/bin/bash

echo "Removing database..."
mysql -uroot -p -e "drop database tfam_dev; create database tfam_dev;"

echo "Removing filesystem data..."
rm -rf /opt/tpat/files/*
rm -rf /opt/tpat/lucene/*


