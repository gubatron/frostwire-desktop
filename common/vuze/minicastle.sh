#!/bin/bash

grep "import org\.bouncycastle\." * -R | awk {'print $1'} | cut -d ":" -f 1 > import_replace

for FILE in `cat import_replace`; do  perl -p -i -e 's/import org\.bouncycastle\./import org\.minicastle\./g' $FILE; done

rm import_replace