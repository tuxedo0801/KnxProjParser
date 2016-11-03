/*
 * Copyright (C) 2016 Alexander Christian <alex(at)root1.de>. All rights reserved.
 * 
 * This file is part of KnxProjParser.
 *
 *   KnxProjParser is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   KnxProjParser is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with KnxProjParser.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.root1.knxprojparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achristian
 */
public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);
    
    static String getKeyForValue(Map<String, List<String>> refMap, String internalId) {

        Iterator<String> iterator = refMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            List<String> list = refMap.get(key);
            for (String value : list) {
                if (value.equals(internalId)) {
                    return key;
                }
            }
        }
        return null;
    }

    static File createTempDirectory() throws IOException {
        final File temp;

        temp = File.createTempFile("KnxProjParser", Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }
        return (temp);
    }

    static void extract(File knxprojfile, File targetDir) {
        try {
            if (!knxprojfile.exists()) {
                throw new IllegalArgumentException("Given file '" + knxprojfile.getAbsolutePath() + "' does not exist");
            }
            // Open the zip file
            try (ZipFile zipFile = new ZipFile(knxprojfile)) {
                Enumeration<?> enu = zipFile.entries();
                while (enu.hasMoreElements()) {
                    ZipEntry zipEntry = (ZipEntry) enu.nextElement();

                    String name = zipEntry.getName();
                    long size = zipEntry.getSize();
                    long compressedSize = zipEntry.getCompressedSize();
                    log.debug(String.format("name: %-20s | size: %6d | compressed size: %6d\n",
                            name, size, compressedSize));

                    // Do we need to create a directory ?
                    File file = new File(targetDir, name);
                    if (name.endsWith("/")) {
                        file.mkdirs();
                        continue;
                    }

                    File parent = file.getParentFile();
                    if (parent != null) {
                        parent.mkdirs();
                    }

                    FileOutputStream fos;
                    // Extract the file
                    try (InputStream is = zipFile.getInputStream(zipEntry)) {
                        fos = new FileOutputStream(file);
                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = is.read(bytes)) >= 0) {
                            fos.write(bytes, 0, length);
                        }
                    }
                    fos.close();

                }
            } catch (ZipException ex) {
                log.error("Error opening '" + knxprojfile.getAbsolutePath() + "'", ex);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int[] convertGroupAddress(int intAddr) {
        int[] ga = new int[3];

        // 1st
        ga[0] = intAddr >> 11;

        // 2nd
        ga[1] = (intAddr >> 8) & 7;

        // 3rd
        ga[2] = intAddr & 0xFF;

        return ga;
    }

    public static int[] convertIndividualAddress(int intAddr) {
        int[] ga = new int[3];

        // area
        ga[0] = intAddr >> 12;
        // line
        ga[1] = (intAddr >> 8) & 7;
        // member
        ga[2] = intAddr & 0xFF;

        return ga;
    }

    public static DecimalFormat df = new DecimalFormat("000");

    public static String convertDpt(String dpt) {

        int mainType = 0;
        int subType = 0;

        String result = "";

        if (dpt != null && !dpt.isEmpty()) {

            if (dpt.contains("DPST") || dpt.contains("DPT")) {
                dpt = dpt.split(" ")[0];
                String[] split = dpt.split("-");
                if (split[0].equals("DPST")) {
                    mainType = Integer.parseInt(split[1]);
                    subType = Integer.parseInt(split[2]);
                } else if (split[0].equals("DPT")) {
                    mainType = Integer.parseInt(split[1]);
                    subType = 0;
                }
            } else {

                switch (dpt) {
                    case "1 Bit":
                        return "1.001";
                    case "2 Bit":
                        return "2.001";
                    default:
                        return "0.000";
                }

            }
        }

        result = mainType + "." + df.format(subType);

        return result;
    }

    public static String byteArrayToHex(byte[] bytearray, boolean whitespace) {
        StringBuilder sb = new StringBuilder(bytearray.length * 2);

        for (int i = 0; i < bytearray.length; i++) {
            sb.append(String.format("%02x", bytearray[i] & 0xff));
            if (i < bytearray.length - 1 && whitespace) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public static XMLGregorianCalendar dateToXmlDateTime(Date d) throws DatatypeConfigurationException {
        GregorianCalendar gcalendar = new GregorianCalendar();
        gcalendar.setTime(d);
        XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcalendar);
        return xmlDate;
    }
    
    public static Date xmlDateTimeToDate(XMLGregorianCalendar x) {
        GregorianCalendar gcalendar = x.toGregorianCalendar();
        return gcalendar.getTime();
    }

    /**
     * CReates (sha256) checksum for given file
     *
     * @param f
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static String createChecksum(File f) throws NoSuchAlgorithmException, IOException {
        InputStream fis = new FileInputStream(f);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("SHA1");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return byteArrayToHex(complete.digest(), false);
    }

}
