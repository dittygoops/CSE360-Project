package simpleDatabase;

/**
 * The Article class represents an article with various attributes such as ID, level, group ID, title, short description, keywords, body, and reference links.
 * It provides constructors to create an article instance and getter and setter methods to access and modify the article's attributes.
 * 
 * @author Aditya Gupta
 * @version 1.0
 * @since 2024-10-30
 */
public class Article {
    private int id;                     // Article ID
    private String level;               // Article level (beginner, intermediate, advanced, expert)
    private String groupId;             // Group ID (e.g. CSE360, CSE360-01, CSE360-02)
    private String title;               // Article title
    private String shortDescription;    // Short description/abstract
    private String keywords;            // Keywords
    private String body;                // Article body
    private String referenceLinks;      // Reference links

    /**
     * Constructor for creating a new Article instance.
     *
     * @param id The article ID
     * @param level The article level (beginner, intermediate, advanced, expert)
     * @param groupId The group ID (e.g. CSE360, CSE360-01, CSE360-02)
     * @param title The article title
     * @param shortDescription The short description/abstract
     * @param keywords The keywords
     * @param body The article body
     * @param referenceLinks The reference links
     */
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

    /**
     * Gets the article ID.
     *
     * @return The article ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the article ID.
     *
     * @param id The article ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the article level.
     *
     * @return The article level
     */
    public String getLevel() {
        return level;
    }

    /**
     * Sets the article level.
     *
     * @param level The article level
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * Gets the group ID.
     *
     * @return The group ID
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the group ID.
     *
     * @param groupId The group ID
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Gets the article title.
     *
     * @return The article title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the article title.
     *
     * @param title The article title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the short description/abstract.
     *
     * @return The short description/abstract
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * Sets the short description/abstract.
     *
     * @param shortDescription The short description/abstract
     */
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * Gets the keywords.
     *
     * @return The keywords
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Sets the keywords.
     *
     * @param keywords The keywords
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * Gets the article body.
     *
     * @return The article body
     */
    public String getBody() {
        return body;
    }

    /**
     * Sets the article body.
     *
     * @param body The article body
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Gets the reference links.
     *
     * @return The reference links
     */
    public String getReferenceLinks() {
        return referenceLinks;
    }

    /**
     * Sets the reference links.
     *
     * @param referenceLinks The reference links
     */
    public void setReferenceLinks(String referenceLinks) {
        this.referenceLinks = referenceLinks;
    }

    public int createArticleId() {
		String id = "";
		for (int i = 0; i < 6; i++) {
			id += (int) (Math.random() * 10);
		}
		int res = Integer.parseInt(id);
		return res;
	}

    public String toString() {
        return "ID: " + id + "\n" +
            "Level: " + level + "\n" +
            "Group ID: " + groupId + "\n" +
            "Title: " + title + "\n" +
            "Short Description: " + shortDescription + "\n" +
            "Keywords: " + keywords + "\n" +
            "Body: " + body + "\n" +
            "Reference Links: " + referenceLinks;
    }
}