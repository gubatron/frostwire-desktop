var portableSource;
var portableTarget;

portableSource = args(0);
portableTarget = args(1);

function IsFrostWireRunning() {
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

function WaitFrostWireStopped() {
    for (var i = 0; i < 30; i++) {
        if (!IsFrostWireRunning()) {
            return true;
        }
        WScript.Sleep(1000);
    }
    
    return false;
}

function CopyFrostWireFiles() {
WScript.Echo("Moving " + portableSource + " " + portableTarget);
    var fso = WScript.CreateObject("Scripting.FileSystemObject");
    fso.DeleteFolder(portableTarget);
    fso.MoveFolder(portableSource, portableTarget);
}

if (WaitFrostWireStopped()) {
    CopyFrostWireFiles();
}