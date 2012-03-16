package de.http;

public class ProxyDefintion {
    
    String user;
    String password;
    String proxy;
    String port;
    
    public ProxyDefintion(String user,String password,String proxy,String port) {
        
        this.user = user;
        this.password = password;
        this.proxy = proxy;
        this.port = port;
        
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getPort() {
        return port;
    }
    public void setPort(String port) {
        this.port = port;
    }
    public String getProxy() {
        return proxy;
    }
    public void setProxy(String proxy) {
        this.proxy = proxy;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    
    public String toString() {
        return proxy + ":" + port;
    }
}
