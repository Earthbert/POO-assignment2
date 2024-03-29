package execution;

import action.Action;
import action.filter.Filter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import database.Database;
import memento.MementoCaretakerFactory;
import memento.MementoCaretaker;
import movie.Movie;
import movie.MovieList;
import user.Credentials;
import user.User;
import user.UserAction;
import page.Page;

import java.util.List;
import java.util.Optional;

public class Execution {
    private final OutputWriter outputWriter;
    private final Page currentPage;

    private final ChangePageAction changePageAction = new ChangePageAction();
    private final OnPageAction onPageAction = new OnPageAction();
    private final DatabaseAction databaseAction = new DatabaseAction();

    private final MementoCaretaker<String, Page> prevPages = MementoCaretakerFactory.create();

    public Execution(final ArrayNode arrayNode) {
        currentPage = new Page("logout");
        outputWriter = new OutputWriter(arrayNode);
    }

    /**
     * Starts the execution of the program.
     * Goes through Action List and calls specific handler depending on action type.
     */
    public void start() {
        for (final Action action : Database.getInstance().getActions()) {
            switch (action.getType()) {
                case "change page" -> changePageAction.handle(action);
                case "on page" -> onPageAction.handle(action);
                case "database" -> databaseAction.handle(action);
                case "back" -> rollbackPage();
                default -> System.err.println("Invalid Action");
            }
        }
        if (currentPage.getUser() != null && currentPage.getUser().getCredentials().isPremium()) {
            currentPage.getUser().getRecommendation();
            outputWriter.write(null, currentPage.getUser());
        }
    }

    private void rollbackPage() {
        if (!prevPages.restoreLastState(currentPage)) {
            outputWriter.write();
            return;
        }
        switch (currentPage.getType()) {
            case "see details" -> outputWriter
                    .write(List.of(currentPage.getMovie()), currentPage.getUser());
            case "movies" -> {
                currentPage.setMovies(MovieList.available(
                        Database.getInstance().getMovies(), currentPage.getUser()));
                outputWriter.write(currentPage.getMovies(), currentPage.getUser());
            }
            default -> { }
        }
    }

    private class DatabaseAction {
        private void handle(final Action action) {
            switch (action.getFeature()) {
                case "add" -> addMovie(action.getAddedMovie());
                case "delete" -> deleteMovie(action.getDeletedMovie());
                default -> System.err.println("Invalid Database Action");
            }
        }

        private void addMovie(final Movie addedMovie) {
            if (!Database.getInstance().addMovie(addedMovie)) {
                outputWriter.write();
            }
        }

        private void deleteMovie(final String deletedMovie) {
            if (!Database.getInstance().deleteMovie(deletedMovie)) {
                outputWriter.write();
            }
        }
    }

    private class ChangePageAction {
        private void handle(final Action action) {
            switch (action.getPage()) {
                case "login" -> login();
                case "register" -> register();
                case "logout" -> logout();
                case "homepage" -> homepage();
                case "movies" -> movies();
                case "see details" -> seeDetails(action.getMovie());
                case "upgrades" -> upgrades();
                default -> System.err.println("Invalid Change Page Action");
            }
        }

        private void logout() {
            if (currentPage.hasLinkTo("logout")) {
                currentPage.setType("logout");
                currentPage.setUser(null);
                currentPage.setMovie(null);
                currentPage.setMovies(null);
                prevPages.clear();
            } else {
                outputWriter.write();
            }
        }

        private void login() {
            if (currentPage.hasLinkTo("login")) {
                currentPage.setType("login");
            } else {
                outputWriter.write();
            }
        }

        private void register() {
            if (currentPage.hasLinkTo("register")) {
                currentPage.setType("register");
                currentPage.setMovie(null);
            } else {
                outputWriter.write();
            }
        }

        private void homepage() {
            if (currentPage.hasLinkTo("homepage")) {
                prevPages.saveState(currentPage);
                currentPage.setType("homepage");
                currentPage.setMovie(null);
                currentPage.setMovies(null);
            } else {
                outputWriter.write();
            }
        }

        private void movies() {
            if (currentPage.hasLinkTo("movies")) {
                prevPages.saveState(currentPage);
                currentPage.setType("movies");
                currentPage.setMovie(null);
                final List<Movie> currentMovies = MovieList.available(
                        Database.getInstance().getMovies(), currentPage.getUser());
                currentPage.setMovies(currentMovies);
                outputWriter.write(currentMovies, currentPage.getUser());
            } else {
                outputWriter.write();
            }
        }

