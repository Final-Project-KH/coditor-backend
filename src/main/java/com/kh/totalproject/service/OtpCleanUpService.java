package com.kh.totalproject.service;

import com.kh.totalproject.repository.EmailValidationForJoinRepository;
import com.kh.totalproject.repository.EmailValidationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@RequiredArgsConstructor
@Service
public class OtpCleanUpService {
    private final EmailValidationRepository emailValidationRepository;
    private final EmailValidationForJoinRepository emailValidationForJoinRepository;


    @Transactional
    @Scheduled(fixedRate = 300000)  // 5분마다 삭제
    public void deleteExpiredOtp() {
        emailValidationRepository.deleteExpiredOtp(new Date());
        emailValidationForJoinRepository.deleteExpiredOtp(new Date());
    }
}
