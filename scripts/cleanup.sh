#!/bin/bash

echo "Removing database..."
mysql -uroot -p -e "drop database tfam_dev; create database tfam_dev;"

echo "Removing filesystem data..."
rm -rf /opt/tfam/files/*
rm -rf /opt/tfam/lucene/*


