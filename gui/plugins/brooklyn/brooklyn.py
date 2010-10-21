from com.frostwire.plugins.models import Plugin
#Java Class inside this Jar. Should be in the classpath
from com.plugwire import HelloFromJar
#from com import HelloFromJar

class Brooklyn (Plugin):
    def __init_(self):
        Plugin.__init__(self)
        self.classLoader = None

    def setClassLoader(self, cl):
        print "Brooklyn.setClassLoader() - Received object from Java World:", cl
        self.classLoader = cl
        #print dir(cl)
        #print cl.URLs
        #url = cl.findResource("com.plugwire.HelloFromJar")
        #print "url found:",url

    def start(self):
        print "Brooklyn.start() - Groovy, this is python world right here."
        print "Brooklyn.start() - MyName is ", self.getName()

        #if self.classLoader is not None:
        #    HelloFromJar = self.classLoader.loadClass("com.plugwire.HelloFromJar",True)
        #    print "Got it now on start?", HelloFromJar
        HelloFromJar().sayHello()



        
