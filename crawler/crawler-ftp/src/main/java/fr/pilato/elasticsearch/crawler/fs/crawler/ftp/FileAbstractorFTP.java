/*
 * Licensed to David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.pilato.elasticsearch.crawler.fs.crawler.ftp;

import fr.pilato.elasticsearch.crawler.fs.crawler.FileAbstractModel;
import fr.pilato.elasticsearch.crawler.fs.crawler.FileAbstractor;
import fr.pilato.elasticsearch.crawler.fs.framework.FsCrawlerUtil;
import fr.pilato.elasticsearch.crawler.fs.settings.FsSettings;
import fr.pilato.elasticsearch.crawler.fs.settings.Server;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class FileAbstractorFTP extends FileAbstractor<FTPFile> {
    private final Logger logger = LogManager.getLogger(FileAbstractorFTP.class);

    private FTPClient ftp;

    private final OutputStream loggerOutputStream = IoBuilder.forLogger(logger).buildOutputStream();

    private final PrintCommandListener ftpListener = new PrintCommandListener(new PrintWriter(loggerOutputStream));

    private String controlEncoding = FTP.DEFAULT_CONTROL_ENCODING;

    public FileAbstractorFTP(FsSettings fsSettings) {
        super(fsSettings);
    }

    @Override
    public FileAbstractModel toFileAbstractModel(String _path, FTPFile file) {
        String filename = file.getName();
        String extension = FilenameUtils.getExtension(filename);
        String path = _path;

        // if server is not using utf-8
        if (controlEncoding.equals(FTP.DEFAULT_CONTROL_ENCODING)) {
            try {
                filename = new String(filename.getBytes(controlEncoding), StandardCharsets.UTF_8);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                path = new String(_path.getBytes(controlEncoding), StandardCharsets.UTF_8);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return new FileAbstractModel(
                filename,
                file.isFile(),
                // We are using here the local TimeZone as a reference. If the remote system is under another TZ, this might cause issues
                LocalDateTime.ofInstant(Instant.ofEpochMilli(file.getTimestamp().getTimeInMillis()), ZoneId.systemDefault()),
                // We don't have the creation date
                null,
                // We don't have the access date
                null,
                extension,
                path,
                path.equals("/") ? path.concat(filename) : path.concat("/").concat(filename),
                file.getSize(),
                file.getUser(),
                file.getGroup(),
                FsCrawlerUtil.getFilePermissions(file));
    }

    @Override
    public InputStream getInputStream(FileAbstractModel file) throws Exception {
        // FTP data connection could be closed after transfer process.
        openFTPConnection();

        ftp.enterLocalPassiveMode();
        String fullPath = file.getFullpath();
        if (controlEncoding.equals(FTP.DEFAULT_CONTROL_ENCODING)) {
            fullPath = new String(fullPath.getBytes(StandardCharsets.UTF_8), FTP.DEFAULT_CONTROL_ENCODING);
        }
        return ftp.retrieveFileStream(fullPath);
    }

    @Override
    public Collection<FileAbstractModel> getFiles(String dir) throws IOException {
        // FTP data connection could be closed after transfer process.
        openFTPConnection();

        if (controlEncoding.equals(FTP.DEFAULT_CONTROL_ENCODING)) {
            dir = new String(dir.getBytes(StandardCharsets.UTF_8), FTP.DEFAULT_CONTROL_ENCODING);
        }
        logger.debug("Listing local files from {}", dir);

        ftp.enterLocalPassiveMode();
        FTPFile[] ftpFiles = ftp.listFiles(dir);
        if (ftpFiles == null) return null;
        List<FTPFile> files = Arrays.stream(ftpFiles).filter(file -> {
            if (fsSettings.getFs().isFollowSymlinks()) return true;
            return !file.isSymbolicLink();
        }).collect(Collectors.toList());

        Collection<FileAbstractModel> result = new ArrayList<>(files.size());
        // Iterate other files
        // We ignore here all files like . and ..
        String finalDir = dir;
        result.addAll(files.stream().filter(file -> !".".equals(file.getName()) &&
                !"..".equals(file.getName()))
                .map(file -> toFileAbstractModel(finalDir, file))
                .collect(Collectors.toList()));

        logger.debug("{} local files found", result.size());
        return result;
    }

    @Override
    public boolean exists(String dir) {
        try {
            if (controlEncoding.equals(FTP.DEFAULT_CONTROL_ENCODING)) {
                dir = new String(dir.getBytes(StandardCharsets.UTF_8), FTP.DEFAULT_CONTROL_ENCODING);
            }
            return ftp.changeWorkingDirectory(dir);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void open() throws IOException {
        Server server = fsSettings.getServer();
        logger.debug("Opening FTP connection to {}@{}", server.getUsername(), server.getHostname());

        ftp = new FTPClient();
        ftp.addProtocolCommandListener(ftpListener);
        // send a safe command (i.e. NOOP) over the control connection to reset the router's idle timer
        ftp.setControlKeepAliveTimeout(300);
        openFTPConnection();
    }

    @Override
    public void close() throws IOException {
        ftp.logout();
        ftp.disconnect();
    }

    private void openFTPConnection() throws IOException {
        Server server = fsSettings.getServer();
        ftp.connect(server.getHostname(), server.getPort());

        // checking FTP client connection.
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            logger.warn("Cannot connect with FTP to {}@{}", server.getUsername(),
                server.getHostname());
            throw new RuntimeException("Can not connect to " + server.getUsername() + "@" + server.getHostname());
        }

        if (!ftp.login(server.getUsername(), server.getPassword())) {
            ftp.disconnect();
            throw new RuntimeException("Please check ftp user or password");
        }

        int utf8Reply = ftp.sendCommand("OPTS UTF8", "ON");
        if (FTPReply.isPositiveCompletion(utf8Reply)) {
            controlEncoding = StandardCharsets.UTF_8.displayName();
            ftp.setControlEncoding(controlEncoding);
        }

        logger.debug("FTP connection successful");
    }
}
