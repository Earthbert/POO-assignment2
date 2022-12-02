package action.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import movie.Movie;
import java.util.Comparator;
import java.util.List;

public class Sort {
    private final String rating;
    private final String duration;

    @JsonCreator
    private Sort(@JsonProperty("rating") final String rating,
                 @JsonProperty("duration") final String duration) {
        this.rating = rating;
        this.duration = duration;
    }

    void apply(final List<Movie> movies) {
        if (rating != null && duration != null) {
            sortByDurationAndRating(movies);
            return;
        }
        if (rating != null) {
            sortByRating(movies);
            return;
        }
        if (duration != null) {
            sortByDuration(movies);
        }
    }

    private void sortByDurationAndRating(final List<Movie> movies) {
        final Comparator<Movie> comparator;
        if (duration.equals("descending")) {
            comparator = Comparator.comparing(Movie::getDuration).reversed();
        } else {
            comparator = Comparator.comparing(Movie::getDuration);
        }
        if (rating.equals("descending")) {
            comparator.thenComparing(Movie::getRating).reversed();
        } else {
            comparator.thenComparing(Movie::getRating);
        }
        movies.sort(comparator);
    }

    private void sortByRating(final List<Movie> movies) {
        final Comparator<Movie> comparator;
        if (rating.equals("descending")) {
            comparator = Comparator.comparing(Movie::getRating).reversed();
        } else {
            comparator = Comparator.comparing(Movie::getRating);
        }
        movies.sort(comparator);
    }

    private void sortByDuration(final List<Movie> movies) {
        final Comparator<Movie> comparator;
        if (duration.equals("descending")) {
            comparator = Comparator.comparing(Movie::getDuration).reversed();
        } else {
            comparator = Comparator.comparing(Movie::getDuration);
        }
        movies.sort(comparator);
    }
}
