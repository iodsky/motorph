package com.iodsky.sweldox.payroll.contribution.sss;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SssContributionService {

    private final SssContributionRepository sssContributionRepository;

    @Transactional
    public SssContribution createSssContribution(SssContributionRequest request) {
        List<SssContribution.SalaryBracket> brackets = request.getSalaryBrackets().stream()
                .map(req -> SssContribution.SalaryBracket.builder()
                        .minSalary(req.getMinSalary())
                        .maxSalary(req.getMaxSalary())
                        .msc(req.getMsc())
                        .build())
                .collect(Collectors.toList());

        SssContribution contribution = SssContribution.builder()
                .totalSss(request.getTotalSss())
                .employeeRate(request.getEmployeeRate())
                .employerRate(request.getEmployerRate())
                .salaryBrackets(brackets)
                .effectiveDate(request.getEffectiveDate())
                .build();

        return sssContributionRepository.save(contribution);
    }

    public Page<SssContribution> getAllSssContributions(
            int page, int limit, LocalDate effectiveDate) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "effectiveDate"));

        return sssContributionRepository.findAll((root, query, cb) -> {
            var predicates = cb.conjunction();

            predicates = cb.and(predicates, cb.isNull(root.get("deletedAt")));

            if (effectiveDate != null) {
                predicates = cb.and(predicates, cb.equal(root.get("effectiveDate"), effectiveDate));
            }

            return predicates;
        }, pageable);
    }

    public SssContribution getSssContributionById(UUID id) {
        return sssContributionRepository.findById(id)
                .filter(config -> config.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "SSS contribution configuration not found with ID: " + id
                ));
    }

    public SssContribution getSssContributionBySalaryAndDate(BigDecimal salary, LocalDate date) {
        SssContribution config = sssContributionRepository.findLatestByEffectiveDate(date)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No SSS contribution configuration found for date: " + date
                ));

        // Verify the salary falls within one of the brackets
        try {
            config.findBracket(salary);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No SSS contribution bracket found for salary: " + salary + " on date: " + date
            );
        }

        return config;
    }

    @Transactional
    public SssContribution updateSssContribution(UUID id, SssContributionRequest request) {
        SssContribution contribution = getSssContributionById(id);

        List<SssContribution.SalaryBracket> brackets = request.getSalaryBrackets().stream()
                .map(req -> SssContribution.SalaryBracket.builder()
                        .minSalary(req.getMinSalary())
                        .maxSalary(req.getMaxSalary())
                        .msc(req.getMsc())
                        .build())
                .collect(Collectors.toList());

        contribution.setTotalSss(request.getTotalSss());
        contribution.setEmployeeRate(request.getEmployeeRate());
        contribution.setEmployerRate(request.getEmployerRate());
        contribution.setSalaryBrackets(brackets);
        contribution.setEffectiveDate(request.getEffectiveDate());

        return sssContributionRepository.save(contribution);
    }

    @Transactional
    public void deleteSssContribution(UUID id) {
        SssContribution contribution = getSssContributionById(id);
        contribution.setDeletedAt(Instant.now());
        sssContributionRepository.save(contribution);
    }
}
