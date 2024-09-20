package ru.beeline.capability.utils;

import java.util.Optional;

public class UrlWrapper {
    public static String proxyUrl(String description) {
        if (description == null) {
            return "";
        }
        return wrapA(wrapFont(wrapUrl(description)));
    }

    private static String wrapUrl(String description) {
        boolean tegA = false;
        boolean tegFont = false;
        int pos = -1;
        while (pos < description.length() - 1) {
            pos++;
            char currentChar = description.charAt(pos);
            if (tegA) {
                if (pos + 3 < description.length() && !description.startsWith("<a", pos)) {
                    continue;
                }
                if (pos + 3 < description.length() && description.startsWith("/a>", pos)) {
                    tegA = false;
                }
            }
            if (pos + 3 < description.length() && description.startsWith("<a", pos)) {
                tegA = true;
            }
            if (tegFont) {
                if (pos + 5 < description.length() && !description.startsWith("/font", pos)) {
                    continue;
                }
                if (pos + 5 < description.length() && description.startsWith("/font", pos)) {
                    tegFont = false;
                }
            }
            if (pos + 6 < description.length() && description.startsWith("<font", pos)) {
                tegFont = true;
                continue;
            }
            if (currentChar == 'h' && pos + 7 < description.length()) {
                String subString = description.substring(pos, pos + 8);
                if (subString.equals("https://")) {
                    int startIndexUrl = pos;
                    int endIndexUrl = pos + 8;
                    while (endIndexUrl < description.length()
                            && description.charAt(endIndexUrl) != ' '
                            && description.charAt(endIndexUrl) != '\"'
                            && description.charAt(endIndexUrl) != '<') {
                        endIndexUrl++;
                    }
                    String fullUrl = description.substring(startIndexUrl, endIndexUrl);
                    description = description.replace(fullUrl, reduceUrlToTemplate(fullUrl, fullUrl));
                    pos = description.indexOf(reduceUrlToTemplate(fullUrl, fullUrl)) + reduceUrlToTemplate(fullUrl, fullUrl).length();
                    System.out.print("");
                }
            }
        }
        return description;
    }

    private static String wrapFont(String description) {
        boolean tegA = false;
        int pos = -1;
        while (pos < description.length() - 1) {
            pos++;
            char currentChar = description.charAt(pos);
            if (tegA) {
                if (pos + 3 < description.length() && !description.startsWith("<a", pos)) {
                    continue;
                }
                if (pos + 3 < description.length() && description.startsWith("/a>", pos)) {
                    tegA = false;
                }
            }
            if (pos + 3 < description.length() && description.startsWith("<a", pos)) {
                tegA = true;
                continue;
            }
            if (currentChar == 'h' && pos + 7 < description.length()) {
                String subString = description.substring(pos, pos + 8);
                if (subString.equals("https://")) {
                    int startIndexUrl = pos;
                    int endIndexUrl = pos + 8;
                    while (endIndexUrl < description.length()
                            && description.charAt(endIndexUrl) != ' '
                            && description.charAt(endIndexUrl) != '\"'
                            && description.charAt(endIndexUrl) != '<') {
                        endIndexUrl++;
                    }
                    String fullUrl = description.substring(startIndexUrl, endIndexUrl);
                    String urlWithTags = getWithTags(description, startIndexUrl, endIndexUrl);
                    description = description.replace(urlWithTags, reduceUrlToTemplate(fullUrl, fullUrl));
                    pos = description.indexOf(reduceUrlToTemplate(fullUrl, fullUrl)) + reduceUrlToTemplate(fullUrl, fullUrl).length();
                    System.out.print("");
                }
            }

        }
        return description;
    }

