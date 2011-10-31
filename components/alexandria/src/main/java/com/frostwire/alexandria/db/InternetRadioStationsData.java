package com.frostwire.alexandria.db;

import java.util.LinkedList;
import java.util.List;

public final class InternetRadioStationsData {

    private List<List<Object>> data;

    public InternetRadioStationsData() {
        data = new LinkedList<List<Object>>();

        add(data, "top 100 station - Germanys No.1 Web Hit Station", "top 100 station - Germanys No.1 Web Hit Station", "http://87.230.101.50:80", "128 kbps", "mp3", "http://www.top100station.de", "Top40", "");
        add(data, "Antena 1", "Antena 1", "http://174.36.237.118:7210", "64 kbps", "AAC+", "http://www.antena1.com.br", "Classical", "[playlist]\nnumberofentries=20\nFile1=http://174.36.237.118:7210\nTitle1=(#1 - 36/1000) Antena 1\nLength1=-1\nFile2=http://173.192.70.157:7150\nTitle2=(#2 - 143/1000) Antena 1\nLength1=-1\nFile3=http://173.192.70.157:7100\nTitle3=(#3 - 144/1000) Antena 1\nLength1=-1\nFile4=http://173.192.70.157:7160\nTitle4=(#4 - 145/1000) Antena 1\nLength1=-1\nFile5=http://173.192.70.157:7060\nTitle5=(#5 - 146/1000) Antena 1\nLength1=-1\nFile6=http://173.192.70.157:7080\nTitle6=(#6 - 147/1000) Antena 1\nLength1=-1\nFile7=http://173.192.70.157:7070\nTitle7=(#7 - 147/1000) Antena 1\nLength1=-1\nFile8=http://173.192.70.157:7140\nTitle8=(#8 - 147/1000) Antena 1\nLength1=-1\nFile9=http://173.192.70.157:7170\nTitle9=(#9 - 147/1000) Antena 1\nLength1=-1\nFile10=http://173.192.70.157:7130\nTitle10=(#10 - 147/1000) Antena 1\nLength1=-1\nFile11=http://173.192.70.157:7030\nTitle11=(#11 - 147/1000) Antena 1\nLength1=-1\nFile12=http://173.192.70.157:7180\nTitle12=(#12 - 148/1000) Antena 1\nLength1=-1\nFile13=http://173.192.70.157:7090\nTitle13=(#13 - 148/1000) Antena 1\nLength1=-1\nFile14=http://173.192.70.157:7110\nTitle14=(#14 - 148/1000) Antena 1\nLength1=-1\nFile15=http://173.192.70.157:7040\nTitle15=(#15 - 148/1000) Antena 1\nLength1=-1\nFile16=http://173.192.70.157:7000\nTitle16=(#16 - 149/1000) Antena 1\nLength1=-1\nFile17=http://173.192.70.157:7010\nTitle17=(#17 - 149/1000) Antena 1\nLength1=-1\nFile18=http://173.192.70.157:7190\nTitle18=(#18 - 150/1000) Antena 1\nLength1=-1\nFile19=http://173.192.70.157:7020\nTitle19=(#19 - 152/1000) Antena 1\nLength1=-1\nFile20=http://173.192.70.157:7120\nTitle20=(#20 - 155/1000) Antena 1\nLength1=-1\nVersion=2\n");
    }

    public List<List<Object>> getData() {
        return data;
    }

    private void add(List<List<Object>> data, String name, String description, String url, String bitrate, String type, String website, String genre, String pls) {
        List<Object> row = new LinkedList<Object>();

        row.add(name);
        row.add(description);
        row.add(url);
        row.add(bitrate);
        row.add(type);
        row.add(website);
        row.add(genre);
        row.add(pls);

        data.add(row);
    }
}
