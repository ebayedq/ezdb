package de.eztools.ezdb.shell;

import org.apache.commons.csv.CSVFormat;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CsvFormatValueProvider implements ValueProvider {

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        return Arrays.stream(CSVFormat.Predefined.values())
                .map(CSVFormat.Predefined::name)
                .sorted()
                .map(CompletionProposal::new)
                .collect(Collectors.toList());
    }
}