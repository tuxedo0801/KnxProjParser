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

import de.root1.knxprojparser.project.ParseException;
import de.root1.logging.JulFormatter;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achristian
 */
public class TestParser {

    private Logger log = LoggerFactory.getLogger(getClass());


    public TestParser() {
        JulFormatter.set();
    }

    @Test
    public void testETS4_2_0() throws IOException, FileNotSupportedException, ParseException {

        File f = new File(getClass().getResource("/ETS-Samples/ETS4.2.0-KnxProjParser-ExampleProject").getFile().replace("%20", " "));
        KnxProjParser parser = new KnxProjParser(f);
        parser.parse();
        Project project = parser.getProject();
        List<GroupAddress> gaList = project.getGroupaddressList();
        for (GroupAddress groupAddress : gaList) {
            log.info("{}", groupAddress);
        }
    }

    @Test
    public void testETS5_0_8() throws IOException, FileNotSupportedException, ParseException {
        File f = new File(getClass().getResource("/ETS-Samples/ETS5.0.8-KnxProjParser-ExampleProject").getFile().replace("%20", " "));
        KnxProjParser parser = new KnxProjParser(f);
        parser.parse();
        Project project = parser.getProject();
        List<GroupAddress> gaList = project.getGroupaddressList();
        for (GroupAddress groupAddress : gaList) {
            log.info("{}", groupAddress);
        }
    }

    @Test
    public void testETS5_5_1() throws IOException, FileNotSupportedException, ParseException {
        File f = new File(getClass().getResource("/ETS-Samples/ETS5.5.1-KnxProjParser-ExampleProject").getFile().replace("%20", " "));
        KnxProjParser parser = new KnxProjParser(f);
        parser.parse();
        Project project = parser.getProject();
        List<GroupAddress> gaList = project.getGroupaddressList();
        for (GroupAddress groupAddress : gaList) {
            log.info("{}", groupAddress);
        }
    }

    @Test
    public void testETS4_2_0_export() throws IOException, FileNotSupportedException, ParseException, ExportException {
        File f = new File(getClass().getResource("/ETS-Samples/ETS4.2.0-KnxProjParser-ExampleProject").getFile().replace("%20", " "));
        KnxProjParser parser = new KnxProjParser(f);
        File out = File.createTempFile("KnxProjParserExportTest", ".xml");
        parser.exportXml(out);
        out.delete();
    }
    
    @Test
    public void testMain() throws IOException, FileNotSupportedException, ParseException, ExportException {
        File f = new File(getClass().getResource("/ETS-Samples/ETS4.2.0-KnxProjParser-ExampleProject").getFile().replace("%20", " "));
        KnxProjParser.main(new String[]{f.getAbsolutePath()});
        File fout = new File(f.getAbsolutePath()+".parsed.xml");
        fout.delete();
    }
}
