/**
 * Copyright 2021-2025 OpenAIRE AMKE
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

package gr.athenarc.catalogue.controller;

import gr.athenarc.catalogue.utils.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.nio.file.Paths;

/**
 * Enables changing Log Level dynamically and retrieving logs through controller.
 * It supports changes only in log4j logging configuration.
 */
@RestController
@RequestMapping("logs")
public class LogsController {

    private static final Logger logger = LoggerFactory.getLogger(LogsController.class);

    @PostMapping("level/root")
    public ResponseEntity<Void> setRootLogLevel(@RequestParam Level standardLevel) {
        logger.info("Changing Root Level Logging to: {}", standardLevel);
        Configurator.setRootLevel(standardLevel);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("level/package/{path}")
    public ResponseEntity<Void> setPackageLogLevel(@PathVariable("path") String path, @RequestParam Level standardLevel) {
        logger.info("Changing '{}' Logger Level to '{}'", path, standardLevel);
        Configurator.setLevel(path, standardLevel);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<?> fetchLogFile(@RequestParam(required = false) String filepath, HttpServletResponse response) {
        String logsDir = "logs/";

        if (!StringUtils.hasText(filepath)) {
            return ResponseEntity.ok().body(FileUtils.getFolderContents(logsDir));
        } else {
            ByteArrayResource resource = FileUtils.readFile(filepath);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + Paths.get(filepath).getFileName().toString());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
        }
    }
}
