frostwire-common
================

common components and libraries shared across frostwire clients.

#Setup
git subtree add --prefix=common --squash -m "Added frostwire-common as a subtree" https://github.com/frostwire/frostwire-common.git master

#Push
git subtree split --prefix=common --rejoin --branch common-backport
git push https://github.com/frostwire/frostwire-common.git common-backport:master
git branch -D common-backport

#Pull
git subtree pull --prefix=common -m "Merged frostwire-common changes" https://github.com/frostwire/frostwire-common.git master

There are a couple of helper scripts inside.