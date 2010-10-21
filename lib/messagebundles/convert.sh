#!/bin/bash


uniquify() {
    echo "Uniquifying po files";
    
    for i in *.po; do
	msguniq -o $i.uniq -u $i;
#	msguniq --use-first -o $i.uniq -u $i;
    done
}

rename() {
    echo "Renaming them";
    
    for i in *.uniq; do
	mv $i `basename $i | sed -e 's/.uniq//'`;
    done
}
 
merge() {   
    echo "Merging them";
    
    for i in *.po; do
	msgmerge --backup=numbered -U $i extractedkeys.pot;
    done
}

generateDefault() {
    echo "Generating default catalog";
    msgen extractedkeys.pot -o default.po
    perl -p -i -e "s/CHARSET/UTF-8/g;" default.po
}

classbundle() {
    echo "Creating class ResourceBundles";
    echo "Cleaning previous files...";
    rm org/limewire/i18n/*.class
    echo "Generating new files";
    for i in *.po; do                
		# FTA: The default.po file is not included as a new Language
		if [ "$i" != "default.po" ]; then		
		echo "Creating resource for $i";  
		msgfmt --java2 -d . -r org.limewire.i18n.Messages -l `basename $i | sed -e 's/.po//'` $i;
		fi
    done
	msgfmt --java2 -d . -r org.limewire.i18n.Messages default.po
}

propsbundle() {
    echo "Creating properties RersourceBundles";
    
    for i in *.po; do
	msgcat -t "UTF-8" -p -o "LimeMessages_`basename $i | sed -e 's/.po//'`.properties" $i;
    done
    msgcat -t "UTF-8" -p -o LimeMessages.properties default.po
}

makejar() {
    echo "Making jar";
	#FTA it removes previous messages.jar to avoid problems
        rm ../jars/messages.jar
	jar -cf ../jars/messages.jar org
}

#uniquify
#rename
#merge
#generateDefault
classbundle
#propsbundle
makejar
