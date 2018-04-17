package lp.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

public class MailHandlerServlet extends HttpServlet {

    private final static Logger logger = LoggerFactory.getLogger(MailHandlerServlet.class);
    private final static Properties props = new Properties();
    private final static String[] allowedExt = {".jpg", ".jpeg", ".png"};
    private final static CloudStorageHelper storage = new CloudStorageHelper();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long receivedTime = System.currentTimeMillis();
        Session session = Session.getDefaultInstance(props, null);
        try {
            MimeMessage message = new MimeMessage(session, req.getInputStream());
            String from = message.getFrom()[0].toString();
            String subject = message.getSubject();
            long latency = receivedTime - message.getSentDate().getTime();
            String sentDate = message.getSentDate().toString();
            String contentType = message.getContentType();
            String messageContent = "";
            // store attachment file name, separated by comma
            String attachFiles = "";
            int numberOfParts = 0;

            if (contentType.contains("multipart")) {
                // content may contain attachments
                Multipart multiPart = (Multipart) message.getContent();
                numberOfParts = multiPart.getCount();
                for (int partCount = 0;
                     partCount < numberOfParts;
                     partCount++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        // this part is attachment
                        String fileName = part.getFileName();
                        String fileContentType = part.getContentType().split(";")[0];
                        checkFileExtension(fileName);
                        attachFiles += fileName + ", ";
                        logger.info(String.format("uploading file=%s contentType=%s size=%d", fileName, fileContentType, part.getSize()));
                        storage.uploadFile(fileName.substring(0,8)+"/"+fileName, part.getInputStream(), "image/jpeg");
                    } else {
                        // this part may be the message content
                        messageContent = part.getContent().toString();
                    }
                }

                if (attachFiles.length() > 1) {
                    attachFiles = attachFiles.substring(0, attachFiles.length() - 2);
                }
            } else if (contentType.contains("text/plain") || contentType.contains("text/html")) {
                Object content = message.getContent();
                if (content != null) {
                    messageContent = content.toString();
                }
            }

            logger.info(String.format("Received mail from: %s subject: %s latency: %d  duration: %d contentType: %s numberOfParts: %d attachFiles: %s messageContent: %s", from, subject, latency, System.currentTimeMillis() - receivedTime, contentType, numberOfParts, attachFiles, messageContent));
        } catch (MessagingException e) {
            logger.error("Received mail message but error occurred.", e);
            // ...
        } catch (Exception e) {
            logger.error("unexpected error", e);
        }
    }

    /**
     * Checks that the file extension is supported.
     */
    private void checkFileExtension(String fileName) {
        if (fileName != null && !fileName.isEmpty() && fileName.contains(".")) {
            for (String ext : allowedExt) {
                if (fileName.endsWith(ext)) {
                    return;
                }
            }
            throw new IllegalArgumentException("file must be an image");
        }
    }

}