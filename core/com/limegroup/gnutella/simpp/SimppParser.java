package com.limegroup.gnutella.simpp;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.limegroup.gnutella.xml.LimeXMLUtils;

public class SimppParser {
    
    private static final Log LOG = LogFactory.getLog(SimppParser.class);

    private static final String VERSION = "version";
    
    private static final String PROPS = "props";

    private int _version;
    private String _propsData;    

    //Format of dataBytes:
    //<xml for version related info with one tag containing all the props data>
    //TODO1: Change the way this is parsed as per the format described above. 
    public SimppParser(byte[] dataBytes) throws IOException {
        parseInfo(new String(dataBytes, "UTF-8"));
    }
    
    public int getVersion() {
        return _version;
    }

    public String getPropsData() {
        return _propsData;
    }

    ///////////////////////////private helpers////////////////////////
    /**
     * This has got to be the stupidest piece of code in LimeWire.
     * Why the hell make SIMPP into XML, if the actual SIMPP configurations
     * are passed as a simple string.
     * 
     * Why not just pass this as a simple text file
     * SIGNATURE|version
     * SIMPPDATA
     * SIMPPDATA
     * ...
     * EOF
     * 
     * they waste more time using XMLUtils and parsing tags than
     * getting the actual parameters.
     * 
     * lame.
     * Gubs.
     */
    private void parseInfo(String xmlStr) throws IOException {
        if(xmlStr == null || xmlStr.equals(""))
            throw new IOException("null xml for version info");
        //System.out.println("SimppParser.parseInfo(): Getting parse information: " + xmlStr + "\n********************END OF PARSED INFORMATION**********\n\n");
        Document d = XMLUtils.getDocument(xmlStr, LOG);
        Element docElement = d.getDocumentElement();
        NodeList children = docElement.getChildNodes();
        int len = children.getLength();
        for(int i= 0; i< len; i++) {
            Node node = children.item(i);
            String nodeName = node.getNodeName().toLowerCase().trim();
            String value = LimeXMLUtils.getText(node.getChildNodes());
            if(nodeName.equals(VERSION)) {
                String ver = value;
                try {
                    _version = Integer.parseInt(ver);
                } catch(NumberFormatException nfx) {
                    LOG.error("Unable to parse version number: " + nfx);
                    _version = -1;
                }
            }
            else if(nodeName.equals(PROPS)) {
                _propsData = value;
            }
        }//end of for -- done all child nodes
    }
}