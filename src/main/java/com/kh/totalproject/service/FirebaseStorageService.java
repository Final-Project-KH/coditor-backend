package com.kh.totalproject.service;

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
        String objectName = profileFolderName + "/" +nickname + "/" + UUID.randomUUID() + "-" + id + "-" + fileName;
        String mimeType = file.getContentType();

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(mimeType)
                .build();
        storage.create(blobInfo, file.getBytes());

        String encodedPath = objectName.replace("/", "%2F");
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/" +bucketName+"/o/" + encodedPath + "?alt=media";

        user.setProfileUrl(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }
}
