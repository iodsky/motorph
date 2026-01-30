package com.iodsky.sweldox.payroll.contribution.pagIbig;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagibigContributionService {

    private final PagibigContributionRepository pagibigContributionRepository;

    @Transactional
    public PagibigContribution createPagibigContribution(PagibigContributionRequest request) {
        if (pagibigContributionRepository.findLatestByEffectiveDate(request.getEffectiveDate()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Pag-IBIG contribution configuration already exists for effective date: " + request.getEffectiveDate()
            );
        }

        PagibigContribution contribution = PagibigContribution.builder()
                .employeeRate(request.getEmployeeRate())
                .employerRate(request.getEmployerRate())
                .lowIncomeThreshold(request.getLowIncomeThreshold())
                .lowIncomeEmployeeRate(request.getLowIncomeEmployeeRate())
                .maxSalaryCap(request.getMaxSalaryCap())
                .effectiveDate(request.getEffectiveDate())
                .build();

        return pagibigContributionRepository.save(contribution);
    }

    public Page<PagibigContribution> getAllPagibigContributions(int page, int limit, LocalDate effectiveDate) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "effectiveDate"));

        if (effectiveDate != null) {
            return pagibigContributionRepository.findAll(
                    (root, query, cb) -> cb.and(
                            cb.lessThanOrEqualTo(root.get("effectiveDate"), effectiveDate),
                            cb.isNull(root.get("deletedAt"))
                    ),
                    pageable
            );
        }

        return pagibigContributionRepository.findAll(
                (root, query, cb) -> cb.isNull(root.get("deletedAt")),
                pageable
        );
    }

    public PagibigContribution getPagibigContributionById(UUID id) {
        return pagibigContributionRepository.findById(id)
                .filter(config -> config.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pag-IBIG contribution configuration not found with ID: " + id
                ));
    }

    public PagibigContribution getLatestPagibigContribution(LocalDate date) {
        return pagibigContributionRepository.findLatestByEffectiveDate(date)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No Pag-IBIG contribution configuration found for date: " + date
                ));
    }

    @Transactional
    public PagibigContribution updatePagibigContribution(UUID id, PagibigContributionRequest request) {
        PagibigContribution contribution = getPagibigContributionById(id);

        contribution.setEmployeeRate(request.getEmployeeRate());
        contribution.setEmployerRate(request.getEmployerRate());
        contribution.setLowIncomeThreshold(request.getLowIncomeThreshold());
        contribution.setLowIncomeEmployeeRate(request.getLowIncomeEmployeeRate());
        contribution.setMaxSalaryCap(request.getMaxSalaryCap());
        contribution.setEffectiveDate(request.getEffectiveDate());

        return pagibigContributionRepository.save(contribution);
    }

    @Transactional
    public void deletePagibigContribution(UUID id) {
        PagibigContribution contribution = getPagibigContributionById(id);
        contribution.setDeletedAt(Instant.now());
        pagibigContributionRepository.save(contribution);
    }
}
