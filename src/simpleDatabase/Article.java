package simpleDatabase;

public class Article {
    private int id;                     // Article ID
    private String level;               // Article level (beginner, intermediate, advanced, expert)
    private String groupId;             // Group ID (e.g. CSE360, CSE360-01, CSE360-02)
    private String title;               // Article title
    private String shortDescription;    // Short description/abstract
    private String keywords;            // Keywords
    private String body;                // Article body
    private String referenceLinks;      // Reference links

    // Constructor for creating a new Article instance
    public Article(int id, String level, String groupId, String title, String shortDescription, String keywords, String body, String referenceLinks) {
        this.id = id;
        this.level = level;
        this.groupId = groupId;
        this.title = title;
        this.shortDescription = shortDescription;
        this.keywords = keywords;
        this.body = body;
        this.referenceLinks = referenceLinks;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getReferenceLinks() {
        return referenceLinks;
    }

    public void setReferenceLinks(String referenceLinks) {
        this.referenceLinks = referenceLinks;
    }
}