        private void seeDetails(final String movieName) {
            if (currentPage.hasLinkTo("see details")) {
                final Optional<Movie> currentMovie = MovieList.getMovie(
                        currentPage.getMovies(), movieName);
                if (currentMovie.isPresent()) {
                    prevPages.saveState(currentPage);
                    currentPage.setType("see details");
                    currentPage.setMovie(currentMovie.get());
                    outputWriter.write(List.of(currentMovie.get()), currentPage.getUser());
                } else {
                    outputWriter.write();
                }
            } else {
                outputWriter.write();
            }
        }

        private void upgrades() {
            if (currentPage.hasLinkTo("upgrades")) {
                prevPages.saveState(currentPage);
                currentPage.setType("upgrades");
                currentPage.setMovie(null);
            } else {
                outputWriter.write();
            }
        }
    }

    private class OnPageAction {
        private void handle(final Action action) {
            switch (action.getFeature()) {
                case "login" -> login(action.getCredentials());
                case "register" -> register(action.getCredentials());
                case "search" -> search(action.getStartsWith());
                case "filter" -> filter(action.getFilters());
                case "purchase" -> purchase();
                case "watch" -> watch();
                case "like" -> like();
                case "rate" -> rate(action.getRate());
                case "subscribe" -> subscribe(action.getSubscribedGenre());
                case "buy premium account" -> buyPremium();
                case "buy tokens" -> buyTokens(action.getCount());
                default -> System.err.println("Invalid On Page Action");
            }
        }

        private void login(final Credentials credentials) {
            if (currentPage.hasFeature("login")) {
                final User user = UserAction.login(credentials);
                if (user != null) {
                    currentPage.setType("homepage");
                    currentPage.setUser(user);
                    outputWriter.write(user);
                } else {
                    currentPage.setType("logout");
                    outputWriter.write();
                }
            } else {
                outputWriter.write();
            }
        }

        private void register(final Credentials credentials) {
            if (currentPage.hasFeature("register")) {
                final User user = UserAction.register(credentials);
                if (user != null) {
                    currentPage.setType("homepage");
                    currentPage.setUser(user);
                    outputWriter.write(user);
                } else {
                    currentPage.setType("logout");
                    outputWriter.write();
                }
            } else {
                outputWriter.write();
            }
        }

        private void search(final String startsWith) {
            if (currentPage.hasFeature("search")) {
                final List<Movie> availableMovies = MovieList.available(
                        Database.getInstance().getMovies(), currentPage.getUser());
                final List<Movie> foundMovies = MovieList.searchMovie(availableMovies, startsWith);
                currentPage.setMovies(foundMovies);
                outputWriter.write(foundMovies, currentPage.getUser());
            } else {
                outputWriter.write();
            }
        }

        private void filter(final Filter filter) {
            if (currentPage.hasFeature("filter")) {
                final List<Movie> availableMovies = MovieList.available(
                        Database.getInstance().getMovies(), currentPage.getUser());
                final List<Movie> filteredMovies = filter.apply(availableMovies);
                currentPage.setMovies(filteredMovies);
                outputWriter.write(filteredMovies, currentPage.getUser());
            } else {
                outputWriter.write();
            }
        }

        private void purchase() {
            if (currentPage.hasFeature("purchase")) {
                if (currentPage.getUser().buyMovie(currentPage.getMovie())) {
                    outputWriter.write(List.of(currentPage.getMovie()), currentPage.getUser());
                    return;
                }
            }
            outputWriter.write();
        }

        private void watch() {
            if (currentPage.hasFeature("watch")) {
                if (currentPage.getUser().watchMovie(currentPage.getMovie())) {
                    outputWriter.write(List.of(currentPage.getMovie()), currentPage.getUser());
                    return;
                }
            }
            outputWriter.write();
        }

        private void like() {
            if (currentPage.hasFeature("like")) {
                if (currentPage.getUser().likeMovie(currentPage.getMovie())) {
                    outputWriter.write(List.of(currentPage.getMovie()), currentPage.getUser());
                    return;
                }
            }
            outputWriter.write();
        }

        private void rate(final int rate) {
            if (currentPage.hasFeature("rate")) {
                if (currentPage.getUser().rateMovie(currentPage.getMovie(), rate)) {
                    outputWriter.write(List.of(currentPage.getMovie()), currentPage.getUser());
                    return;
                }
            }
            outputWriter.write();
        }

        private void subscribe(final String genreName) {
            if (currentPage.hasFeature("subscribe")) {
                if (currentPage.getUser().subscribeMovie(currentPage.getMovie(), genreName)) {
                    return;
                }
            }
            outputWriter.write();
        }

        private void buyPremium() {
            if (!currentPage.hasFeature("buy premium account")
                    || !currentPage.getUser().buyPremiumAccount()) {
                outputWriter.write();
            }
        }

        private void buyTokens(final int count) {
            if (!currentPage.hasFeature("buy tokens") || !currentPage.getUser().buyTokens(count)) {
                outputWriter.write(currentPage.getUser());
            }
        }
    }
}
