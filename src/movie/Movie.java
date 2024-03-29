package movie;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import observer.Subject;

import java.util.ArrayList;

public final class Movie extends Subject {
    private final String name;
    private final String year;
    private final int duration;
    private final ArrayList<String> genres;
    private final ArrayList<String> actors;
    private final ArrayList<String> countriesBanned;
    private int numLikes;
    private int numRatings;
    private int ratingSum;

    @JsonCreator
    private Movie(@JsonProperty("name") final String name,
                  @JsonProperty("year") final String year,
                  @JsonProperty("duration") final int duration,
                  @JsonProperty("genres") final ArrayList<String> genres,
                  @JsonProperty("actors") final ArrayList<String> actors,
                  @JsonProperty("countriesBanned") final ArrayList<String> countriesBanned) {
        this.name = name;
        this.year = year;
        this.duration = duration;
        this.genres = genres;
        this.actors = actors;
        this.countriesBanned = countriesBanned;
    }

    public Movie(final Movie movie) {
        this.name = movie.name;
        this.year = movie.year;
        this.duration = movie.duration;
        this.genres = movie.genres;
        this.actors = movie.actors;
        this.countriesBanned = movie.countriesBanned;
        this.numLikes = movie.numLikes;
        this.numRatings = movie.numRatings;
        this.ratingSum = movie.ratingSum;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj.getClass() == String.class) {
            return obj.equals(this.name);
        }
        if (obj.getClass() == Movie.class) {
            return ((Movie) obj).getName().equals(this.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Called when a movie receives a like.
     */
    public void getLiked() {
        numLikes++;
    }

    /**
     * Called when a movie receives a rating.
     * @param rating rating
     */
    public void rate(final int rating) {
        ratingSum += rating;
        numRatings++;
    }

    /**
     * Called when a movie receives a new rating.
     * @param rating rating
     * @param prevRating previous rating
     */
    public void rateAgain(final int rating, final int prevRating) {
        ratingSum += rating - prevRating;
    }

    /**
     * Getter for rating
     * It calculates the rating everytime it's called.
     */
    public double getRating() {
        if (numRatings > 0) {
            return (double) ratingSum / (double) numRatings;
        }
        return 0;
    }

    public int getNumLikes() {
        return numLikes;
    }

    public int getNumRatings() {
        return numRatings;
    }

    public String getName() {
        return name;
    }

    public String getYear() {
        return year;
    }

    public int getDuration() {
        return duration;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }

    public ArrayList<String> getActors() {
        return actors;
    }

    public ArrayList<String> getCountriesBanned() {
        return countriesBanned;
    }
}
