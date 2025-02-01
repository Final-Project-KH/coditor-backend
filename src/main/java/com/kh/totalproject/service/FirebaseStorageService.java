package com.kh.totalproject.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import com.kh.totalproject.entity.User;
import com.kh.totalproject.repository.UserRepository;
import com.kh.totalproject.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FirebaseStorageService {

    private final Storage storage = StorageClient.getInstance().bucket().getStorage();
    private final String bucketName = StorageClient.getInstance().bucket().getName();
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public String uploadFile(String authorizationHeader, MultipartFile file, String fileName) throws IOException {
        String token = authorizationHeader.replace("Bearer ", ""); // Bearer 제거
        jwtUtil.getAuthentication(token); // 인증 정보 생성
        Long id = jwtUtil.extractUserId(token);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
        String nickname = user.getNickname();
        String profileFolderName = "upload/profile";
        String objectName = profileFolderName + "/" + nickname + "/" + UUID.randomUUID() + id + "-" + fileName;
        String mimeType = file.getContentType();

        if (user.getProfileUrl() != null){
            String existedProfileUrl = user.getProfileUrl();
            String extractedPath = existedProfileUrl.split("o/")[1];
            String existedObjectName = extractedPath.replace("%2F", "/").replace("?alt=media", "");
            BlobId blobId = BlobId.of(bucketName, existedObjectName);
            storage.delete(blobId);
        }

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(mimeType)
                .build();
        storage.create(blobInfo, file.getBytes());

        String encodedPath = objectName.replace("/", "%2F");
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/" + encodedPath + "?alt=media";

        user.setProfileUrl(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }
    public Boolean deleteFile(String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", ""); // Bearer 제거
        jwtUtil.getAuthentication(token); // 인증 정보 생성
        Long id = jwtUtil.extractUserId(token);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
        String profileUrl = user.getProfileUrl();
        if (profileUrl != null && !profileUrl.isEmpty()) {
            String extractedPath = profileUrl.split("o/")[1];
            String objectName = extractedPath.replace("%2F", "/").replace("?alt=media", "");
            log.info(objectName);
            BlobId blobId = BlobId.of(bucketName, objectName);
            storage.delete(blobId);
            System.out.println("기존 파일 삭제 완료 : " + objectName);
        }
        user.setProfileUrl(null);
        userRepository.save(user);

        return true;
    }
}
