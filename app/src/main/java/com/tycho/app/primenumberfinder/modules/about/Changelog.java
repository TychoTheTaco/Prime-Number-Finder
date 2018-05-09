package com.tycho.app.primenumberfinder.modules.about;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Changelog {

    private final List<Release> releases = new ArrayList<>();

    private Changelog(final List<Release> releases){
        this.releases.addAll(releases);
    }

    public static Changelog readChangelog(final InputStream inputStream){
        final List<Release> releases = new ArrayList<>();

        final Pattern releasePattern = Pattern.compile("(([\\d?]+)/([\\d?]+)/([\\d?]+)).+?(\\d.+)");

        try {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = bufferedReader.readLine()) != null){
                final Matcher releaseMatcher = releasePattern.matcher(line);

                if (releaseMatcher.find()){
                    releases.add(new Release(releaseMatcher.group(5)));
                }else{
                    if (releases.size() > 0){
                        releases.get(releases.size() - 1).addNote(line);
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return new Changelog(releases);
    }

    public static Changelog readChangelog(final File file) throws FileNotFoundException{
        return readChangelog(new FileInputStream(file));
    }

    public static class Release{

        private final String versionName;

        private final List<String> notes = new ArrayList<>();

        public Release(final String versionName){
            this.versionName = versionName;
        }

        public void addNote(final String note){
            this.notes.add(note);
        }

        public String getVersionName() {
            return versionName;
        }

        public List<String> getNotes() {
            return notes;
        }

        public String concatenate(){
            final StringBuilder stringBuilder = new StringBuilder();
            for (String note : notes){
                stringBuilder.append(note);
                stringBuilder.append('\n');
            }
            if (stringBuilder.length() > 0){
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            return stringBuilder.toString();
        }
    }

    public List<Release> getReleases() {
        return releases;
    }

    public Release getLatestRelease(){
        if (releases.size() > 0){
            return releases.get(releases.size() - 1);
        }
        return null;
    }
}
