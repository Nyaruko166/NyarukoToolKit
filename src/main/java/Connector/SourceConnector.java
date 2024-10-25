package Connector;

import Model.Chapter;
import Util.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public interface SourceConnector {

    Logger log = LogManager.getLogger(SourceConnector.class);

    Path mangaDir = Paths.get(Config.getInstance().getProperty().getWorking_directory() + "/Mangas/");

    String getMangaTitle(String url);

    List<Chapter> getChapterListQQ(String url);

    void downloadManga(String title, List<Chapter> lstChapter);

    void downloadChapter(Chapter chapter, Path chapterPath);

}
