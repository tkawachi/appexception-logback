package com.github.tkawachi.appexception;

import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;

import java.util.ArrayList;
import java.util.List;

public class AppThrowableProxyConverter extends ThrowableHandlingConverter {

    protected static final int BUILDER_CAPACITY = 2048;

    List<String> printPrefixes = new ArrayList<String>();

    @SuppressWarnings("unchecked")
    public void start() {
        final List<String> optionList = getOptionList();

        if (optionList != null) {
            for (String anOptionList : optionList) {
                printPrefixes.add(anOptionList);
            }
        }

        super.start();
    }

    protected void extraData(StringBuilder builder, StackTraceElementProxy step) {
        // nop
    }

    public String convert(ILoggingEvent event) {

        IThrowableProxy tp = event.getThrowableProxy();
        if (tp == null) {
            return CoreConstants.EMPTY_STRING;
        }

        return throwableProxyToString(tp);
    }

    protected String throwableProxyToString(IThrowableProxy tp) {
        StringBuilder sb = new StringBuilder(BUILDER_CAPACITY);

        recursiveAppend(sb, null, ThrowableProxyUtil.REGULAR_EXCEPTION_INDENT, tp);

        return sb.toString();
    }

    private void recursiveAppend(StringBuilder sb, String prefix, int indent, IThrowableProxy tp) {
        if (tp == null)
            return;
        subjoinFirstLine(sb, prefix, indent, tp);
        sb.append(CoreConstants.LINE_SEPARATOR);
        subjoinSTEPArray(sb, indent, tp);
        IThrowableProxy[] suppressed = tp.getSuppressed();
        if (suppressed != null) {
            for (IThrowableProxy current : suppressed) {
                recursiveAppend(sb, CoreConstants.SUPPRESSED, indent + ThrowableProxyUtil.SUPPRESSED_EXCEPTION_INDENT, current);
            }
        }
        recursiveAppend(sb, CoreConstants.CAUSED_BY, indent, tp.getCause());
    }

    private void subjoinFirstLine(StringBuilder buf, String prefix, int indent, IThrowableProxy tp) {
        ThrowableProxyUtil.indent(buf, indent - 1);
        if (prefix != null) {
            buf.append(prefix);
        }
        subjoinExceptionMessage(buf, tp);
    }

    private void subjoinExceptionMessage(StringBuilder buf, IThrowableProxy tp) {
        buf.append(tp.getClassName()).append(": ").append(tp.getMessage());
    }

    protected void subjoinSTEPArray(StringBuilder buf, int indent, IThrowableProxy tp) {
        StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();

        int ignoredCount = 0;
        for (StackTraceElementProxy element : stepArray) {
            element.getStackTraceElement().getClassName();

            if (isAppLine(element)) {
                ThrowableProxyUtil.indent(buf, indent);
                printStackLine(buf, ignoredCount, element);
                ignoredCount = 0;
                buf.append(CoreConstants.LINE_SEPARATOR);
            } else {
                ++ignoredCount;
            }
        }
        if (ignoredCount > 0) {
            printIgnoredCount(buf, ignoredCount);
            buf.append(CoreConstants.LINE_SEPARATOR);
        }
    }

    private void printStackLine(StringBuilder buf, int ignoredCount, StackTraceElementProxy element) {
        buf.append(element);
        extraData(buf, element); // allow other data to be added
        if (ignoredCount > 0) {
            printIgnoredCount(buf, ignoredCount);
        }
    }

    private void printIgnoredCount(StringBuilder buf, int ignoredCount) {
        buf.append(" [").append(ignoredCount).append(" skipped]");
    }

    private boolean isAppLine(StackTraceElementProxy element) {
        String className = element.getStackTraceElement().getClassName();
        for (String prefix : printPrefixes) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

}
