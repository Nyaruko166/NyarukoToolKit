package Connector;

import Model.Chapter;

import java.nio.file.Path;
import java.util.List;

public class testy implements SourceConnector {

    public static void main(String[] args) {
        testy t = new testy();
        t.getMangaTitle("adadad");

    }

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
