package handler;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;

import java.io.IOException;

public class FileUploadProgressListener implements MediaHttpUploaderProgressListener {

    @Override
    public void progressChanged(MediaHttpUploader uploader) throws IOException {
        switch (uploader.getUploadState()) {
            case INITIATION_STARTED:
                System.out.println("Upload initiation started.");
                break;
            case INITIATION_COMPLETE:
                System.out.println("Upload initiation completed.");
                break;
            case MEDIA_IN_PROGRESS:
                System.out.printf("Upload in progress: %.2f%%\n", uploader.getProgress() * 100);
                break;
            case MEDIA_COMPLETE:
                System.out.println("Upload complete!");
                break;
        }
    }

}
