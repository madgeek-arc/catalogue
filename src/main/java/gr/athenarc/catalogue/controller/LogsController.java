package gr.athenarc.catalogue.controller;

import gr.athenarc.catalogue.utils.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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
            response.setHeader("Content-disposition", "attachment; filename=" + Paths.get(filepath).getFileName().toString());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
        }
    }
}
