package Handler;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class FileUploadProgressListener implements MediaHttpUploaderProgressListener {

    Logger log = LogManager.getLogger(FileUploadProgressListener.class);

    @Override
    public void progressChanged(MediaHttpUploader uploader) throws IOException {
        switch (uploader.getUploadState()) {
            case INITIATION_STARTED:
                log.info("Upload initiation started.");
                break;
            case INITIATION_COMPLETE:
                log.info("Upload initiation completed.");
                break;
            case MEDIA_IN_PROGRESS:
                log.info("Upload in progress: %.2f%%".formatted(uploader.getProgress() * 100));
                break;
            case MEDIA_COMPLETE:
                log.info("Upload complete!");
                break;
        }
    }

}
