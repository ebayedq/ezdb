package de.eztools.ezdb.shell;

import org.apache.commons.csv.CSVFormat;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProviderSupport;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CsvFormatValueProvider extends ValueProviderSupport {

    @Override
    public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext, String[] hints) {
        return Arrays.stream(CSVFormat.Predefined.values())
                .map(CSVFormat.Predefined::name)
                .sorted()
                .map(CompletionProposal::new)
                .collect(Collectors.toList());
    }
}