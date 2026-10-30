package com.dawn.catalog.controller;

import com.dawn.catalog.batch.SkuImportJob;
import com.dawn.common.response.ResponseObject;
import com.dawn.common.security.AuthorizationConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;

@Slf4j
@RestController
@RequestMapping("/catalog/import")
@RequiredArgsConstructor
public class ImportController {

    private final JobLauncher jobLauncher;
    private final Job skuImportBatchJob;

    @PostMapping("/sku")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<String> importSkus(@RequestParam("file") MultipartFile file) throws Exception {
        File temp = Files.createTempFile("sku-import-", ".xlsx").toFile();
        file.transferTo(temp);

        JobParameters params = new JobParametersBuilder()
                .addString("filePath", temp.getAbsolutePath())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(skuImportBatchJob, params);
        return ResponseObject.success("Import started for file: " + file.getOriginalFilename());
    }
}
