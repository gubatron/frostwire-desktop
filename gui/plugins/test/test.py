from com.frostwire.plugins.controllers import PluginManager

class MyPlugin:
  PM = None
  availablePlugins = None

  def __init__(self):
    self.PM = None
    self.availablePlugins = None
    print "PythonWorld: MyPlugin()"
    self.run()


  def run(self):
    print "PythonWorld: run()"
    self.PM = PluginManager.getInstance()
    self.PM.checkForAvailablePluginsRemotely(True)
    plugins = self.PM.getAvailablePlugins()


  def getPluginManagerInstance(self):
    print "PythonWorld: getPluginManagerInstance() invoked"
    return self.PM
