function isFrostWireRunning() {
    var wmi = GetObject("winmgmts://./root/cimv2");

    var colItems = wmi.ExecQuery("SELECT * FROM Win32_Process");

    var enumItems = new Enumerator(colItems); 

    for (; !enumItems.atEnd(); enumItems.moveNext()) { 
        var item = enumItems.item();
        if (item.Name === "FrostWire.exe") {
            return true;
        }
    }
    
    return false;
}

function waitFrostWireStopped() {
    for (var i = 0; i < 30; i++) {
        if (!isFrostWireRunning()) {
            return true;
        }
        WScript.Sleep(1000);
    }
    
    return false;
}

if (waitFrostWireStopped()) {
    WScript.Echo("Si");
} else {
    WScript.Echo("No");
}