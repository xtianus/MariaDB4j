/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2014 Michael Vorburger
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package ch.vorburger.mariadb4j.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ch.vorburger.mariadb4j.DBConfiguration;
import ch.vorburger.mariadb4j.DBConfiguration.Executable;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import ch.vorburger.mariadb4j.Util;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DBConfigurationBuilderTest {

    @Test
    public void defaultDataDirIsTemporaryAndIncludesPortNumber() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        File defaultDataDir = config.getDataDir();
        assertTrue(Util.isTemporaryDirectory(defaultDataDir));
        int port = config.getPort();
        assertTrue(defaultDataDir.toString().contains(Integer.toString(port)));
    }

    @Test
    public void defaultDataDirIsTemporaryAndIncludesPortNumberEvenIfPortIsExplicitlySet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setPort(12345);
        DBConfiguration config = builder.build();
        File defaultDataDir = config.getDataDir();
        assertTrue(Util.isTemporaryDirectory(defaultDataDir));
        assertTrue(defaultDataDir.toString().contains(Integer.toString(12345)));
    }

    @Test
    public void dataDirDoesNotIncludePortNumberEvenItsExplicitlySet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setDataDir(new File("db/data"));
        DBConfiguration config = builder.build();
        File defaultDataDir = config.getDataDir();
        assertEquals(new File("db/data"), defaultDataDir);
        assertFalse(Util.isTemporaryDirectory(defaultDataDir));
    }

    @Test
    public void resetDataDirToDefaultTemporary() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setDataDir(new File("db/data"));
        assertEquals(new File("db/data"), builder.getDataDir());
        builder.setDataDir(null);
        DBConfiguration config = builder.build();
        File defaultDataDir = config.getDataDir();
        assertTrue(Util.isTemporaryDirectory(defaultDataDir));
        int port = config.getPort();
        assertTrue(defaultDataDir.toString().contains(Integer.toString(port)));
    }

    @Test
    public void defaultTmpDirIsTemporaryAndIncludesPortNumber() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        File defaultTmpDir = config.getTmpDir();
        assertTrue(Util.isTemporaryDirectory(defaultTmpDir));
        int port = config.getPort();
        assertTrue(defaultTmpDir.toString().contains(Integer.toString(port)));
    }

    @Test
    public void defaultTmpDirIsTemporaryAndIncludesPortNumberEvenIfPortIsExplicitlySet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setPort(12345);
        DBConfiguration config = builder.build();
        File defaultTmpDir = config.getTmpDir();
        assertTrue(Util.isTemporaryDirectory(defaultTmpDir));
        assertTrue(defaultTmpDir.toString().contains(Integer.toString(12345)));
    }

    @Test
    public void tmpDirDoesNotIncludePortNumberEvenItsExplicitlySet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setTmpDir("db/tmp");
        DBConfiguration config = builder.build();
        File defaultTmpDir = config.getTmpDir();
        assertFalse(Util.isTemporaryDirectory(defaultTmpDir));
        var defaultTmpDirString = defaultTmpDir.toString();
        assertTrue(
                defaultTmpDirString, defaultTmpDirString.contains("db" + File.separator + "tmp"));
    }

    @Test
    public void resetTmpDirToDefaultTemporary() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setTmpDir("db/tmp");
        assertEquals(new File("db/tmp"), builder.getTmpDir());
        builder.setTmpDir(null);
        assertTrue(Util.isTemporaryDirectory(builder.getTmpDir()));
        builder.setTmpDir(null);
        DBConfiguration config = builder.build();
        File defaultTmpDir = config.getTmpDir();
        assertTrue(Util.isTemporaryDirectory(defaultTmpDir));
    }

    @Test
    public void defaultLibDirIsRelativeToBaseDir() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        File defaultBaseDir = config.getBaseDir();
        assertTrue(Util.isTemporaryDirectory(defaultBaseDir));
        File defaultLibDir = config.getLibDir();
        assertEquals(defaultLibDir, new File(defaultBaseDir, "libs"));
    }

    @Test
    public void defaultLibDirIsRelativeToUpdatedBaseDir() throws IOException {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        Path baseDirPath = Files.createTempDirectory("MariaDB4j");
        builder.setBaseDir(baseDirPath.toFile());
        DBConfiguration config = builder.build();

        File baseDir = config.getBaseDir();
        File defaultLibDir = config.getLibDir();
        assertEquals(defaultLibDir, new File(baseDir, "/libs"));
    }

    @Test
    public void modifiedLibDir() throws IOException {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        Path libDir = Files.createTempDirectory("libsdir");
        builder.setLibDir(libDir.toFile());
        Path baseDir = Files.createTempDirectory("mariadb");
        builder.setBaseDir(baseDir.toFile());
        DBConfiguration config = builder.build();

        File updatedLibDir = config.getLibDir();
        assertEquals(updatedLibDir, libDir.toFile());
    }

    @Test
    public void deletesTemporaryDirectoriesAsDefault() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        assertTrue(config.isDeletingTemporaryBaseAndDataDirsOnShutdown());
    }

    @Test
    public void keepsTemporaryDirectories() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setDeletingTemporaryBaseAndDataDirsOnShutdown(false);
        DBConfiguration config = builder.build();
        assertFalse(config.isDeletingTemporaryBaseAndDataDirsOnShutdown());
    }

    @Test
    public void defaultCharacterSet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        String character = "utf8mb4";
        builder.setDefaultCharacterSet(character);
        DBConfiguration config = builder.build();
        String defaultCharacterSet = config.getDefaultCharacterSet();
        assertEquals(character, defaultCharacterSet);
    }

    @Test
    public void defaultCharacterSetIsEmpty() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        String defaultCharacterSet = config.getDefaultCharacterSet();
        assertNull(defaultCharacterSet);
    }

    @Test
    public void defaultExecutables() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        var pathSeparator = System.getProperty("file.separator");
        var expectedMariaDB4jString = "MariaDB4j/base/".replace("/", pathSeparator);
        var expectedMariaDBString = "bin/mariadbd".replace("/", pathSeparator);
        var expectedMySqlString = "bin/mysqld".replace("/", pathSeparator);
        String executable = config.getExecutable(Executable.Server).toString();
        assertTrue(
                executable.contains(expectedMariaDB4jString)
                        || executable.contains(expectedMariaDBString)
                        || executable.contains(expectedMySqlString));
    }

    @Test
    public void customExecutables() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setExecutable(Executable.Server, "/usr/sbin/mariadbd");
        DBConfiguration config = builder.build();
        assertEquals(new File("/usr/sbin/mariadbd"), config.getExecutable(Executable.Server));
    }
}
