#!/usr/bin/env bash

if [[ $# -lt 6 ]] ; then
    echo 'Usage: ./ultimate <inputfolder> <outputfolder> <gfyClientId> <gfyClientSecret> <gfyUserName> <gfyPassword> [playlist]'
    exit 0
fi

echo "Starting up"

inputfolder=$1
outputfolder=$2
clientId=$3
clientSecret=$4
userName=$5
password=$6

if [[ $# -eq 7 ]] ; then
    playlist=$7
    echo 'Play list not implemented yet'
    exit 0
fi

find $inputfolder -name "* *" -type d | rename 's/ /_/g'    # do the directories first
find $inputfolder -name "* *" -type f | rename 's/ /_/g'

# mass executing trailer maker
find $inputfolder -iname "*.mp4" -exec java -jar trailer-maker.jar -f {} -d 15000 -l 1000 -s 4000 -o $outputfolder --prepend-length --preserve \;
find $inputfolder -iname "*.mp4" -exec java -jar trailer-maker.jar -f {} -d 30000 -l 1500 -s 2000 -o $outputfolder --prepend-length --preserve \;

# mass executing gfycatUppload
find $outputfolder -iname "*.webm" -exec echo {} \; -exec java -jar gfyup.jar {} $clientId $clientSecret $userName $password \; -exec sleep 15 \;
