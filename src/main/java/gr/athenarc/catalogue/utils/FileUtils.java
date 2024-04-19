/*
 * Copyright 2021-2024 OpenAIRE AMKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.athenarc.catalogue.utils;

import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains methods for files and directories.
 */
public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Retrieves the contents of a file.
     *
     * @param filepath The path of the file to read.
     * @return {@link ByteArrayResource}
     */
    public static ByteArrayResource readFile(String filepath) {
        ByteArrayResource resource = null;

        try {
            Path path = Paths.get(filepath);
            resource = new ByteArrayResource(Files.readAllBytes(path));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ResourceNotFoundException("File '" + filepath + "' does not exist..", e);
        }

        return resource;
    }

    /**
     * Retrieves the contents of a directory.
     *
     * @param dir The directory to check.
     * @return {@link List}<{@link String}>
     */
    public static List<String> getFolderContents(String dir) {
        List<String> folderContents = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Paths.get(dir))) {
            folderContents = paths
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return folderContents;
    }

    private FileUtils() {
    }
}
