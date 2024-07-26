package pupket.togedogserver.global.s3.util;

import org.springframework.web.multipart.MultipartFile;

public interface S3FileUtil {

    String upload(MultipartFile image);

    void deleteImageFromS3(String imageAddress);

    String getS3(String fileName);

}
