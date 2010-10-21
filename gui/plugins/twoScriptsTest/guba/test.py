from com import HelloFromJar
#import brooklyn.HelloFromJar

'''
This test, demonstrates, that plugins can import classes from another plugins.

It will also attempt to do an import of another python script within the jar.
'''

print "Hello from Python inside the Jar! (will now instanciate class that existss in this jar.\n\n"

#print "From brooklyn.jar/brooklyn.HelloFromJar (call from twoScriptsTest.jar/test.py)"
#hello = brooklyn.HelloFromJar()
#hello.sayHello()

#print "Now from twoScriptsTest.jar/twoScriptsTest.HelloFromJar (call from twoScriptsTest.jar/test/py)"
#hello2 = twoScriptsTest.HelloFromJar()
#hello2.sayHello()

h = HelloFromJar()
h.sayHello()
h.sayHello()
h.sayHello()

import sys
print "sys.path ->", sys.path

#JOptionPane.showConfirmDialog(None,"Hello from test.py within the twoScriptTest.jar");


from pleaseWork import echo

echo("FrostWire")
result = echo("Avalanche")
print "The result was", result


print "__name__ ->",__name__
