package com.example.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@RestController
public class OcrController {

    private static String guessTessdataPath() {
        // 1) Env var wins if set
        String env = System.getenv("TESSDATA_PREFIX");
        if (env != null && new File(env).exists()) return env;

        String os = System.getProperty("os.name").toLowerCase();

        // 2) Common defaults per OS
        if (os.contains("win")) {
            String win = "C:\\\\Program Files\\\\Tesseract-OCR\\\\tessdata";
            if (new File(win).exists()) return win;
        } else if (os.contains("mac")) {
            // Homebrew locations (try both)
            String hb1 = "/opt/homebrew/Cellar/tesseract/5.0.0/share/tessdata";
            String hb2 = "/usr/local/share/tessdata";
            if (new File(hb1).exists()) return hb1;
            if (new File(hb2).exists()) return hb2;
        } else {
            // Linux/Debian defaults
            String lx1 = "/usr/share/tesseract-ocr/5/tessdata";
            String lx2 = "/usr/share/tesseract-ocr/tessdata";
            if (new File(lx1).exists()) return lx1;
            if (new File(lx2).exists()) return lx2;
        }
        // 3) Fallback: empty → Tess4J will try system defaults; may fail if none
        return "";
    }

    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> ocr(@RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "lang", defaultValue = "eng") String lang)
            throws IOException, TesseractException {

        long t0 = System.currentTimeMillis();
        File tmp = File.createTempFile("upload-", "-" + file.getOriginalFilename());
        file.transferTo(tmp);

        Tesseract tess = new Tesseract();
        String datapath = guessTessdataPath();
        if (!datapath.isEmpty()) tess.setDatapath(datapath);
        tess.setLanguage(lang);

        String text = tess.doOCR(tmp);
        long ms = System.currentTimeMillis() - t0;
        Files.deleteIfExists(tmp.toPath());

        Map<String, Object> resp = new HashMap<>();
        resp.put("language", lang);
        resp.put("datapath", datapath.isEmpty() ? "(default)" : datapath);
        resp.put("millis", ms);
        resp.put("text", text);
        return resp;
    }

    @RequestMapping(value = "/ocr", method = { RequestMethod.HEAD, RequestMethod.OPTIONS })
    public ResponseEntity<Void> ocrMeta() {
    // tells proxies & browsers: “/ocr exists — use POST for real work”
        return ResponseEntity.noContent().build(); // 204
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> m = new HashMap<>();
        m.put("ok", true);
        return m;
    }
}
