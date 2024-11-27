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
    private String level;
    private String authors;
    private String title;
    private String shortDescription;
    private String keywords;
    private String body;
    private String referenceLinks;
    private String groupName;

   
    // overloaded article constructor
    public Article(String level, String authors, String title, String shortDescription, String keywords, String body, String referenceLinks, String groupName) {
        this.id = createArticleId();
        this.level = level;
        this.authors = authors;
        this.title = title;
        this.shortDescription = shortDescription;
        this.keywords = keywords;
        this.body = body;
        this.referenceLinks = referenceLinks;
        this.groupName = groupName;
    }

    // article constructor
    public Article(int id, String level, String authors, String title, String shortDescription, String keywords, String body, String referenceLinks, String groupName) {
        this.id = id;
        this.level = level;
        this.authors = authors;
        this.title = title;
        this.shortDescription = shortDescription;
        this.keywords = keywords;
        this.body = body;
        this.referenceLinks = referenceLinks;
        this.groupName = groupName;
    }

    // Getter methods
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

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }


    // create article id
    public int createArticleId() {
        String generatedId = "";
        for (int i = 0; i < 6; i++) {
            generatedId += (int) (Math.random() * 10);
        }
        int res = Integer.parseInt(generatedId);
        return res;
	}



    // make a toString method
    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", level='" + level + '\'' +
                ", authors='" + authors + '\'' +
                ", title='" + title + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", keywords='" + keywords + '\'' +
                ", body='" + body + '\'' +
                ", referenceLinks='" + referenceLinks + '\'' +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}