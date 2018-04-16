package lp.mail;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.InputStream;

public class CloudStorageHelper {

    private final static Storage storage = StorageOptions.getDefaultInstance().getService();
    private final static Bucket bucket = storage.get("tlp-images");

    /**
     * Uploads a file to Google Cloud Storage to the bucket specified in the defined bucket
     */
    public String uploadFile(final String fileName, final InputStream in, final String contentType) {
        Blob blob = bucket.create(fileName, in, contentType);
        return blob.getMediaLink();
    }
}
