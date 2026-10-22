#!/bin/bash
# b) Write a shell script to validate the following inputs
# I. Email ID
# II. URL

read -p "Enter an Email ID: " email
read -p "Enter a URL: " url

if [[ $email =~ ^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$ ]]; then
    echo "Valid Email ID"
else
    echo "Invalid Email ID"
fi

if [[ $url =~ ^(https)://[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}(/.*)?$ ]]; then
    echo "Valid URL"
else
    echo "Invalid URL"
fi