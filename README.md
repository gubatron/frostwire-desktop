frostwire-common
================

common components and libraries shared across frostwire clients.

You can work inside common/ and commit to frostwire-desktop repository. Once your 'common/' changes are good, go
to the root folder of frostwire-desktop and invoke:
./common/push.sh

This will grab the commits related to the 'common/' folder and push them to the github.com/frostwire/frostwire-common.git repository.

If you need to get the latest changes from the github.com/frostwire/frostwire-common.git repository invoke from the root folder of frostwire-desktop:
./common/pull.sh

-----------------------

#Setup
git subtree add --prefix=common --squash -m "Added frostwire-common as a subtree" https://github.com/frostwire/frostwire-common.git master

#Push
git subtree split --prefix=common --rejoin --branch common-backport
git push https://github.com/frostwire/frostwire-common.git common-backport:master
git branch -D common-backport

#Pull
git subtree pull --prefix=common -m "Merged frostwire-common changes" https://github.com/frostwire/frostwire-common.git master

There are a couple of helper scripts inside.