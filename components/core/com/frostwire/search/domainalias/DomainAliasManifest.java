package com.frostwire.search.domainalias;

import java.util.List;
import java.util.Map;

/**
 * Modeled after a JSON manifest that could look like this
 * 
 * {
 *   "version": 134,
 *   "lastUpdated":1383936035000,
 *   "aliases": {
 *      "site.com":["siteMirror1.com","siteMirror2.com","siteMirrorN.com"],
 *      "otherSite.com":["otherSite.org","otherSite.net","mirror.otherSite.io"]
 *   }
 *  }
 */
public class DomainAliasManifest {
    public int version;
    public long lastUpdated;
    public Map<String,List<String>> aliases;
}