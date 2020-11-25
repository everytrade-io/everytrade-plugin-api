package io.everytrade.server.plugin.api.parser;

import java.util.List;

import static io.everytrade.server.plugin.api.parser.ParserUtils.occurrenceCount;
import static io.everytrade.server.plugin.api.parser.ParserUtils.split;
import static io.everytrade.server.plugin.api.parser.ParserUtils.splitInTwo;

public class DateTimeFormatFinder {

    public String findFormatPattern(String dateTime) {
        final List<String> splitT = split(dateTime, "T", false);
        if (splitT.size() == 2) {
            return new StringBuilder()
                .append(findDateFormat(splitT.get(0)))
                .append("'T'")
                .append(findTimeFormat(splitT.get(1)))
                .toString();
        }
        final List<String> splitSpaceThird = splitInTwo(dateTime, " ", 3);
        if (splitSpaceThird.size() == 2) {
            return new StringBuilder()
                .append(findDateFormat(splitSpaceThird.get(0)))
                .append(" ")
                .append(findTimeFormat(splitSpaceThird.get(1)))
                .toString();
        }
        final List<String> splitSpace = splitInTwo(dateTime, " ", 1);
        if (splitSpace.size() == 2) {
            return new StringBuilder()
                .append(findDateFormat(splitSpace.get(0)))
                .append(" ")
                .append(findTimeFormat(splitSpace.get(1)))
                .toString();
        }

        return "";
    }

    private String findDateFormat(String date) {
        final String dashSplit = getDashSplit(date);
        if (dashSplit != null) {
            return dashSplit;
        }
        String dotSplit = getDotSplit(date);
        if (dotSplit != null) {
            return dotSplit;
        }

        String slashSplit = getSlashSplit(date);
        if (slashSplit != null) {
            return slashSplit;
        }

        String spaceSplit = getSpaceSplit(date);
        if (spaceSplit != null){
            return spaceSplit;
        }

        return "";
    }

    private String getSpaceSplit(String date) {
        final List<String> splitSpace = split(date, " ", false);
        if (splitSpace.size() == 3) {
            if (splitSpace.get(2).length() == 5) {
                return "MMM. d, yyyy,";
            }

            if (splitSpace.get(2).length() == 3) {
                return "MMM. d, yy,";
            }
        }
        return null;
    }

    private String getSlashSplit(String date) {
        final List<String> splitSlash = split(date, "/", false);
        if (splitSlash.size() == 3) {
            if (splitSlash.get(2).length() == 2) {
                return "M/d/yy";
            }
            if (splitSlash.get(2).length() == 4) {
                return "M/d/yyyy";
            }
        }
        return null;
    }

    private String getDotSplit(String date) {
        final List<String> splitDot = split(date, ".", false);
        if (splitDot.size() == 3) {
            if (splitDot.get(2).length() == 4) {
                return "d.M.yyyy";
            }
            if (splitDot.get(2).length() == 2) {
                return "d.M.yy";
            }
        }
        return null;
    }

    private String getDashSplit(String date) {
        final List<String> splitDash = split(date, "-", false);
        if (splitDash.size() == 3) {
            if (splitDash.get(0).length() == 4) {
                return "yyyy-M-d";
            }
            if (splitDash.get(0).length() == 2) {
                return "yy-M-d";
            }
        }
        return null;
    }

    private String findTimeFormat(String time) {
        final List<String> splitColon = split(time, ":", false);
        if (splitColon.size() == 2) {
            final int spaceOccurrence = occurrenceCount(splitColon.get(1), " ");
            if (spaceOccurrence == 1) {
                return "h:m a";
            }
            if (spaceOccurrence == 0) {
                return "H:m";
            }
        }
        if (splitColon.size() == 3) {
            String timeFormat = getTimeFormat(splitColon);
            if (timeFormat != null) {
                return timeFormat;
            }
        }
        return "";
    }

    private String getTimeFormat(List<String> splitColon) {
        if (occurrenceCount(splitColon.get(2), " ") == 1) {
            return "h:m:s a";
        }
        final List<String> splitDot = split(splitColon.get(2), ".", false);
        if (splitDot.size() == 1) {
            return "H:m:s";
        }
        if (splitDot.size() == 2) {
            if (splitDot.get(1).length() == 1) {
                return "H:m:s.S";
            }
            if (splitDot.get(1).length() == 2) {
                return "H:m:s.SS";
            }
            if (splitDot.get(1).length() == 4) {
                return "H:m:s.SSSS";
            }
        }
        return null;
    }
}
