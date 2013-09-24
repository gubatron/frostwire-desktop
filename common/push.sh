#!/bin/bash

git subtree split --prefix=common --rejoin --branch common-backport
git push git@github.com:frostwire/frostwire-common.git common-backport:master
git branch -D common-backport