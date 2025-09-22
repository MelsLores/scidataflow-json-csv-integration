package com.example.scidataflow_json_csv_integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Model class representing a scientific publication for scientometric analysis.
 * 
 * This class represents a research publication with bibliometric data including
 * identifiers, authorship, publication details, and citation metrics.
 * 
 * @author Melany Rivera
 * @since 21/09/2025
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Publication {

    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("authors")
    private List<String> authors;

    @JsonProperty("journal")
    private String journal;

    @JsonProperty("year")
    private Integer year;

    @JsonProperty("citations")
    private Integer citations;

    @JsonProperty("keywords")
    private List<String> keywords;

    /**
     * Default constructor for Jackson deserialization.
     */
    public Publication() {
        // Default constructor
    }

    /**
     * Constructs a Publication with all required fields.
     * 
     * @param id unique publication identifier
     * @param title publication title
     * @param authors list of author names
     * @param journal journal name
     * @param year publication year
     * @param citations citation count
     * @param keywords list of keywords
     */
    public Publication(String id, String title, List<String> authors, String journal, 
                      Integer year, Integer citations, List<String> keywords) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.journal = journal;
        this.year = year;
        this.citations = citations;
        this.keywords = keywords;
    }

    // Getters and Setters

    /**
     * Gets the publication ID.
     * 
     * @return publication identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the publication ID.
     * 
     * @param id publication identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the publication title.
     * 
     * @return publication title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the publication title.
     * 
     * @param title publication title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the list of authors.
     * 
     * @return list of author names
     */
    public List<String> getAuthors() {
        return authors;
    }

    /**
     * Sets the list of authors.
     * 
     * @param authors list of author names
     */
    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    /**
     * Gets the journal name.
     * 
     * @return journal name
     */
    public String getJournal() {
        return journal;
    }

    /**
     * Sets the journal name.
     * 
     * @param journal journal name
     */
    public void setJournal(String journal) {
        this.journal = journal;
    }

    /**
     * Gets the publication year.
     * 
     * @return publication year
     */
    public Integer getYear() {
        return year;
    }

    /**
     * Sets the publication year.
     * 
     * @param year publication year
     */
    public void setYear(Integer year) {
        this.year = year;
    }

    /**
     * Gets the citation count.
     * 
     * @return number of citations
     */
    public Integer getCitations() {
        return citations;
    }

    /**
     * Sets the citation count.
     * 
     * @param citations number of citations
     */
    public void setCitations(Integer citations) {
        this.citations = citations;
    }

    /**
     * Gets the list of keywords.
     * 
     * @return list of keywords
     */
    public List<String> getKeywords() {
        return keywords;
    }

    /**
     * Sets the list of keywords.
     * 
     * @param keywords list of keywords
     */
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    /**
     * Gets authors as a concatenated string for CSV export.
     * 
     * @return authors separated by semicolons
     */
    public String getAuthorsString() {
        return authors != null ? String.join("; ", authors) : "";
    }

    /**
     * Gets keywords as a concatenated string for CSV export.
     * 
     * @return keywords separated by semicolons
     */
    public String getKeywordsString() {
        return keywords != null ? String.join("; ", keywords) : "";
    }

    /**
     * Returns a string representation of this publication.
     * 
     * @return string representation containing all publication fields
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Override
    public String toString() {
        return "Publication{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", authors=" + authors +
                ", journal='" + journal + '\'' +
                ", year=" + year +
                ", citations=" + citations +
                ", keywords=" + keywords +
                '}';
    }

    /**
     * Compares this publication with another object for equality.
     * Two publications are considered equal if they have the same ID.
     * 
     * @param o the object to compare with
     * @return true if objects are equal, false otherwise
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Publication that = (Publication) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    /**
     * Returns hash code value for this publication.
     * Hash code is based on the publication ID.
     * 
     * @return hash code value
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}