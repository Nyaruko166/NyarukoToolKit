package Connector;

import Model.Chapter;
import Util.Auth;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class testy implements SourceConnector {

    @Override
    public String getMangaTitle(String url) {
        log.info(mangaDir);
        return "";
    }

    @Override
    public List<Chapter> getChapterListQQ(String url) {
        return List.of();
    }

    @Override
    public void downloadManga(String title, List<Chapter> lstChapter) {

    }

    @Override
    public void downloadChapter(Chapter chapter, Path chapterPath) {

    }
}
