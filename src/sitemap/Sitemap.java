/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sitemap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import org.jdom2.*;
import org.jdom2.input.*;
import java.util.List;

/**
 *
 * @author Alexandr Evlanov
 */
public class Sitemap {

    public static void main(String[] args) throws Exception
    {
        System.out.println("Sitemap Tester v. 0.1b");
        System.out.println("Author: Alexandr Evlanov\n");

        Sitemap smClass = new Sitemap();
        SAXBuilder builder = new SAXBuilder();

        try {
            Document xmlDoc = builder.build(args[0]);
            //System.out.println(xmlDoc.getRootElement().getName());
            List<Element> temp = xmlDoc.getRootElement().getChildren();
            String[] urlList = new String[temp.size()];
            for (int i = 0; i < temp.size(); ++i) {
                //System.out.println(temp.get(i).getChildren().get(0).getValue());
                urlList[i] = temp.get(i).getChildren().get(0).getValue();
            }
            sendRequest(urlList);
        }
        catch (JDOMException | IOException e) {
            System.err.println(e.getMessage());
            //e.getCause().printStackTrace();
        }
    }

    /**
     * Проверяет УРЛы из списка
     * @param urlList
     * @throws Exception
     */
    public static void sendRequest(String[] urlList) throws Exception
    {
        String host = getHost(urlList[0]);
        int port = 80;

        System.out.println("Host: " + host + ":" + port);
        System.out.println("URL count: " + urlList.length);

        String header = "";
        Socket socket = null;
        String res = null;
        String[] restofile = new String[urlList.length];

        try {
            for (int i = 0; i < urlList.length; i++) {
                socket = new Socket(host, port);
                //System.out.println("Создан сокет: " + host + " port:" + port);

                InputStreamReader isr = new InputStreamReader(socket.getInputStream());
                BufferedReader bfr = new BufferedReader(isr);
                StringBuilder sbf = new StringBuilder();

                header = "HEAD " + urlList[i] + " HTTP/1.0\n\n" +
                    "Connection: Close\n\n" +
                    "User-Agent: Mozilla/4.05 (WinNT; 1)\n\n" +
                    "Host: www.ronikon.ru\n\n" +
                    "Accept: image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, */*\n\n";
                socket.getOutputStream().write(header.getBytes());
                //System.out.println("Заголовок отправлен. \n");


                int ch = bfr.read();
                while (ch != -1) {
                    sbf.append((char) ch);
                    ch = bfr.read();
                }
                res = sbf.toString();

                if (!res.contains("HTTP/1.1 200 OK")) {

                    restofile[i] = urlList[i] + " > " + res.split("\\r\\n")[0];
                    System.out.println(restofile[i]);
                }

                socket.close();
            }
        }
        catch (Exception e) {
            throw new Exception("Ошибка при отправке запроса: " + e.getMessage(), e);
        }

        save(restofile);
    }

    /**
     * Возвращает домен из URLа
     * @param url
     * @return String
     * @throws Exception
     */
    public static String getHost(String url) throws Exception
    {
        String[] urlArray = url.split("//?");

        return urlArray[1];
    }

    /**
     * Сохраняет список адресов в файл
     * @param list
     */
    public static void save(String[] list) {

        System.out.println("Save result...");

        FileWriter writeFile = null;
        try {
            File logFile = new File("error.txt");
            writeFile = new FileWriter(logFile);
            for (int i = 0; i < list.length; i++) {
                if (list[i] != null) {
                    writeFile.append(list[i] + "\n");
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(writeFile != null) {
                try {
                    writeFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
