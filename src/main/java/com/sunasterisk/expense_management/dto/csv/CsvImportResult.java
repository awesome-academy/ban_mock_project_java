package com.sunasterisk.expense_management.dto.csv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvImportResult {

    private int totalRows;
    private int successCount;
    private int errorCount;

    @Builder.Default
    private List<ImportError> errors = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportError {
        private int lineNumber;
        private String errorMessage;
        private String lineContent;
    }

    public void addError(int lineNumber, String errorMessage, String lineContent) {
        errors.add(ImportError.builder()
                .lineNumber(lineNumber)
                .errorMessage(errorMessage)
                .lineContent(lineContent)
                .build());
        errorCount++;
    }

    public boolean hasErrors() {
        return errorCount > 0;
    }
}
