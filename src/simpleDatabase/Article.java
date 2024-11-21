package simpleDatabase;

/**
 * The Article class represents an article in the help system database.
 * Each article contains metadata like ID, level, groups it belongs to, title, etc.
 * as well as the actual content in the form of a body and reference links.
 * 
 * Articles can be created, retrieved, updated and deleted through the DatabaseHelper class.
 * They can be associated with one or more groups to control access permissions.
 * 
 * The level field indicates the technical complexity:
 * - beginner: Basic introductory content
 * - intermediate: Moderate complexity requiring some background
 * - advanced: Complex topics for experienced users
 * - expert: Very advanced topics requiring deep expertise
 *
 * @author Aditya Gupta
 * @version 1.0
 * @since 2024-10-30
 */
public class Article {
    /** Unique identifier for the article */
    private int id;
    
    /** Technical complexity level (beginner/intermediate/advanced/expert) */
    private String level;
    
    /** Comma-separated list of group IDs this article belongs to */
    private String groupId;
    
    /** Title/heading of the article */
    private String title;
    
    /** Brief abstract or summary of the article content */
    private String shortDescription;
    
    /** Comma-separated list of search keywords */
    private String keywords;
    
    /** Main content text of the article */
    private String body;
    
    /** Comma-separated list of reference URLs */
    private String referenceLinks;

    /**
     * Creates a new Article with the specified attributes.
     * All fields are required and cannot be null.
     *
     * @param id Unique identifier for the article
     * @param level Technical complexity level 
     * @param groupId Comma-separated list of group IDs
     * @param title Article title/heading
     * @param shortDescription Brief summary of content
     * @param keywords Search keywords
     * @param body Main article text
     * @param referenceLinks Reference URLs
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
     * Gets the unique identifier of this article.
     *
     * @return The article ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this article.
     * Should only be used when creating new articles.
     *
     * @param id The article ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the technical complexity level of this article.
     *
     * @return The level (beginner/intermediate/advanced/expert)
     */
    public String getLevel() {
        return level;
    }

    /**
     * Sets the technical complexity level of this article.
     *
     * @param level The level to set
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * Gets the comma-separated list of group IDs this article belongs to.
     *
     * @return The group IDs
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the comma-separated list of group IDs this article belongs to.
     *
     * @param groupId The group IDs to set
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Gets the title/heading of this article.
     *
     * @return The article title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title/heading of this article.
     *
     * @param title The title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the brief summary/abstract of this article's content.
     *
     * @return The short description
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * Sets the brief summary/abstract of this article's content.
     *
     * @param shortDescription The description to set
     */
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * Gets the comma-separated list of search keywords for this article.
     *
     * @return The keywords
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Sets the comma-separated list of search keywords for this article.
     *
     * @param keywords The keywords to set
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * Gets the main content text of this article.
     *
     * @return The article body text
     */
    public String getBody() {
        return body;
    }

    /**
     * Sets the main content text of this article.
     *
     * @param body The body text to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Gets the comma-separated list of reference URLs for this article.
     *
     * @return The reference links
     */
    public String getReferenceLinks() {
        return referenceLinks;
    }

    /**
     * Sets the comma-separated list of reference URLs for this article.
     *
     * @param referenceLinks The reference links to set
     */
    public void setReferenceLinks(String referenceLinks) {
        this.referenceLinks = referenceLinks;
    }

    /**
     * Returns a string representation of this article containing all its fields.
     * Useful for debugging and logging purposes.
     *
     * @return A multi-line string with all article fields
     */
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