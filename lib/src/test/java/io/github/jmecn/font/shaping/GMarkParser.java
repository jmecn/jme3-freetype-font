package io.github.jmecn.font.shaping;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GMarkTag {
    private String name;
    private String attributes;
    private String content;

    public GMarkTag(String name, String attributes, String content) {
        this.name = name;
        this.attributes = attributes;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getAttributes() {
        return attributes;
    }

    public String getContent() {
        return content;
    }
}

public class GMarkParser {
    private static final Pattern tagPattern = Pattern.compile("<(\\w+)(.*?)>(.*?)</\\1>");

    public static List<GMarkTag> extractTags(String gmarkText) {
        List<GMarkTag> tags = new ArrayList<>();
        Matcher matcher = tagPattern.matcher(gmarkText);
        while (matcher.find()) {
            String tagName = matcher.group(1);
            String attributes = matcher.group(2);
            String content = matcher.group(3);
            GMarkTag tag = new GMarkTag(tagName, attributes, content);
            tags.add(tag);
        }
        return tags;
    }

    public static void main(String[] args) {
        String gmarkText = "这是一个美丽的新世界。<i><b>Hello world</b></i>我希望大家<font>永远开心</font>";
        List<GMarkTag> tags = extractTags(gmarkText);
        for (GMarkTag tag : tags) {
            System.out.println("Tag: " + tag.getName());
            System.out.println("Attributes: " + tag.getAttributes());
            System.out.println("Content: " + tag.getContent());
            System.out.println();
        }
    }
}