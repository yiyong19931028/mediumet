package com.software.microServerUtils.utils;


import com.alibaba.fastjson.JSONObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MediumArticleTranslator {

    private static final String MEDIUM_URL = "https://medium.com/?tag=software-engineering";
    private static final String OUTPUT_FOLDER = "output";

    public static void main(String[] args) throws IOException {
        File outputFolder = new File(OUTPUT_FOLDER);
        if (!outputFolder.exists()) {
            outputFolder.mkdir();
        }

        List<String> articleUrls = getTop10Articles();

        for (String articleUrl : articleUrls) {
            String articleHtml = getArticleHtml(articleUrl);
            String articleText = extractArticleText(articleHtml);
            String translatedText = translateArticleText(articleText);
            saveArticleAsPdf(articleUrl, articleText, translatedText);
        }

        createZipFile(articleUrls);
    }

    private static List<String> getTop10Articles() throws IOException {
        List<String> articleUrls = new ArrayList<>();

        Document doc = Jsoup.connect(MEDIUM_URL).get();


        Elements articles = doc.select("a.postArticle-readMore");
        for (Element article : articles) {
            String articleUrl = article.attr("href");
            articleUrls.add(articleUrl);
        }

        return articleUrls.subList(0, 10);
    }

    private static String getArticleHtml(String articleUrl) throws IOException {
        Document doc = Jsoup.connect(articleUrl).get();

        return doc.select("div.section-inner").html();
    }

    private static String extractArticleText(String articleHtml) {
        String articleText = Jsoup.parse(articleHtml).text();

        return articleText;
    }

    /**
     * 对其的数据进行翻译
     * @param articleText
     * @return
     * @throws IOException
     */
    private static String translateArticleText(String articleText) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("text", articleText);
        requestBody.put("target", "zh");

        URL url = new URL("https://translation.googleapis.com/v3/projects/YOUR_PROJECT_ID/locations/global/translations:translateText?key=YOUR_API_KEY");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(requestBody.toString());
        writer.flush();
        writer.close();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String responseBody = reader.readLine();
        reader.close();

        JSONObject responseJson = JSONObject.parseObject(responseBody);
        String translatedText = responseJson.get("translations").toString();

        return translatedText;
    }

    /**
     * 保存对应pdf文件·1
     * @param articleUrl
     * @param articleText
     * @param translatedText
     * @throws IOException
     */
    private static void saveArticleAsPdf(String articleUrl, String articleText, String translatedText) throws IOException {

        PDDocument document = new PDDocument();


        PDPage page = new PDPage();
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        contentStream.setFont(PDType1Font.HELVETICA, 12);

        contentStream.beginText();
        contentStream.showText(articleText);
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(20, -20);
        contentStream.showText(translatedText);
        contentStream.endText();

        contentStream.close();

        String fileName = articleUrl.substring(articleUrl.lastIndexOf('/') + 1) + ".pdf";
        document.save(new File(OUTPUT_FOLDER, fileName));

        document.close();
    }

    /**
     * 创建zip文件
     * @param articleUrls
     * @throws IOException
     */
    private static void createZipFile(List<String> articleUrls) throws IOException {
                FileOutputStream fos = new FileOutputStream(OUTPUT_FOLDER + "/articles.zip");
        ZipOutputStream zos = new ZipOutputStream(fos);

        for (String articleUrl : articleUrls) {
            String fileName = articleUrl.substring(articleUrl.lastIndexOf('/') + 1) + ".pdf";
            File file = new File(OUTPUT_FOLDER, fileName);
            FileInputStream fis = new FileInputStream(file);
            zos.putNextEntry(new ZipEntry(fileName));
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
            fis.close();
        }

        zos.close();
    }
}
