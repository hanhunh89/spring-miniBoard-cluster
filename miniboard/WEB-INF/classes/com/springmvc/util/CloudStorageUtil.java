package com.springmvc.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class CloudStorageUtil {

  private static String projectId = "rosy-drake-400902";
  private static String bucketName = "miniboard-storage";
  

  // upload file to GCS
  public static void uploadFile(String filePath, String file_name, String credentialsPath) throws IOException {
    GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
            .createScoped("https://www.googleapis.com/auth/cloud-platform");
    // storage ��ü ����
    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).setCredentials(credentials).build().getService();
    // blobid ����
    BlobId blobId = BlobId.of(bucketName, file_name);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    //���� ���ε�
    storage.createFrom(blobInfo, Paths.get(filePath+file_name));
  }
}
