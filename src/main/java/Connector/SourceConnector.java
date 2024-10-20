package Connector;

import Model.Chapter;

import java.nio.file.Path;
import java.util.List;

public interface SourceConnector {

    String getMangaTitle(String url);

    List<Chapter> getChapterListQQ(String url);

    void downloadManga(String title, List<Chapter> lstChapter);

    void downloadChapter(Chapter chapter, Path chapterPath);

}
