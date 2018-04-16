package lp.mail;

import com.google.cloud.storage.*;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;

// [START example]
public class CloudStorageHelper {

    private final static Storage storage = StorageOptions.getDefaultInstance().getService();
    private final static Bucket bucket = storage.create(BucketInfo.of("tlp-images"));
    //private final static String pathPrefix = "cam_snapshots/";

    // [START uploadFile]

    /**
     * Uploads a file to Google Cloud Storage to the bucket specified in the BUCKET_NAME
     * environment variable, appending a timestamp to end of the uploaded filename.
     */
    public String uploadFile(final String fileName, final InputStream in, final String contentType)
            throws IOException, ServletException {
        //checkFileExtension(fileName);

//        DateTimeFormatter dtf = DateTimeFormat.forPattern("-YYYY-MM-dd-HHmmssSSS");
//        DateTime dt = DateTime.now(DateTimeZone.UTC);
//        String dtString = dt.toString(dtf);
//        final String fileName = fileStream.getName() + dtString;

        //InputStream content = new ByteArrayInputStream("Hello, World!".getBytes(UTF_8));
        Blob blob = bucket.create(fileName, in, contentType);

        // the inputstream is closed by default, so we don't need to close it here
        BlobInfo blobInfo =
                storage.create(blob);
        // return the public download link
        return blobInfo.getMediaLink();
    }
    // [END uploadFile]

    // [START checkFileExtension]

    // [END checkFileExtension]
}
// [END example]