package com.banksphere.controller;

import com.banksphere.dto.MoneyOperationRequest;
import com.banksphere.dto.TransactionDto;
import com.banksphere.dto.TransferRequest;
import com.banksphere.dto.TransferResponse;
import com.banksphere.model.TransactionType;
import com.banksphere.service.BankingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BankingController {
    private final BankingService bankingService;

    @PostMapping("/transactions/deposit")
    public TransactionDto deposit(@Valid @RequestBody MoneyOperationRequest request, Principal principal) {
        return bankingService.deposit(request, principal.getName());
    }

    @PostMapping("/transactions/withdraw")
    public TransactionDto withdraw(@Valid @RequestBody MoneyOperationRequest request, Principal principal) {
        return bankingService.withdraw(request, principal.getName());
    }

    @PostMapping("/transfers")
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request, Principal principal) {
        return bankingService.transfer(request, principal.getName());
    }

    @GetMapping("/transactions")
    public Page<TransactionDto> transactions(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable,
            Principal principal) {
        return bankingService.customerTransactions(principal.getName(), query, type, from, to, sorted(pageable));
    }

    @GetMapping("/statements.csv")
    public ResponseEntity<ByteArrayResource> statementCsv(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Principal principal) {
        byte[] csv = bankingService.statementCsv(principal.getName(), query, type, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=banksphere-statement.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new ByteArrayResource(csv));
    }

    @GetMapping("/statements.pdf")
    public ResponseEntity<ByteArrayResource> statementPdf(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Principal principal) {
        byte[] pdf = bankingService.statementPdf(principal.getName(), query, type, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=banksphere-statement.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new ByteArrayResource(pdf));
    }

    private Pageable sorted(Pageable pageable) {
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "occurredAt");
        int size = Math.min(pageable.getPageSize(), 50);
        return PageRequest.of(pageable.getPageNumber(), size, sort);
    }
}