    private static String wrapA(String description) {
        int startIndexTag = 0;
        int endIndexTag = 0;
        boolean tagA = false;
        int pos = -1;
        while (pos < description.length() - 1) {
            pos++;
            char currentChar = description.charAt(pos);

            if (tagA) {
                if (pos + 3 < description.length() && description.startsWith("/a>", pos)) {
                    tagA = false;
                    endIndexTag = pos + 3;
                } else {
                    continue;
                }
            } else {
                if (pos + 3 < description.length() && !description.startsWith("<a", pos)) {
                    startIndexTag = pos;
                    tagA = true;
                    continue;
                } else {
                    continue;
                }
            }
            if (description.substring(startIndexTag, endIndexTag).contains("<font")) {
                continue;
            }
            int pos2 = -1;
            String cutDescription = description.substring(startIndexTag, endIndexTag);
            while (pos2 < endIndexTag - 1) {
                pos2++;
                if (cutDescription.charAt(pos2) == 'h' && pos2 + 7 < cutDescription.length()) {
                    String subString = cutDescription.substring(pos2, pos2 + 8);
                    if (subString.equals("https://")) {
                        int startIndexUrl = pos2;
                        int endIndexUrl = pos2 + 8;
                        while (endIndexUrl < cutDescription.length()
                                && cutDescription.charAt(endIndexUrl) != ' '
                                && cutDescription.charAt(endIndexUrl) != '\"'
                                && cutDescription.charAt(endIndexUrl) != '<') {
                            endIndexUrl++;
                        }
                        String fullUrl = cutDescription.substring(startIndexUrl, endIndexUrl);
                        String findLink = findLink(cutDescription, fullUrl);
                        description = description.replace(cutDescription, reduceUrlToTemplate(fullUrl, findLink));
                        pos = description.indexOf(reduceUrlToTemplate(fullUrl, findLink)) + reduceUrlToTemplate(fullUrl, findLink).length();
                        System.out.print("");
                        break;
                    }
                }
            }
        }
        return description;
    }

    private static String findLink(String urlWithTag, String fullUrl) {
        Integer endIndex = findFinishIndex(urlWithTag, 1, "</a>").orElse(null);
        if (endIndex != null) {
            int pos = endIndex - 2;
            while (pos >= 0) {
                pos--;
                if (urlWithTag.charAt(pos) == '<') {
                    endIndex = pos;
                    break;
                }
            }
            pos = endIndex - 3;
            while (pos > 0) {
                pos--;
                if (urlWithTag.charAt(pos) == '>') {
                    return urlWithTag.substring(pos + 1, endIndex);
                }
            }
        }
        return fullUrl;
    }

    private static String getWithTags(String description, Integer startIndexUrl, Integer endIndexUrl) {
        if (endIndexUrl < description.length()) {
            endIndexUrl = getFinishIndex(description, endIndexUrl);
            startIndexUrl = getStartIndex(description, startIndexUrl);
        }
        return description.substring(startIndexUrl, endIndexUrl);
    }

    private static Integer getFinishIndex(String description, Integer endIndexUrl) {
        return findFinishIndex(description, endIndexUrl, "/font>")
                .orElse(endIndexUrl);
    }

    private static Integer getStartIndex(String description, Integer startIndexUrl) {
        return findStartIndex(description, startIndexUrl, "<font")
                .orElse(startIndexUrl);
    }

    private static Optional<Integer> findFinishIndex(String description, Integer endIndexUrl, String tag) {
        int pos = endIndexUrl;
        while (pos < description.length() - tag.length() + 1) {
            String subString = description.substring(pos, pos + tag.length());
            if (subString.equals(tag)) {
                return Optional.of(pos + tag.length());
            }
            pos++;
        }
        return Optional.empty();
    }

    private static Optional<Integer> findStartIndex(String description, Integer startIndexUrl, String tag) {
        int pos = startIndexUrl;
        while (pos >= 0) {
            char currentChar = description.charAt(pos);
            if (currentChar == tag.charAt(0)) {
                String subString = description.substring(pos, pos + tag.length());
                if (subString.equals(tag)) {
                    return Optional.of(pos);
                }
            }
            pos--;
        }
        return Optional.empty();
    }

    private static String reduceUrlToTemplate(String fullUrl, String link) {
        return String.format("<a href=\"%s\"><font color=\"#0000ff\">%s</font></a>", fullUrl, link);
    }
}
