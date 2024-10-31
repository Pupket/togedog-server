package pupket.togedogserver.global.s3.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.S3Exception;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3FileUtilImpl implements S3FileUtil {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucket;

    @Override
    public String upload(MultipartFile image) {
        if (image.isEmpty() || Objects.isNull(image.getOriginalFilename())) {
            throw new S3Exception(ExceptionCode.FILE_IS_EMPTY);
        }
        return this.uploadImage(image);
    }

    private String uploadImage(MultipartFile image) {
        this.validateImageFiletExtention(Objects.requireNonNull(image.getOriginalFilename()));
        try {
            return this.uploadImageToS3(image);
        } catch (IOException e) {
            throw new S3Exception(ExceptionCode.IO_EXCEPTION_ON_IMAGE_UPLOAD);
        }
    }

    public String uploadImageToS3(MultipartFile image) throws IOException {
        String originalFileName = image.getOriginalFilename();
        String extension = Objects.requireNonNull(originalFileName).substring(originalFileName.lastIndexOf("."));

        String s3FileName = UUID.randomUUID().toString().substring(0, 10) + originalFileName;

        InputStream is = image.getInputStream();
        byte[] bytes = IOUtils.toByteArray(is);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/" + extension);
        metadata.setContentLength(bytes.length);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        try {
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucket, s3FileName, byteArrayInputStream, metadata)
                            .withCannedAcl((CannedAccessControlList.PublicRead));
            amazonS3.putObject(putObjectRequest);
        } catch (Exception e) {
            throw new S3Exception(ExceptionCode.PUT_OBJECT_EXCEPTION);
        } finally {
            byteArrayInputStream.close();
            is.close();
        }

        return amazonS3.getUrl(bucket, s3FileName).toString();
    }

    public String uploadImageToS3UsingByteImage(String image) throws IOException {
        String s3ImageUrl= "";
        try{
            String[] imageFile = image.split(",");
            String base64Image = imageFile[0];
            String extension;

            if(imageFile[0].equals("data:image/jpeg;base64,")){
                extension = "jpeg";
            }else if(imageFile[0].equals("data:image/png;base64,")){
                extension = "png";
            }else {
                extension = "jpg";
            }
            log.info("extension={}",extension);

            byte[] bindingImage = DatatypeConverter.parseBase64Binary(base64Image);

            File tempFile = File.createTempFile("image", "." + extension); // createTempFile을 통해 임시 파일을 생성해준다. (임시파일은 지워줘야함)
            log.info("tempFile={}",tempFile.getName());
            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                outputStream.write(bindingImage); //outputStream 객체에 imageBytes를 작성해준다.
            }

            String originalName = UUID.randomUUID().toString(); // uuid를 통해 파일명이 겹치지 않게 해준다

            amazonS3.putObject(new PutObjectRequest(bucket, originalName, tempFile).withCannedAcl(CannedAccessControlList.PublicRead)); // s3에 tempFile을 저장해준다.

             s3ImageUrl = amazonS3.getUrl(bucket, originalName).toString(); // s3에 저장된 이미지 불러오기

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(tempFile); // 파일 삭제시 전부 아웃풋 닫아줘야함 (방금 생성한 임시 파일을 지워주는 과정
                fileOutputStream.close(); // 아웃풋 닫아주기
                if (tempFile.delete()) {
                    log.info("File delete success"); // tempFile.delete()를 통해 삭제
                } else {
                    log.info("File delete fail");
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }

        }catch (IOException ex) {
            log.error("IOException Error Message : {}",ex.getMessage());
        }

        return s3ImageUrl;
    }

    private void validateImageFiletExtention(String fileName) {
        int dotPos = fileName.lastIndexOf(".");
        if (dotPos == -1) {
            throw new S3Exception(ExceptionCode.NO_FILE_EXTENTION);
        }
        String extention = fileName.substring(dotPos + 1).toLowerCase();
        List<String> allowedExtentionList = Arrays.asList("jpg", "jpeg", "png", "gif");

        if (!allowedExtentionList.contains(extention)) {
            throw new S3Exception(ExceptionCode.INVALID_FILE_EXTENTION);
        }
    }

    @Override
    public void deleteImageFromS3(String imageAddress) {
        String key = getKeyFromImageAddress(imageAddress);
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, key));
        } catch (Exception e) {
            throw new S3Exception(ExceptionCode.IO_EXCEPTION_ON_IMAGE_DELETE);
        }
    }

    private String getKeyFromImageAddress(String imageAddress) {
        try {
            URL url = new URL(imageAddress);
            String decodingKey = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);
            return decodingKey.substring(1);
        } catch (MalformedURLException e) {
            throw new S3Exception(ExceptionCode.IO_EXCEPTION_ON_IMAGE_DELETE);
        }
    }

    @Override
    public String getS3(String fileName) {

        return amazonS3.getUrl(bucket, fileName).toString();

    }

}
