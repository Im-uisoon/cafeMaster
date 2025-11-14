package com.project.summer.controller;

import com.project.summer.domain.dto.discard.DailyDiscardRecordResponseDto;
import com.project.summer.domain.dto.discard.DiscardItemResponseDto;
import com.project.summer.domain.dto.discard.ManualDiscardRequestDto;
import com.project.summer.domain.dto.discard.MonthlyDiscardSummaryResponseDto;
import com.project.summer.service.DiscardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DiscardController {

    private final DiscardService discardService;

    // (STORE) 수동 폐기
    @PostMapping("/store/discard")
    public ResponseEntity<Void> manualDiscard(
            @Valid @RequestBody ManualDiscardRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        discardService.manualDiscard(requestDto, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // (STORE/MASTER) 일일 폐기 내역 조회 - 20xx-xx-xx
    @GetMapping("/discard/daily")
    public ResponseEntity<DailyDiscardRecordResponseDto> getDailyDiscardRecords(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Authentication authentication) {
        String username = authentication.getName();
        String authority = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
        DailyDiscardRecordResponseDto response = discardService.getDailyDiscardRecords(date, username, authority);
        return ResponseEntity.ok(response);
    }

    // (STORE/MASTER) 월별 폐기 요약 조회 - 20xx-xx
    @GetMapping("/discard/monthly")
    public ResponseEntity<List<DiscardItemResponseDto>> getMonthlyDiscardSummary(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {
        String username = authentication.getName();
        String authority = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
        List<DiscardItemResponseDto> response = discardService.getMonthlyDiscardItems(year, month, username, authority);
        return ResponseEntity.ok(response);
    }
}
