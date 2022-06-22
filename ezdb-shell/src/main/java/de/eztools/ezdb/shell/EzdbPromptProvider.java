package de.eztools.ezdb.shell;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class EzdbPromptProvider implements PromptProvider {

    @Override
    public AttributedString getPrompt() {
        return new AttributedString("ezdb:> ", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }
}
