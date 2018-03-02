package me.cxis.zip;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Controller
public class ExportAndImportController {

    @GetMapping("/")
    public String index() {
        return "download";
    }

    @GetMapping(path = "/exportWithByteArrayResource", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> exportWithByteArrayResource() throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        zipOutputStream.putNextEntry(new ZipEntry("index.json"));
        zipOutputStream.write("{\"name\":\"Jack\"}".getBytes());

        zipOutputStream.putNextEntry(new ZipEntry("index.txt"));
        zipOutputStream.write("test".getBytes());

        zipOutputStream.closeEntry();
        zipOutputStream.close();
        byteArrayOutputStream.close();

        Resource body = new ByteArrayResource(byteArrayOutputStream.toByteArray());

        HttpStatus status = HttpStatus.OK;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "exportWithByteArrayResource.zip");

        return new ResponseEntity<>(body, headers, status);
    }

    @GetMapping(path = "/exportWithInputStreamResource", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> exportWithInputStreamResource() throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        zipOutputStream.putNextEntry(new ZipEntry("index.json"));
        zipOutputStream.write("{\"name\":\"Jack\"}".getBytes());

        zipOutputStream.putNextEntry(new ZipEntry("index.txt"));
        zipOutputStream.write("test".getBytes());

        zipOutputStream.closeEntry();
        zipOutputStream.close();
        byteArrayOutputStream.close();

        Resource body = new InputStreamResource(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

        HttpStatus status = HttpStatus.OK;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "exportWithInputStreamResource.zip");

        return new ResponseEntity<>(body, headers, status);
    }


    @GetMapping(path = "/exportWithResponse", produces = "application/zip")
    @ResponseBody
    public void exportWithResponse(HttpServletResponse response) throws IOException {
        response.setContentType("application/zip");
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader("Content-Disposition", "attachment; filename=\"exportWithResponse.zip\"");

        ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());

        zipOutputStream.putNextEntry(new ZipEntry("index.json"));
        zipOutputStream.write("{\"name\":\"Jack\"}".getBytes());

        zipOutputStream.putNextEntry(new ZipEntry("index.txt"));
        zipOutputStream.write("test".getBytes());

        zipOutputStream.closeEntry();
        zipOutputStream.close();

    }

    @PostMapping("import")
    @ResponseBody
    public void importZip(@RequestParam("file")MultipartFile file) throws IOException {

        ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream());

        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (zipEntry.isDirectory()) {
                // do nothing
            }else {
                String name = zipEntry.getName();
                long size = zipEntry.getSize();
                // unknown size
                if (size == -1) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while (true) {
                        int bytes = zipInputStream.read();
                        if (bytes == -1) break;
                        baos.write(bytes);
                    }
                    baos.close();
                    System.out.println(String.format("Name:%s,Content:%s",name,new String(baos.toByteArray())));
                } else {
                    byte[] bytes = new byte[(int) zipEntry.getSize()];
                    zipInputStream.read(bytes, 0, (int) zipEntry.getSize());
                    System.out.println(String.format("Name:%s,Content:%s",name,new String(bytes)));
                }
            }

        }
        zipInputStream.closeEntry();
        zipInputStream.close();
    }
}
