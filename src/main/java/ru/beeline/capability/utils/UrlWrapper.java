package ru.beeline.capability.utils;

import java.util.Optional;

public class UrlWrapper {

    public static String proxyUrl(String description) {
        if (description == null) {
            return "";
        }
        description = description.replaceAll("\\\\\"", "\"");
        return wrapTagA(wrapTagFont(wrapCleanUrl(description)));
    }

    private static String wrapCleanUrl(String description) {
        boolean tegA = false;
        boolean tegFont = false;
        int pos = -1;
        while (pos < description.length() - 1) {
            pos++;
            char currentChar = description.charAt(pos);
            if (tegA) {
                if (pos + 2 < description.length() && !description.startsWith("/a>", pos)) {
                    continue;
                }
                if (pos + 2 < description.length() && description.startsWith("/a>", pos)) {
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
                    description = replaceRange(description, startIndexUrl, endIndexUrl, reduceUrlToTemplate(fullUrl, fullUrl), false);
                    pos = description.indexOf(reduceUrlToTemplate(fullUrl, fullUrl)) + reduceUrlToTemplate(fullUrl, fullUrl).length();
                    System.out.print("");
                }
            }
        }
        return description;
    }

    private static String wrapTagFont(String description) {
        boolean tegA = false;
        int pos = -1;
        while (pos < description.length() - 1) {
            pos++;
            char currentChar = description.charAt(pos);
            if (tegA) {
                if (pos + 2 < description.length() && !description.startsWith("/a>", pos)) {
                    continue;
                }
                if (pos + 2 < description.length() && description.startsWith("/a>", pos)) {
                    tegA = false;
                }
            }
            if (pos + 1 < description.length() && description.startsWith("<a", pos)) {
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
                    description = replaceRange(description, startIndexUrl, endIndexUrl, reduceUrlToTemplate(fullUrl, fullUrl), true);
                    pos = description.indexOf(reduceUrlToTemplate(fullUrl, fullUrl)) + reduceUrlToTemplate(fullUrl, fullUrl).length();
                    System.out.print("");
                }
            }

        }
        return description;
    }

    private static String wrapTagA(String description) {
        int startIndexTag = 0;
        int endIndexTag = 0;
        boolean tagA = false;
        int pos = -1;
        while (pos < description.length() - 1) {
            pos++;

            if (tagA) {
                if (pos + 2 < description.length() && description.startsWith("/a>", pos)) {
                    tagA = false;
                    endIndexTag = pos + 3;
                } else {
                    continue;
                }
            } else {
                if (pos + 3 < description.length() && description.startsWith("<a", pos)) {
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
            String urlWithTags = description.substring(startIndexTag, endIndexTag);
            while (pos2 < endIndexTag - 1) {
                pos2++;
                if (urlWithTags.charAt(pos2) == 'h' && pos2 + 7 < urlWithTags.length()) {
                    String subString = urlWithTags.substring(pos2, pos2 + 8);
                    if (subString.equals("https://")) {
                        int startIndexUrl = pos2;
                        int endIndexUrl = pos2 + 8;
                        while (endIndexUrl < urlWithTags.length()
                                && urlWithTags.charAt(endIndexUrl) != ' '
                                && urlWithTags.charAt(endIndexUrl) != '\"'
                                && urlWithTags.charAt(endIndexUrl) != '<') {
                            endIndexUrl++;
                        }
                        String httpUrl = urlWithTags.substring(startIndexUrl, endIndexUrl);
                        String findLink = findLink(urlWithTags, httpUrl);
                        description = replaceRange(description, startIndexTag, endIndexTag, reduceUrlToTemplate(httpUrl, findLink), false);
                        pos = description.indexOf(reduceUrlToTemplate(httpUrl, findLink)) + reduceUrlToTemplate(httpUrl, findLink).length();
                        break;
                    }
                }
            }
        }
        return description;
    }
    static String replaceRange(String description, int startIndex, int endIndex, String replacement, boolean urlWithTags) {
        if(urlWithTags){
            if (endIndex < description.length()) {
                endIndex = getFinishIndex(description, endIndex);
                startIndex = getStartIndex(description, startIndex);
            }
        }
        return description.substring(0, startIndex) + replacement + description.substring(endIndex);
    }
    private static String findLink(String urlWithTag, String httpUrl) {
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
        return httpUrl;
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
