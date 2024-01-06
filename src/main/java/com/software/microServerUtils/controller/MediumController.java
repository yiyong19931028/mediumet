package com.software.microServerUtils.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.zip.ZipOutputStream;
@Controller
@RequestMapping
public class MediumController {

    @PostMapping("/create-pdf")
    @ResponseBody
    public String createPdfFiles() {
        return "{\"success\": true}";
    }

    @GetMapping("/download-zip")
    public void downloadZip(HttpServletResponse response) {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=articles.zip");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
