#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"
pushd $DIR
git pull
hugo
popd
