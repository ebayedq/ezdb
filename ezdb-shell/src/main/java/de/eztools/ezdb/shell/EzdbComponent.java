package de.eztools.ezdb.shell;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.standard.AbstractShellComponent;

import java.time.Duration;

public class EzdbComponent extends AbstractShellComponent {

    private static final String CUU = "\u001B[A";
    private static final String DL = "\u001B[1M";

    private boolean started = false;
    private long startTimeMillis;

    private void display(int percentage, String statusMessage) {
        if (!started) {
            started = true;
            getTerminal().writer().println();
        }

        int x = (percentage / 5);
        int y = 20 - x;
        String message = ((statusMessage == null) ? "" : statusMessage);

        String doneMarker = "=";
        String done = new AttributedString(new String(new char[x]).replace("\0", doneMarker), AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN)).toAnsi();
        String remainsMarker = "-";
        String remains = new String(new char[y]).replace("\0", remainsMarker);

        String rightDelimiter = ">";
        String leftDelimiter = "<";
        String progressBar = String.format("%s%s%s%s %d", leftDelimiter, done, remains, rightDelimiter, percentage);

        getTerminal().writer().println(CUU + "\r" + DL + progressBar + "% " + message);
        getTerminal().flush();
    }

    protected void showProgress(int current, int total) {
        if (current == 0) {
            startTimeMillis = System.currentTimeMillis();
        }
        int percentage = (int) ((current / (double) total) * 100);
        String remainingText = getRemaining(startTimeMillis, percentage);
        display(percentage, remainingText);
    }

    protected void showProgress(int percentage) {
        showProgress(percentage, 100);
    }

    protected void showProgressDone() {
        showProgress(100);
        started = false;
        startTimeMillis = 0;
    }

    protected String prompt(String prompt, boolean mask) {
        StringInput component = new StringInput(getTerminal(), prompt, null);
        component.setResourceLoader(getResourceLoader());
        component.setTemplateExecutor(getTemplateExecutor());
        if (mask) {
            component.setMaskCharater('*');
        }
        StringInput.StringInputContext context = component.run(StringInput.StringInputContext.empty());
        return context.getResultValue();
    }

    private String getRemaining(long startTimeMillis, int percentage) {
        if (percentage == 0 || startTimeMillis == 0) {
            return "";
        }

        long currentTimeMillis = System.currentTimeMillis();
        long durationMillis = currentTimeMillis - startTimeMillis;
        long remainingMillis = Math.round(durationMillis * (100 - percentage) / (double) percentage);
        long totalMillis = Math.round(durationMillis * (100) / (double) percentage);

        String remainingText = remainingMillis == 0 ? "" : formatDuration(remainingMillis) + " remaining, ";
        String totalText = formatDuration(totalMillis) + " total";
        return remainingText + totalText;
    }

    private String formatDuration(long millis) {
        return Duration.ofMillis(millis).toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .replaceAll("\\.\\d+", "")
                .toLowerCase();
    }

    protected void printError(String message) {
        print(message, AttributedStyle.RED);
    }

    protected void printWarning(String message) {
        print(message, AttributedStyle.YELLOW);
    }

    protected void printInfo(String message) {
        print(message, AttributedStyle.CYAN);
    }

    protected void print(String message) {
        AttributedString attributedString = new AttributedString(message);
        getTerminal().writer().println(attributedString.toAnsi());

    }

    private void print(String message, int style) {
        AttributedString attributedString = new AttributedString(message, AttributedStyle.DEFAULT.foreground(style));
        getTerminal().writer().println(attributedString.toAnsi());
    }
}
