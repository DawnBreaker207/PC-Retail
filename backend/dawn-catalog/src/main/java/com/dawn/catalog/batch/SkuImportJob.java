package com.dawn.catalog.batch;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SkuImportJob {

    private static final SkuImportRow POISON_PILL = new SkuImportRow();

    @Bean
    @StepScope
    public ItemReader<SkuImportRow> skuImportReader(
            @Value("#{jobParameters['filePath']}") String filePath) {
        BlockingQueue<SkuImportRow> queue = new LinkedBlockingQueue<>(5000);

        Thread producer = Thread.ofVirtual().start(() -> {
            try (FileInputStream fis = new FileInputStream(filePath)) {
                EasyExcel.read(fis, SkuImportRow.class, new AnalysisEventListener<SkuImportRow>() {
                    @Override
                    public void invoke(SkuImportRow row, AnalysisContext ctx) {
                        try {
                            queue.put(row);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    @Override
                    public void doAfterAllAnalysed(AnalysisContext ctx) {
                        try {
                            queue.put(POISON_PILL);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }).sheet().doRead();
            } catch (Exception e) {
                log.error("EasyExcel read error", e);
                try {
                    queue.put(POISON_PILL);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        return () -> {
            SkuImportRow row = queue.take();
            return row == POISON_PILL ? null : row;
        };
    }

    @Bean
    public ItemProcessor<SkuImportRow, SkuImportRow> skuImportProcessor() {
        return row -> {
            if (row.getSkuCode() == null || row.getSkuCode().isBlank()) return null;
            if (row.getPrice() == null || row.getPrice().signum() <= 0) return null;
            return row;
        };
    }

    @Bean
    public ItemWriter<SkuImportRow> skuImportWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<SkuImportRow>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO sku (sku_code, name, description, price, slug, category_id, status, created_at, updated_at)
                        VALUES (:skuCode, :name, :description, :price,
                                lower(regexp_replace(:skuCode, '[^a-zA-Z0-9]', '-', 'g')),
                                (SELECT id FROM category WHERE LOWER(name) = LOWER(:categoryName) LIMIT 1),
                                'ACTIVE', now(), now())
                        ON CONFLICT (sku_code) DO UPDATE SET
                            name = EXCLUDED.name,
                            description = EXCLUDED.description,
                            price = EXCLUDED.price,
                            updated_at = now()
                        """)
                .beanMapped()
                .build();
    }

    @Bean
    public Step skuImportStep(JobRepository jobRepository,
                              PlatformTransactionManager txManager,
                              ItemReader<SkuImportRow> skuImportReader,
                              ItemProcessor<SkuImportRow, SkuImportRow> skuImportProcessor,
                              ItemWriter<SkuImportRow> skuImportWriter) {
        return new StepBuilder("skuImportStep", jobRepository)
                .<SkuImportRow, SkuImportRow>chunk(500, txManager)
                .reader(skuImportReader)
                .processor(skuImportProcessor)
                .writer(skuImportWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(1000)
                .build();
    }

    @Bean
    public Job skuImportBatchJob(JobRepository jobRepository, Step skuImportStep) {
        return new JobBuilder("skuImportJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(skuImportStep)
                .build();
    }
}
