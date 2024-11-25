package it.unibo.mvc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) throws IOException {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        Configuration.Builder cf = new Configuration.Builder();
        try {
            List<String> lines = Files.readAllLines(new File(ClassLoader.getSystemResource("config.yml").getPath()).toPath(), StandardCharsets.UTF_8);
                for(String line : lines){
                    String[] sr = line.split(":");
                    if(sr.length == 2){
                        final int value = Integer.parseInt(sr[1].trim());
                        if(sr[0].contains("min")){
                            cf.setMin(value);
                        } 
                        else if (sr[0].contains("max")) {
                            cf.setMax(value);
                        }
                        else {
                            cf.setAttempts(value);
                        }
                    }
 
                }
        } catch ( IOException | NumberFormatException e){
            System.out.print("negro");
        }
        final Configuration configuration = cf.build();
        if (configuration.isConsistent()) {
            this.model = new DrawNumberImpl(configuration);
        }
        else{
            this.model = new DrawNumberImpl(new Configuration.Builder().build());
        }
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException, IOException {
        new DrawNumberApp(new DrawNumberViewImpl());
    }

}
